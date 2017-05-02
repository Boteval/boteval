(ns org.boteval.engine.evaluator-api

  " api to be used by user code "

  (:require [org.boteval.driverInterface :refer [Driver]]) ; the driver interface
  (:require [org.boteval.loggerInterface :refer [Logger]]) ; the logger interface
  (:use [org.boteval.time])
  (:require [org.boteval.self :as self])
  (:use [org.boteval.self-logging])
  (:use [org.boteval.engine.api])
  (:require [cheshire.core :as json])
  (:require [honeysql.core :as sql])
  (:require [honeysql.helpers :refer :all])
  (:gen-class))


(defn scenario-specifier [arg logger]
  " typing function "
  (cond
    (and (map? arg) (every? arg [:scenario-name :scenario-project-name])) :scenario-unique-key
    (number? arg) :scenario-id
    :default (throw (Exception. "invalid scenario specification"))))

(defmulti get-scenario-executions
  " returns all executions for given scenario "
  scenario-specifier)

(defmethod get-scenario-executions :scenario-id
  [scenario-id logger]
  " returns the executions for given database-assgined scenario-id "
  {:pre (some? scenario-id)}
    (. logger get-from-db (->
      (from :scenario_executions)
      (select :scenario-id :id :parent_id :started :ended :parameters)
      (where [:= :scenario_id scenario-id]))))

(defmethod get-scenario-executions :scenario-unique-key
  [{:keys [scenario-name scenario-project-name]} logger]
   " returns the executions for given scenario identification.
     (TODO) https://github.com/boteval/boteval/issues/7
     actually, this is not a very solid way to get a unique scenario,
     a unique key for a scenario involves more than a project name. it should
     be made to involve a project id, or (recursively) a unique key of a project "
    {:pre [(some? scenario-name)
           (some? scenario-project-name)]}

  (let
    [scenario-id-rows
       (. logger get-from-db (->
         (select :s.id)
         (from [:scenarios :s] [:projects :projects])
         (where [:= :s.name scenario-name] [:= :projects.name scenario-project-name] [:= :s.project_id :projects.id])))

     ids (count scenario-id-rows)]

     (println ids scenario-id-rows)

        (cond
          (> ids 1) (throw (Exception. (str "error in uniquely identifying the input scenario: only one scenario id was expected but " ids " ids were found in the database. oops ...")))
          (= ids 1) (get-scenario-executions (:id (first scenario-id-rows)) logger)
          :default nil)))


(defn get-latest-scenario-execution [scenario-specifier logger]
  " returns the latest execution for given scenario "
  " NOTE: latest is elusive semantics. one execution might start after the other but finish first; which one is latest then? "
  (println scenario-specifier)
  (let
    [executions (get-scenario-executions scenario-specifier logger)]
    (first (reverse (sort-by :started executions))))) ; latest here means latest started


(defn get-latest-scenario-execution-or-execute [scenario-specifier logger scenario-fn scenario-fn-params]
  " same as get-latest-scenario-execution, only runs the given scenario function, if no existing scenario execution matches.
    note that this is obviously only useful, when the scenario function belongs in the same project as this code"
  (or
    (get-latest-scenario-execution scenario-specifier logger)
    (run-scenario scenario-fn scenario-fn-params)))
