(ns boteval.samples.evaluator

  " a sample evaluator, which is also part of our test suite "

  (:require [boteval.dumbot.core])
  (:use     [boteval.dumbot.driver])
  (:use     [org.boteval.engine.api])
  (:use     [org.boteval.engine.evaluator-api])
  (:use     [boteval.datastore-clean])
  (:require [org.boteval.self :as self])
  (:require [org.boteval.util :as util])
  (:require [org.boteval.defaultLogger.core :as logger])
  (:require [honeysql.core :as sql])
  (:require [honeysql.helpers :refer :all])
  (:require [clojure.test :refer :all]))


(defn ^:private test-scenario [_]
  " just a scenario for our test "
  (let [session-id (openBotSession)]
    (sendToBot session-id "hi")))


(defn sample-evaluator [logger]
  " sample evaluator, that inspects data of the last execution of a scenario, or runs it for the first time "
  (let [latest
    (get-latest-scenario-execution-or-execute
      {:scenario-name (util/clean-fn-name test-scenario) :scenario-project-name (:project-name self/unique-key)}
      logger
      test-scenario
      {})]

    (println latest)))


(deftest ^:self evaluators
  " tests part of the valuators api
    TODO: make into a database coordinated test that verifies
          get-latest-scenario-execution-or-execute works in both its cases "

  (datastore-clean)

  (let [logger logger/default-logger]
    (init
       self/unique-key
       driver
       logger)
    (connectToBot)

    (sample-evaluator logger)))
