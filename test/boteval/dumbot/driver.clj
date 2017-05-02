(ns boteval.dumbot.driver
  " implementation of a driver, for dumbot. a driver bridges between scenario code
    and a particular bot implementation "

  (:require [org.boteval.driverInterface :refer [Driver]]) ; the driver interface
  (:require [boteval.dumbot.core :as bot])                 ; dubmot functions
  (:require [org.boteval.engine.api :as api]))             ; this is not needed in a real driver, only used for easy dumbot drivering

; sessions vector
(def sessions (atom (sorted-map)))

; the driver
(def driver
  "a driver for our test bot"
  (reify Driver

    ; callback called by the framework, not the scenario writer
    (receiveFromBot [this session-id message]
      #_(println "message received from bot" message "for session-id" session-id)
      (swap! sessions (fn [sessions] (update sessions session-id (fn [messages] (conj messages message)))))
      nil
    )

    ; connects to the bot
    (connectToBot [this]
      (bot/connect api/receiveFromBotHandler)) ;dumbot is a special dumb bot, it writes directly to our callback but real bots won't

    ; opens a new session, returns its bot allocated session id
    (openBotSession [this]
      (let [session-id (bot/open-session)]
        (swap! sessions (fn [sessions] (assoc sessions session-id [])))
        session-id))

    ; send to the bot
    (sendToBot [this session-id message]
      #_(println "sample driver sending message" message)
      (bot/receive session-id message)
    )

    (getReceived [this session-id]
      (let [messages (get @sessions session-id)]
        (swap! sessions (fn [sessions] (assoc sessions session-id [])))
        messages))
))
