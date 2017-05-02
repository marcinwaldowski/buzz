(ns buzz.impl.test-commons
  (:require [buzz.core :as b]
            [clojure.core.async :as a :refer [chan put! go-loop
                                              timeout alts! close! <!!]]))

(defn- watching-chan
  "Returns chan which returns the value of atom right before its value was hanged to :end
  or returns :timeout if atom wasn't changed to :end in given timeout miliseconds."
  [atom timeout-msecs]
  (let [ch (chan)]
    (add-watch atom :watch #(put! ch %4))
    (go-loop [prev-val nil]
      (let [val (first (alts! [ch (timeout timeout-msecs)]))]
        (if (or (nil? val) (= val :end))
          (do (remove-watch atom :watch)
              (close! ch)
              (if val
                prev-val
                :timeout))
          (recur val))))))

(defn test-buzz
  "Creates and test buzz with given parameters. Return final state of atom managed by buzz."
  [initial-state update-fn execute-fn messages & opts]
  (let [{:keys [timeout-msecs update-ex-fn]
         :or   {timeout-msecs 2000
                update-ex-fn (fn [_ _])}} opts
        state     (atom initial-state)
        ch        (watching-chan state timeout-msecs)
        update-fn (fn [state msg]
                    (if (= msg :end)
                      :end
                      (update-fn state msg)))
        buzz      (b/buzz state update-fn execute-fn :update-ex-fn update-ex-fn)]
    (doseq [msg messages]
      (b/put! buzz msg))
    (b/put! buzz :end)
    (let [result  (<!! ch)]
      (b/close! buzz)
      result)))

(defn throw-ex
  "Throws some exception."
  []
  (throw (IllegalArgumentException. "This exception is expected")))
