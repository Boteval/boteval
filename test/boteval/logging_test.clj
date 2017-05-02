(ns boteval.logging-test

  " mildly stress tests the logging
    assumes that no other tests concurrently run "

  (:require [clojure.test :refer :all])
  (:require [org.boteval.self :as self])
  (:use [org.boteval.engine.api]
        [boteval.dumbot.driver]
        [boteval.samples.scenarios]
        [boteval.datastore-clean])
  (:require [clojure.java.io :as io])
  (:require [org.boteval.defaultLogger.core :as logger])
  (:require [honeysql.core :as sql])
  (:require [honeysql.helpers :refer :all])
  (:use [org.boteval.util]))


(defn ^:private get-logged-executions []
  (. logger/default-logger get-from-db (-> (select :scenario_id :id :parent_id) (from :scenario_executions))))

(defn ^:private get-logged-scenarios []
  (. logger/default-logger get-from-db (-> (select :id) (from :scenarios))))

(defn ^:private get-logged-exchanges []
  (. logger/default-logger get-from-db (-> (select :text :scenario_execution_id) (from :exchanges))))


(deftest ^:self logging-under-concurrency

  (datastore-clean)

  (init
       self/unique-key
       driver
       logger/default-logger)

  (connectToBot)

  (let [phrases ["Hi, good morning" "Hi good morning" "hi good morning"] ; (clojure.string/split-lines (slurp (io/file (io/resource "samples/paraphrases.txt"))))
        single-message-runner (fn [message] (sendToBot (openBotSession) message))
        run-scenario-for-phrase (fn [text] (run-scenario single-message-runner text))
        concurrency 5]

      (dotimes [i concurrency]
         (doall (pmap run-scenario-for-phrase phrases)))

      (let [logged-executions (get-logged-executions)
            logged-scenarios (get-logged-scenarios)
            logged-exchanges (get-logged-exchanges)]

         (is (= (count logged-executions) (* concurrency (count phrases)))
             "number of logged executions should equal the number of executions made during this test")

         (is (= (count logged-scenarios) 1)
             "only one scenario should be logged during this test")

         (is (= (count logged-exchanges) (* (count logged-executions) 2))
             "there should be two exchanges logged per execution, in this scenario")

)))



