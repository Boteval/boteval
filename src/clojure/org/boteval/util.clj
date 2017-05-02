(ns org.boteval.util
  " various utility functions ")

(defn ^:private call [this & that]
  " calls the function who's symbol name is `this`, with arguments `that`.
    this allows calling a function by a string representing its name.
    credit: http://stackoverflow.com/a/20277152/1509695 "
  (apply (resolve (symbol this)) that))

(defn clean-fn-name [fn-name]
  " returns a fully qualified function name, from a runtime representation of one with dollars and stuff
    i.e. returns package.package.name from something like package.package$name@30400 "
  (clojure.string/replace (clojure.string/replace (str fn-name) #"@(.*)" "") "$" "/"))

