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
                 "  Event: " (with-out-str (pprint/pprint msg) "\n")
                 "  " #?(:cljs ex
                         :clj  (with-out-str
                                 (.printStackTrace ex
                                                   (PrintWriter. *out*)))))]
    #?(:cljs (js/console.error err-msg)
       :clj  (.println *err* err-msg))))


(defn ^:private handle-msg
  "Handles messages."
  [state-atom update-fn update-ex-fn msg]
  (try (let [res (update-fn @state-atom msg)]
         (reset! state-atom res))
       (catch #?(:clj Exception :cljs :default) ex
         (update-ex-fn msg ex))))


(defn ^:private start-message-processing
  "Starts message processing."
  [state-atom update-fn update-ex-fn msg-chan]
  (let [handle-msg (partial handle-msg state-atom update-fn update-ex-fn)]
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

  1. state-atom - Atom managed by this buzz instance.

  2. update-fn - Update function.

     This function have to:
     - obligatorily produce new value for managed atom based on current value
       and passed message,
     - optionally produce commands to execute.

     Function must accept two arguments:
     [current-state-val message]
     where:
       - current-state-val is the current value of managed atom,
       - message is the message to handle,
     and returns one of following:
     - new-state-val
         new state value, which could be also current-state-val,
     - [new-state-val cmd]
         new state and one command to execute,
     - [new-state-val [cmd1 cmd2 ...  cmdn]]
         new state and sequence of commands to execute.

     It is required that returned new state and command(s) be a data structure
     or value for which sequential? returns false, which implies that they
     cannot be vectors and lists. In practice they are almost always maps.

  3. execute-fn - Execute function.

     This function have to produce core.async channel which will return new
     event(s). Events returned by this channel will be passed to update-fn
     function.

     Function must accept one argument:
     [cmd]
     where cmd is a command returned from update-fn
     and returns core.async channel which will return events.

     It is required that events returned from channel be a data structure
     or value for which sequential? returns false, which implies that they
     cannot be vectors or lists. In practice they are almost always maps.

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
         msg-chan               (async/chan)]
     (start-message-processing state-atom update-fn update-ex-fn msg-chan)
     {:msg-chan msg-chan})))


(defn put!
  "Puts message into buzz. Message must be a data structure or value for which
  sequential? returns false, which implies that they cannot be vectors or lists.
  In practice message is almost always map."
  [buzz message]
  (async/put! (:msg-chan buzz) message))


(defn close!
  "Closes buzz."
  [buzz]
  (async/close! (:msg-chan buzz)))
