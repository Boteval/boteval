(ns org.boteval.loggerInterface)

(defprotocol Logger
  "a protocol for a logger"

  (init [_ project-meta])

  (log-scenario-execution-start [_ scenario-name scenario-hierarchy start-time parameters])

  (log-scenario-execution-end [_ scenario-execution-id end-time])

  (log [_ scenario-hierarchy message-record])

  (get-from-db [_ honey-sql-map]) ; TODO: rename more neutrally

  (shutdown [_]))
