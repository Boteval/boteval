(ns boteval.samples-test

  " runs the clojure samples, as that it only tests that they do not throw "

  (:require [clojure.test :refer :all])
  (:require [org.boteval.self :as self])
  (:use [org.boteval.engine.api]
        [boteval.dumbot.driver]
        [org.boteval.defaultLogger.core]
        [boteval.samples.scenarios]
        [boteval.datastore-clean]))

(deftest ^:samples run
    (init
       self/unique-key
       driver
       default-logger)
    (connectToBot)
    (run-scenario master-scenario-1 [])
    (run-scenario scenario-3 []))

