(ns buzz.core-test
  (:require [buzz.core :as b]
            [clojure.test :refer [deftest is testing]]
            [buzz.impl.test-commons :refer [expected-atom-val-chan throw-ex]])
  #?(:clj (:require [buzz.impl.test-commons :refer [async]]))
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]
                            [cljs.test :refer [async]])))

(deftest update-fn-returns-only-state
  (let [state      (atom 0)
        update-fn  (fn [state msg]
                     (+ state msg))
        execute-fn nil
        buzz       (b/buzz state update-fn execute-fn :update-ex-fn (fn [_ _]))]
    (async done
      (go
          (b/put! buzz 1)
          (b/put! buzz 2)
          (is (<! (expected-atom-val-chan state 3)))
          (b/close! buzz)
          (done)))))

(deftest update-fn-erorrs-should-not-break-processing
  (let [state      (atom 0)
        update-fn  (fn [state msg]
                     (case msg
                       :throw-ex (throw-ex)
                       (+ state msg)))
        execute-fn nil
        buzz       (b/buzz state update-fn execute-fn :update-ex-fn (fn [_ _]))]
    (async done
      (go
        (b/put! buzz 1)
        (b/put! buzz :throw-ex)
        (b/put! buzz 2)
        (is (<! (expected-atom-val-chan state 3)))
        (b/close! buzz)
        (done)))))
