(ns org.boteval.loggerInterface)

(defprotocol Logger

  " a protocol for bot activity logging and the retrieval of logged bot activity.
    this protocol defines both the logging and the retrieval, both of which are
    equally logger-specific. "

  (init [_ project-meta])

  ;; writing methods

  (log-scenario-execution-start [_ scenario-name scenario-hierarchy start-time parameters])

  (log-scenario-execution-end [_ scenario-execution-id end-time])

  (log [_ scenario-hierarchy message-record])

  ;; retrieval methods

  (get-from-db [_ honey-sql-map]) ; a utility method we should actually logically place elsewhere

  (get-logged-exchanges [this scenario-execution-id])

  (shutdown [_]))
