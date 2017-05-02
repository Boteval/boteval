;; function for cleaning the logger's datastore, necessary for tests code only
;; todo: move to a namespace unavailable for library users?

(ns boteval.datastore-clean
  (:require [clojure.java.shell])
  (:use org.boteval.self-logging))

(defn datastore-clean []
  (self-log "clearing the logger's datastore...")
  (let [result (clojure.java.shell/sh "bash" "-c" "mysql -u root boteval < resources/deploy/mysql.sql")]
    (if-not (= (:exit result) 0)
      (do
        (self-log "running the database reinit mysql script returned exit code " (:exit result))
        (self-log "stdout: " (:out result))
        (self-log "stderr: " (:err result))
        (throw (Exception. (:err result)))))))
