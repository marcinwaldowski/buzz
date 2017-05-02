(defproject buzz "0.1.0-SNAPSHOT"
  :description "Asynchronous state management based on messages with pure functions."
  :url "https://github.com/marcinwaldowski/buzz"
  :license {:name "Apache License Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]]

  :profiles {:dev {:dependencies [[eftest "0.3.1"]]}}
  :plugins [[lein-eftest "0.3.1"]])
