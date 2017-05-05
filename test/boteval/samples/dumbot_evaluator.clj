(ns boteval.samples.dumbot-evaluator

  " a sample evaluator, which is also part of our test suite "

  (:require
     [boteval.dumbot.core]
     [org.boteval.self :as self]
     [org.boteval.util :as util]
     [org.boteval.defaultLogger.core :as logger]
     [honeysql.core :as sql]
     [honeysql.helpers :refer :all]
     [clojure.test :refer :all])
  (:use
     [boteval.dumbot.driver]
     [org.boteval.engine.api]
     [org.boteval.engine.evaluator-api]
     [boteval.datastore-clean]))


(defn ^:private sample-scenario [_]

  " just a scenario sending messages to our bot "

  (let [session-id (openBotSession)]
    (dotimes [_ 500] (sendToBot session-id "hi"))))


(defn sample-evaluator [logger]

  " sample evaluator, that inspects data of the last execution of one sample scenario, or runs it for the first time,
    only to check whether on that scenario, dumbot always sends the same message text "

  (let [exchanges
    (analyze-latest-scenario-execution-or-execute
      {:scenario-name (util/clean-fn-name sample-scenario) :scenario-project-name (:project-name self/unique-key)}
      logger
      sample-scenario
      {})]

    (println exchanges)
    (every? true? ; just checkin that dumbot always sends the same single sentence
      (map
        #(if (:is_user %)
           true
           (= (:text %) "got your message, no comments"))
        exchanges))))


(deftest ^:self dumb-dumbot-evaluator

  " sample evaluator evaluating that dumbot says only one sentence all of the time "

  (datastore-clean)

  (let [logger logger/default-logger]
    (init
       self/unique-key
       driver
       logger)
    (connectToBot)

    (println (sample-evaluator logger))))
