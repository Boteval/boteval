(ns org.boteval.self-logging
  #_(:use [taoensso.timbre :only (handle-uncaught-jvm-exceptions! info) :rename {info self-log}])
  (:use [taoensso.timbre])
  (:require [org.boteval.self :as self]))

(let [log-file-name "logs/self/log.log"]
  (set-config! {:level :trace :appenders {:spit (spit-appender {:fname log-file-name :append? true})}}) ;https://github.com/ptaoussanis/timbre/issues/228
  (println "self-logging to" log-file-name))


; logging function for the rest of the project to use
(defn self-log [& message]
  (info (apply str message)))

; log starting up when this namespace is first loaded
(self-log "starting up.. self-hash is " self/project-git-hash)


;
; set the Sets JVM-global DefaultUncaughtExceptionHandler to log such exceptions before (thread) termination
; https://github.com/ptaoussanis/timbre/blob/0e094753753bc4b78585c1e6e2e803f9afcfd71b/src/taoensso/timbre.cljx#L628
;
; TODO: need to add console logging as `lein test` would go silent and only the log will get the exception
; TODO: need to be draining the bot logger here if we are the last thread, so that bot activity is never lost
;       so this may not need to be here under self-logging at all.
;
(handle-uncaught-jvm-exceptions!)

