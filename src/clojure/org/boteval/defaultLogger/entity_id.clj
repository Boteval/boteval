(ns org.boteval.defaultLogger.entity-id

  " helper extension for org.boteval.defaultLogger.core ―
    exposes a function that gets-or-sets a unique id for a given entity

    TODO: we can use partial function application to bind an entity rather than
          have conditional logic sprinkeld per entity type as currently.

          none the same we'll need to constrain the allowed entity types,
          a clojure type-hierarchy would make sense "

  (:require
     [hikari-cp.core :refer :all]
     [clojure.java.jdbc :as jdbc]
     [honeysql.core :as sql]
     [honeysql.helpers :refer :all]
     [org.boteval.defaultLogger.db :refer :all])
  (:use
     [org.boteval.self-logging]
     [clojure.test]))

;; using here types seamlessly useful for honeysql
(derive ::scenarios ::entity-type)
(derive ::analyzers ::entity-type)

;; a map associating every ::entity-type to a dedicated index atom
(def index-mirors
  (apply array-map (mapcat (fn [entity-type] [entity-type (atom (sorted-map))]) (descendants ::entity-type))))

;(def ^:private scenarios-index-mirror (atom (sorted-map)))
;(def ^:private analyzers-index-mirror (atom (sorted-map)))

(defn ^:private get-entity-id-from-db [project-id entity-name entity-type]
  {:pre (isa? entity-type ::entity-type)}

  "from the database, returns, or sets and returns, an id for the given entity.
   the id is obtained through MySQL auto-increment, in case the entity is new to the database.
   as a race-condition with other threads can arise, if another thread already added our entity
   between the test and add phases, we just return its id already created through that thread (but
   this may waste an auto-increment in the table).

   as an alternative way to syncrhonize the unique creation of an id per entity, but without going to the database,
   we could possibly guard the two-phase process with a lock (but not a transaction, because of the auto-increment),
   roughly as follows:

       ```
       (def ^:private index-mirror-locker (Object.))
       (locking index-mirror-locker ..... )
       ```

   locks have their cost, but going to the database has a cost too. "

  (letfn [(query-for-id [connection]
     (:id (first
         (let [sql-statement (-> (select :id) (from entity-type) (where [:= :project_id project-id] [:= :name entity-name]) sql/format)]
             (jdbc/query connection sql-statement)))))]


  (jdbc/with-db-connection [connection {:datasource datasource}]
    (if-let [entity-id (query-for-id connection)] entity-id
      (do
        (self-log "adding entity " entity-name " to database") ;; ADD ENTITY TYPE
        (try
          (insert-and-get-id entity-type {:name entity-name :project_id project-id})
          (catch Exception e
            (if-let [entity-id (query-for-id connection)] entity-id
              (throw e)))))))))


(defn get-entity-id [project-id entity-name entity-type]
  {:pre (some? entity-name) :post (some? %)}

  " gets an entity id for the given entity name, either from the in-memory cache or from the database "

  (let [index-mirror (entity-type index-mirors)]

    (if-let [entity-id (get @index-mirror entity-name)] entity-id
      (let [entity-id (get-entity-id-from-db project-id entity-name entity-type)]
        (swap! index-mirror #(assoc %1 entity-name entity-id))
        (get @index-mirror entity-name)))))
