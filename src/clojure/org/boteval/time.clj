(ns org.boteval.time
  (:require [clj-time.core :as time])
  (:require [clj-time.coerce :as time-convert]))

(defn now []
  "get the current time. this only gives millisecond precision, to get micro or nano precision.
   todo: need to use Java 8's new time object http://stackoverflow.com/a/33472641/1509695 rather than
   clojure's joda-time wrappar"
  (time/now))

(defn sql-time []
  "dbms friendly current time"
  (time-convert/to-sql-time (now)))

