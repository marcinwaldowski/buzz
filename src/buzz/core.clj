(ns buzz.core

  "Asynchronous state management based on messages with pure functions."

  (:require [clojure.core.async :as a :refer [go-loop <! chan]]
            [clojure.pprint :as pp])
  (:import java.io.PrintWriter))

(defn default-update-ex
  "Default exception handler for update-fn."
  [msg ex]
  (let [err-msg (str
                 "Error occured while handling message.\n"
                 "  Event: " (with-out-str (pp/pprint msg) "\n")
                 "  Error: " (with-out-str (.printStackTrace ex (PrintWriter. *out*))))]
    (binding [*out* *err*]
      (.println *err* err-msg))))

(defn- handle-msg
  "Handles messages."
  [state-atom update-fn update-ex-fn msg]
  (try (let [res (update-fn @state-atom msg)]
         (reset! state-atom res))
       (catch Exception ex
         (update-ex-fn msg ex))))

(defn- start-message-processing
  "Starts message processing."
  [state-atom update-fn update-ex-fn msg-chan]
  (let [handle-msg (partial handle-msg state-atom update-fn update-ex-fn)]
    (go-loop []
      (if-let [msg (<! msg-chan)]
        (do (handle-msg msg)
            (recur))))))

(defn buzz
  "Creates buzz which manages given state-atom based on messages."
  [state-atom update-fn execute-fn & opts]
  (let [{:keys [update-ex-fn]
         :or   {update-ex-fn default-update-ex}} opts
        msg-chan (chan)]
    (start-message-processing state-atom update-fn update-ex-fn msg-chan)
    {:msg-chan msg-chan}))

(defn put!
  "Puts message into buzz."
  [buzz msg]
  (a/put! (:msg-chan buzz) msg))

(defn close!
  "Closes buzz."
  [buzz]
  (a/close! (:msg-chan buzz)))
