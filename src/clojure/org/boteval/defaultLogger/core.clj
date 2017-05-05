(ns org.boteval.defaultLogger.core

  "#our default logger

  a component logging messages sent and received to/from the target bots → into the data store,
  the data store being mysql here, we use https://github.com/jkk/honeysql for query building.
  well as providing methods to query its logged data.

  some outstanding TODO items:

  + add time & message buffering for proper performance (tricky while relying on db-side auto incrementing values)
  + handle draining the buffer on program exit too "

  (:require
      [org.boteval.loggerInterface :refer [Logger]]
      [hikari-cp.core :refer :all]
      [clojure.java.jdbc :as jdbc]
      [honeysql.core :as sql]
      [honeysql.helpers :refer [select from where]]
      [cheshire.core :as json])
  (:use
      [org.boteval.self-logging]))


(load "core_db_util")
(load "core_getScenarioId")


(def default-logger (reify Logger

   ;; init method
   (init
      [this {:keys [project-name version project-owner project-git-hash]}]
      {:pre [(some? project-name)
             (some? project-owner)
             (some? project-git-hash)]}

        (def project-id
          "the get-or-create-id in/from the database dance, for our project name and owner
           todo: make concurrency-friendly same as in getting a scenario id, possibly by
                 making them share a common core"
          (if-let [project-id
            (:id (first
              (let [sql-statement (-> (select :id) (from :projects) (where [:= :name project-name] [:= :owner project-owner]) sql/format)]
                (jdbc/with-db-connection [connection {:datasource datasource}]
                   (jdbc/query connection sql-statement)))))]

              project-id

              (do
                (self-log "registering an id for the project")
                (first (db-execute
                  (-> (insert-into :projects)
                      (values [{:name project-name
                                :owner project-owner
                                :version_name nil ; later make this an optional argument
                                ;:git_hash project-git-hash
                                }])))))))

        (self-log "project id is " project-id)

        (def project-git-hash project-git-hash))

    ;; the bot-exchange logging method (messages sent and received from the bot by a bot driver)
    (log
      [this scenario-execution-hierarchy {:keys [text is-user time session-id]}]

      (db-execute
         (-> (insert-into :exchanges)
             (values [{:text text
                       :is_user is-user
                       :exchange_time time
                       :session_id session-id
                       :scenario_execution_id (:scenario-execution-id (first scenario-execution-hierarchy))}]))))

    ;; scenario execution start method
    (log-scenario-execution-start
      [this scenario-name scenario-execution-hierarchy start-time parameters]
      {:pre (map? parameters) }

      (let [scenario-id (get-scenario-id project-id scenario-name)]
        (let [parent-scenario-execution-id (:scenario-execution-id (first scenario-execution-hierarchy))]
           (insert-and-get-id
             :scenario_executions
             {:scenario_id scenario-id
              :parent_id parent-scenario-execution-id
              :started start-time
              :ended nil
              :parameters (json/generate-string parameters)}))))

    ;; scenario execution end method
    (log-scenario-execution-end
      [this scenario-execution-id end-time]
      (db-execute
         (-> (update :scenario_executions)
             (where [:= :id scenario-execution-id])
             (sset {:ended end-time}))))

    ;; shutdown method
    (shutdown [this]
      (close-datasource datasource))


    (get-from-db [this honey-sql-map]
       (let [sql-statement (sql/format honey-sql-map)]
          (println sql-statement)
          (jdbc/with-db-connection [connection {:datasource datasource}]
             (jdbc/query connection sql-statement))))


    (get-logged-exchanges [this scenario-execution-id]
      {:pre (number? scenario-execution-id)}
      " retrieves all exchanges for given scenario execution "
      (map (fn [exchange-map] (clojure.core/update exchange-map :is_user #(if (= % 1) true false)))
         (. this get-from-db (->
            (select :text :is_user :exchange_time :session_id :scenario_execution_id)
            (from :exchanges)
            (where [:= :scenario_execution_id scenario-execution-id])))))

))


