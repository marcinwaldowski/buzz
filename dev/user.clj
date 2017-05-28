(ns user
  (:require
   [figwheel-sidecar.repl-api :as figwheel-sidecar]))


(defn fig-start
  "This starts the figwheel server and watch based auto-compiler."
  []
  (figwheel-sidecar/start-figwheel!))


(defn fig-stop
  "Stop the figwheel server and watch based auto-compiler."
  []
  (figwheel-sidecar/stop-figwheel!))


(defn cljs-repl
  "Launch a ClojureScript REPL that is connected to your build and host environment."
  []
  (figwheel-sidecar/cljs-repl))
