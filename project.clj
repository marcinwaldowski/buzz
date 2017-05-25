(defproject buzz "0.1.0-SNAPSHOT"
  :description "Asynchronous state management based on messages with pure functions."
  :url "https://github.com/marcinwaldowski/buzz"
  :license {:name "Apache License Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.542"]
                 [org.clojure/core.async "0.3.442"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-figwheel "0.5.10"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.7"]
            [lein-npm "0.6.2"]]

  :npm {:devDependencies [[karma "1.7.0"]
                          [karma-cljs-test "0.1.0"]
                          [karma-chrome-launcher "2.1.1"]
                          [karma-firefox-launcher "1.0.1"]]}

  :source-paths ["src"]
  :resource-paths []

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src" "test"]
                :figwheel {:open-urls ["http://localhost:3449/index.html"]}
                :compiler {:main buzz.core
                           :asset-path "js/dev/out"
                           :output-to "resources/public/js/dev/buzz.js"
                           :output-dir "resources/public/js/dev/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "test"
                :source-paths ["src" "test"]
                :compiler {:main buzz.test-run
                           :asset-path "js/test/out"
                           :output-to "resources/public/js/test/test.js"
                           :output-dir "resources/public/js/test/out"
                           :source-map-timestamp true}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/min/buzz.js"
                           :main buzz.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.4"]
                                  [figwheel-sidecar "0.5.10"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [lein-doo "0.1.7"]]

                   :source-paths ["src" "dev"]
                   :resource-paths ["resources"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js"
                                                     :target-path]}})
