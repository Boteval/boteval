(defproject boteval "0.1.0-SNAPSHOT"
  :description "the boteval framework"
  :url "https://github.com/Boteval"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"

  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options     ["-target" "1.8" "-source" "1.8"]

  :test-paths ["test" "src/clojure"] ; for picking up unit tests from regular source files not only the tests directory

  :test-selectors {:default (complement :unit) ; https://github.com/technomancy/leiningen/blob/983847276d12fcdac7a5b5eabbd5dfcb926087d7/src/leiningen/test.clj#L172
                   :unit :unit-tests ; real tests of the library
                   :self :self ; sample scenarios and evaluators
                   :all (constantly true)}

  :dependencies [[org.clojure/clojure "1.8.0"]

                 [com.taoensso/timbre "4.10.0"]    ; clojure logging

                 [org.slf4j/slf4j-simple "1.7.25"] ; for the draining of sl4fj-reliant libraries used here

                 [clj-time "0.13.0"]               ; https://github.com/clj-time/clj-time

                 ;; sql wrangling stack
                 [honeysql "0.8.2"]                     ; sort of a query builder
                 [mysql/mysql-connector-java "5.1.41"]  ; mysql jdbc driver
                 [org.clojure/java.jdbc "0.7.0-alpha3"] ; clojure jdbc, needed for the rest of them libraries
                 [hikari-cp "1.7.5"]                    ; jdbc connection pooling, if we really need it (https://github.com/tomekw/hikari-cp)

                 [cheshire "5.7.1"]]                    ; for working with json

                 ; [io.aviso/pretty "0.1.33"] ; pretty exceptions in leinigen, currently trying out ultra instead

  :plugins [;[io.aviso/pretty "0.1.33"] we now use ultra instead
            [venantius/ultra "0.5.1"]
            [lein-codox "0.10.3"]
            [lein-auto "0.1.3"]   ; provides the auto lein command for watching source changes
            [test2junit "1.2.6"]] ; push test results into junit xml format (or sucky html reports) https://github.com/ruedigergad/test2junit

  ; this doesn't work yet ― see https://github.com/weavejester/lein-auto/issues/6
  ; :auto {:default {:paths (:source-paths :java-source-paths :test-paths :java-source-paths "my path")}} ; https://github.com/weavejester/lein-auto#usage

  :codox {:metadata {:doc/format :markdown}} ; treat docstrings as codox extended markdown (https://github.com/weavejester/codox/blob/master/example/src/clojure/codox/markdown.clj)

  :aot [org.boteval.engine.api org.boteval.engine.evaluator-api]
  :profiles {:java-tests-compile
    {:java-source-paths ["src/java-test"]}}
  :aliases {
    "java-tests" ["do" "compile," "with-profile" "java-tests-compile" "javac," "run" "-m" "org.boteval.java.samples.ScenarioRunner"]
    "all-tests" ["do" "test," "java-tests"]
  }
  :main org.boteval.main)

;; tips towards the interweaving of dependant java and clojure compilation phases:
;; https://github.com/technomancy/leiningen/issues/847#issuecomment-289943710
