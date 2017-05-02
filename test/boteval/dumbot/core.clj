;; dumbot - a silly bot, supporting separate user sessions but no memory about users
;; although real bots may not afford so, dumbot allows a single connection to hold many sessions

(ns boteval.dumbot.core)

;; this bot is limited to MAXINT sessions
(def max-session-id (atom 0))

(defn connect [reply-callback]
  (def reply reply-callback))

(defn open-session []
  (let [session-id @max-session-id]
    (swap! max-session-id inc)
    session-id))

(defn- send-to-client [session-id message]
  (reply session-id message))

(defn receive [session-id message]
  #_(println "received message " message "from session id" session-id)
  (send-to-client session-id "got your message, no comments"))
