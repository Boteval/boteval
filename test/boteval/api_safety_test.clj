(ns boteval.api-safety-test

  " placeholder for tests for api safety (basically, that abusing the api produces exceptions before anything actually happens "

  (:require [clojure.test :refer :all])
  (:require [org.boteval.self :as self])
  (:use [org.boteval.engine.api]
        [boteval.dumbot.driver]
        [org.boteval.defaultLogger.core]
        [boteval.samples.scenarios]
        [boteval.datastore-clean]))

(deftest ^:api-safety fail-without-run-scenario
  " driving the bot outside of a `run-scenario` wrapper should fail, even if everything was properly initialized.
    this specific test code is bound to be replaced when the api initialization refactors, so it should be entirely
    re-written to match the new api, rather than fixed-up, at that time. that is because at present, this test
    tightly wraps around the ailments of the current api "

  (is (thrown? Exception
     (do
       (init
          self/unique-key
          driver
          default-logger)

       (connectToBot)

       (let [session-id (openBotSession)]
         (sendToBot session-id "hi"))))))
