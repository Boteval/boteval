;;; some sample scenarios

(ns boteval.samples.scenarios
  (:use [org.boteval.engine.api])
  (:use [org.boteval.util]))

(defn scenario-1 [{:keys [session-id]}]
  (sendToBot session-id "Hi bot")
)

(defn scenario-2 [{:keys [session-id]}]
  (sendToBot session-id "What's up bot?"))

(defn scenario-3 [_]
  (let [session-id (openBotSession)]
    (sendToBot session-id "מה?")
    #_(println session-id "received " (getReceived session-id))))

(defn master-scenario-1 [_]
  (let [session-id (openBotSession)]
    (run-scenario scenario-1 {:session-id session-id})
    (run-scenario scenario-2 {:session-id session-id})
    #_(println session-id "received " (getReceived session-id))))
