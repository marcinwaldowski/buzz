(ns buzz.core

  "Asynchronous state management based on messages."

  (:require                 [clojure.core.async     :as async]
                            [clojure.pprint         :as pprint])
  #?(:cljs (:require-macros [cljs.core.async.macros :as async])
     :clj  (:import         [java.io PrintWriter])))


(defn ^:private default-handle-ex
  "Default exception handler for handle-fn which prints
  exception to stderr or browser console."
  [msg ex]
  (let [err-msg (str
                 "Error occured while handling message.\n"
                 "  Message: " (with-out-str (pprint/pprint msg) "\n")
                 "  " #?(:cljs ex
                         :clj  (with-out-str
                                 (.printStackTrace ex
                                                   (PrintWriter. *out*)))))]
    #?(:cljs (js/console.error err-msg)
       :clj  (.println *err* err-msg))))


(defn ^:private handle-msg
  "Handles messages."
  [state-atom handle-fn handle-ex-fn execute-fn mixer msg]
  (try (let [[new-state cmd] (handle-fn @state-atom msg)]
         (reset! state-atom new-state)
         (if-not (nil? cmd)
           (async/admix mixer (execute-fn cmd))))
       (catch #?(:clj Exception :cljs :default) ex
         (handle-ex-fn msg ex))))


(defn ^:private start-message-processing
  "Starts message processing."
  [state-atom handle-fn handle-ex-fn execute-fn mixer msg-chan]
  (let [handle-msg (partial handle-msg state-atom handle-fn handle-ex-fn execute-fn mixer)]
    (async/go-loop []
      (if-let [msg (async/<! msg-chan)]
        (do (handle-msg msg)
            (recur))))))


(def ^:private *default-opts
  "Default options for buzz."
  {:handle-ex-fn default-handle-ex})


(defn buzz
  "Creates buzz which manages given state-atom based on messages.

  Arguments:

  1. state-atom - Atom managed by this buzz instance.

  2. handle-fn - Handle function.

     This function have to:
     - obligatorily produce new value for managed atom based on current value
       and passed message,
     - optionally produce command to execute.

     Function must accept two arguments:
     [current-state-val message]
     where:
       - current-state-val is the current value of managed atom,
       - message is the message to handle,
     and returns one of following:
     - [new-state-val]
         vector with new state value,
     - [new-state-val cmd]
         vector with new state value and command to execute, the nil cmd
         is treated like no command

  3. execute-fn - Execute function.

     This function have to produce core.async channel which will return
     new message(s). Messages returned by this channel are passed later
     to handle-fn function.

     Function must accept one argument:
     [cmd]
     where cmd is a command returned from handle-fn
     and returns core.async channel which returns messages.

  4. opts - Optional options map.

     This map contains options which override default options.

     This map may contain following keys:
     - :handle-ex-fn
          Value for this key must be a function which handles exceptions
          thrown by handle-fn. Default value for this key is default-handle-ex.
          This function accepts two arguments:
          [message exception]
          where:
            - message is a message for which exception occured,
            - exception is a exception throwed by handle-fn function."

  ([state-atom handle-fn execute-fn]
   (buzz state-atom handle-fn execute-fn {}))
  ([state-atom handle-fn execute-fn opts]
   (let [{:keys [handle-ex-fn]} (merge *default-opts opts)
         msg-out-chan           (async/chan)
         mixer                  (async/mix msg-out-chan)
         msg-in-chan            (async/chan)]
     (async/admix mixer msg-in-chan)
     (start-message-processing state-atom handle-fn handle-ex-fn execute-fn mixer msg-out-chan)
     {:msg-in-chan msg-in-chan})))


(defn put!
  "Puts message into buzz."
  [buzz message]
  (async/put! (:msg-in-chan buzz) message))


(defn close!
  "Closes buzz."
  [buzz]
  (async/close! (:msg-in-chan buzz)))
