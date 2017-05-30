(ns buzz.core

  "Asynchronous state management based on messages with pure functions."

  (:require                 [clojure.core.async     :as async]
                            [clojure.pprint         :as pprint])
  #?(:cljs (:require-macros [cljs.core.async.macros :as async])
     :clj  (:import         [java.io PrintWriter])))


(defn default-update-ex
  "Default exception handler for update-fn which prints
  exception to stderr or browser console."
  #_(try
      (throw (#?(:cljs js/Error. :clj Exception.) "Oh no"))
      (catch #?(:cljs :default :clj Exception) ex
          (default-update-ex "Msg" ex)))
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
  [state-atom update-fn update-ex-fn execute-fn mixer msg]
  (try (let [update-res (update-fn @state-atom msg)]
         (if (sequential? update-res)
           (let [[new-state cmd] update-res]
             (do (reset! state-atom new-state)
                 (if-not (nil? cmd)
                   (async/admix mixer (execute-fn cmd)))))
           (reset! state-atom update-res)))
       (catch #?(:clj Exception :cljs :default) ex
         (update-ex-fn msg ex))))


(defn ^:private start-message-processing
  "Starts message processing."
  [state-atom update-fn update-ex-fn execute-fn mixer msg-chan]
  (let [handle-msg (partial handle-msg state-atom update-fn update-ex-fn execute-fn mixer)]
    (async/go-loop []
      (if-let [msg (async/<! msg-chan)]
        (do (handle-msg msg)
            (recur))))))


(def ^:private *buzz-opts-defaults
  "Default options for buzz."
  {:update-ex-fn default-update-ex})


(defn buzz
  "Creates buzz which manages given state-atom based on messages.

  Arguments:

  1. state-atom - Atom managed by this buzz instance. It is required
     for the atom value to be a data structure for which sequential?
     returns false, which implies that it must not be vector or list.
     In practice atom value is always a map.

  2. update-fn - Update function.

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
     - new-state-val
         new state value, which could be also current-state-val,
     - [new-state-val]
         new state value, which could be also current-state-val,
     - [new-state-val nil]
         new state value, which could be also current-state-val,
     - [new-state-val cmd]
         new state and command to execute.

     It is required for the returned new state value to be a data structure
     or value for which sequential? returns false, which implies that it
     must not be vector or list. In practice it is always a map.

  3. execute-fn - Execute function.

     This function have to produce core.async channel which will return
     new message(s). Messages returned by this channel will be passed later
     to update-fn function.

     Function must accept one argument:
     [cmd]
     where cmd is a command returned from update-fn
     and returns core.async channel which will return messages.

  4. opts - Optional additional options as map.

     This map contains options which overrides default options.

     This map may contain following keys:
     - :update-ex-fn
          Value for this key must be a function which handles exceptions
          thrown by update-fn. Default value for this key is default-update-ex.
          This function accepts two arguments:
          [message exception]
          where:
            - message is a message for which exception occured,
            - exception is a exception throwed by update-fn function."

  ([state-atom update-fn execute-fn]
   (buzz state-atom update-fn execute-fn {}))
  ([state-atom update-fn execute-fn opts]
   (let [{:keys [update-ex-fn]} (merge *buzz-opts-defaults opts)
         msg-out-chan           (async/chan)
         mixer                  (async/mix msg-out-chan)
         msg-in-chan            (async/chan)]
     (async/admix mixer msg-in-chan)
     (start-message-processing state-atom update-fn update-ex-fn execute-fn mixer msg-out-chan)
     {:msg-in-chan msg-in-chan})))


(defn put!
  "Puts message into buzz."
  [buzz message]
  (async/put! (:msg-in-chan buzz) message))


(defn close!
  "Closes buzz."
  [buzz]
  (async/close! (:msg-in-chan buzz)))
