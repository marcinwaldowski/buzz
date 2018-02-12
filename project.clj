(defproject buzz "0.2.0"
  :description "Asynchronous state management based on messages."
  :url "https://github.com/marcinwaldowski/buzz"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.4.474"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-figwheel "0.5.14"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.8"]
            [lein-npm "0.6.2"]
            [lein-codox "0.10.3"]]

  :npm {:devDependencies [[karma "1.7.0"]
                          [karma-cljs-test "0.1.0"]
                          [karma-chrome-launcher "2.1.1"]
                          [karma-firefox-launcher "1.0.1"]]}

  :source-paths ["src"]
  :resource-paths []

  :doo {:alias {:browsers [:chrome :firefox]}}
  :codox {:namespaces [buzz.core]}

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

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.9"]
                                  [figwheel-sidecar "0.5.14"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [lein-doo "0.1.8"]]

                   :source-paths ["dev"]
                   :resource-paths ["resources"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js"
                                                     :target-path]}})
