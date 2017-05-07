(defproject buzz "0.1.0-SNAPSHOT"
  :description "Asynchronous state management based on messages with pure functions."
  :url "https://github.com/marcinwaldowski/buzz"
  :license {:name "Apache License Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.521"]
                 [org.clojure/core.async "0.3.442"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-figwheel "0.5.10"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]
            [lein-eftest "0.3.1"]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel {:open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main buzz.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/buzz.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/buzz.js"
                           :main buzz.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.2"]
                                  [figwheel-sidecar "0.5.10"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [eftest "0.3.1"]]

                   :source-paths ["src" "dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
