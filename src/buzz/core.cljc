(ns buzz.core

  "Asynchronous state management based on messages with pure functions."

  (:require [clojure.core.async :as a :refer [#?(:clj go-loop) <! chan]]
            [clojure.pprint :as pp])
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go-loop]])
     :clj  (:import java.io.PrintWriter)))

(defn default-update-ex
  "Default exception handler for update-fn."
  #_(try
      (throw (#?(:cljs js/Error. :clj Exception.) "Oh no"))
      (catch #?(:cljs :default :clj Exception) ex
        (default-update-ex "Msg" ex)))
  [msg ex]
  (let [err-msg (str
                 "Error occured while handling message.\n"
                 "  Event: " (with-out-str (pp/pprint msg) "\n")
                 "  " #?(:cljs ex
                         :clj  (with-out-str
                                 (.printStackTrace ex
                                                   (PrintWriter. *out*)))))]
    #?(:cljs (js/console.error err-msg)
       :clj  (.println *err* err-msg))))

(defn- handle-msg
  "Handles messages."
  [state-atom update-fn update-ex-fn msg]
  (try (let [res (update-fn @state-atom msg)]
         (reset! state-atom res))
       (catch #?(:clj Exception :cljs :default) ex
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