(ns org.boteval.defaultLogger.core
  " helper extension for org.boteval.defaultLogger.core ―
    exposes a function that gets-or-sets an id for a given scenario without much database access "

  (:use clojure.test)
  (:require [hikari-cp.core :refer :all])
  (:require [clojure.java.jdbc :as jdbc])
  (:require [honeysql.core :as sql])
  (:require [honeysql.helpers :refer :all])
  (:use [org.boteval.self-logging]))

;; a hikari connection pool definition
;; (may optimize for performance following https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration)
(def ^:private datasource
  (make-datasource {:driver-class-name "com.mysql.jdbc.jdbc2.optional.MysqlDataSource"
                    :jdbc-url          "jdbc:mysql://localhost/boteval"
                    :username          "boteval"
                    :password          "boteval234%^&"
                    :useSSL            false})) ; using SSL without a trust store for server verification will flood the logs

;; a wrapper for database writing
;; if no transactional connection is provided, obtains a new one, otherwise reuses the given one
;; TODO: it can be seen in stack traces, that this leads to clojure.java.jdbc/execute-batch.
;;       this may be misaligned with how we use this function to execute a single statement,
;;       in terms of performance optimization.
(defn ^:private db-execute
  ([honey-sql-map]
     (jdbc/with-db-connection [connection {:datasource datasource}]
        (db-execute connection honey-sql-map)))

  ([connection honey-sql-map]
    (let [sql-statement (sql/format honey-sql-map)]
       (jdbc/execute! connection sql-statement {:transaction? false}))))

;; insert one row and get its database assigned auto-incremented key
(defn ^:private insert-and-get-id
  [table-name row-map]
  {:pre (map? row-map)}
    (let [honey-sql (-> (insert-into table-name) (values [row-map]) sql/format)
          new-id
            (jdbc/with-db-connection [connection {:datasource datasource}]
               (jdbc/execute! connection honey-sql)
                 (:id (first (jdbc/query connection "select last_insert_id() id"))))] ; this is mysql specific
             new-id))




