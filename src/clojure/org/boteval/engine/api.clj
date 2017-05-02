(ns org.boteval.engine.api

  " api to be used by user code "

  (:require [org.boteval.driverInterface :refer [Driver]]) ; the driver interface
  (:require [org.boteval.loggerInterface :refer [Logger]]) ; the logger interface
  (:use [org.boteval.time])
  (:require [org.boteval.self :as self])
  (:use [org.boteval.self-logging])
  (:use [org.boteval.util])
  (:require [cheshire.core :as json])
  #_(:require [clojure.repl]) ; for demunge if we'll need it after all
  (:gen-class))


(defn init
  " initializes the api functions to use the given driver and logger
    todo: this is not concurrency-safe, one init will overwrite the other.
    todo: consider a design providing the api not through `defn` per api endpoint (?) "
  [project-meta driver logger]
  {:pre [(contains? project-meta :project-name)
         (contains? project-meta :project-owner)
         (satisfies? Driver driver)
         (satisfies? Logger logger)]}

    (self-log "api initializing with given driver and logger")

    (. logger init (assoc project-meta :project-git-hash self/project-git-hash))

    ;; this var is dynamic for the sake of the stack discipline (https://clojure.org/reference/vars) which
    ;; perfectly matches the notion of `run-scenario` keeping track of the scenario hierarchy
    (def ^:dynamic ^:private scenario-execution-hierarchy '())

    (defn receiveFromBotHandler [session-id bot-message]
      (. logger log scenario-execution-hierarchy
         {:text bot-message
          :time (sql-time)
          :is-user false
          :session-id session-id})

      (. driver receiveFromBot session-id bot-message)
      nil)

    (defn connectToBot []
      (. driver connectToBot))

    (defn openBotSession []
      (. driver openBotSession))

    (defn sendToBot [session-id message]
      (. logger log scenario-execution-hierarchy
         {:text message
          :time (sql-time)
          :is-user true
          :session-id session-id})

      (. driver sendToBot session-id message))

    (defn getReceived [session-id]
      (. driver getReceived session-id))

    (defn ^:private run-scenario-impl [fn scenario-name fn-params]
      {:pre (map? fn-params)
       :post (number? %)}

      " this is the function that runs a scenario, returning its logger assigned execution id
        a scenario should always be run through this function, other than during its development "
      (let [scenario-execution-id (. logger log-scenario-execution-start scenario-name scenario-execution-hierarchy (sql-time) fn-params)]
        (binding [scenario-execution-hierarchy
           (conj scenario-execution-hierarchy {:scenario-name scenario-name :scenario-execution-id scenario-execution-id})]
               #_(self-log scenario-execution-hierarchy)
               (fn fn-params))
        (. logger log-scenario-execution-end scenario-execution-id (sql-time))

        scenario-execution-id))

    nil
)

(defmacro run-scenario [fn-name fn-params]
  " automatically passes the function's full name as the scenario name "
   (list 'run-scenario-impl fn-name (list `clean-fn-name fn-name) fn-params))

; a started attempt on a macro for defining scenario functions, that would automatically add
; a first argument (named context) to them and hinge metadata on them. abandoned for now.
; (see http://stackoverflow.com/a/989482/1509695 for how to correctly add metadata)
#_(defmacro def-scenario [given-name params body]
  " defines a scenario function for the given name, params and body"
  (list 'defn ^{:scenario-name given-name} given-name (vec ['context params]) body))

