(ns org.boteval.defaultLogger.core
  " helper extension for org.boteval.defaultLogger.core ―
    exposes a function that gets-or-sets a unique id for a given scenario "

  (:use clojure.test)
  (:require [hikari-cp.core :refer :all])
  (:require [clojure.java.jdbc :as jdbc])
  (:require [honeysql.core :as sql])
  (:require [honeysql.helpers :refer :all])
  (:use [org.boteval.self-logging]))

(def ^:private index-mirror (atom (sorted-map)))

(defn ^:private get-scenario-id-from-db [project-id scenario-name]

  "from the database, returns, or sets and returns, a scenario id for the given scenario.
   the id is obtained through MySQL auto-increment, in case the scenario is new to the database.
   as a race-condition with other threads can arise, if another thread already added our scenario
   between the test and add phases, we just return its id already created through that thread (but
   this might cause wasting an auto-increment in the table).

   as an alternative way to syncrhonize the unique creation of an id per scenario, but without going to the database,
   we could guard the two-phase process with a lock (but not a transaction, because of the auto-increment),
   as follows:

   ```
   (def ^:private index-mirror-locker (Object.))
   (locking index-mirror-locker ..... )
   ```

   locks have their cost, but going to the database has a cost too.
  "

  (letfn [(query-for-id [connection]
     (:id (first
         (let [sql-statement (-> (select :id) (from :scenarios) (where [:= :project_id project-id] [:= :name scenario-name]) sql/format)]
             (jdbc/query connection sql-statement)))))]


  (jdbc/with-db-connection [connection {:datasource datasource}]
    (if-let [scenario-id (query-for-id connection)] scenario-id
      (do
        (self-log "adding scenario " scenario-name " to database")
        (try
          (insert-and-get-id :scenarios {:name scenario-name :project_id project-id})
          (catch Exception e
            (if-let [scenario-id (query-for-id connection)] scenario-id
              (throw e)))))))))


(defn get-scenario-id [project-id scenario-name]
  {:pre (some? scenario-name) :post (some? %)}

  "gets a scenario id for the given scenario name, either from the in-memory cache or from the database"

  (if-let [scenario-id (get @index-mirror scenario-name)] scenario-id
    (let [scenario-id (get-scenario-id-from-db project-id scenario-name)]
      (swap! index-mirror #(assoc %1 scenario-name scenario-id))
      (get @index-mirror scenario-name))))
