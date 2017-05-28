(ns buzz.core-test
  (:require                 [clojure.test           :refer [deftest is testing]]
                            [clojure.core.async     :as async]
                            [buzz.core              :as buzz]
                            [buzz.impl.test-commons :as test-commons])
  #?(:clj  (:require        [buzz.impl.test-commons :as test]))
  #?(:cljs (:require-macros [cljs.test              :as test]
                            [cljs.core.async.macros :as async])))


(deftest update-fn-returns-only-state
  (let [state      (atom 0)
        update-fn  (fn [state msg]
                     (+ state msg))
        execute-fn nil
        buzz       (buzz/buzz state update-fn execute-fn {:update-ex-fn (fn [_ _])})]
    (test/async done
      (async/go
        (buzz/put! buzz 1)
        (buzz/put! buzz 2)
        (is (<! (test-commons/expected-atom-val-chan state 3)))
        (buzz/close! buzz)
        (done)))))


(deftest update-fn-errors-should-not-break-processing
  (let [state      (atom 0)
        update-fn  (fn [state msg]
                     (case msg
                       :throw-ex (test-commons/throw-ex)
                       (+ state msg)))
        execute-fn nil
        buzz       (buzz/buzz state update-fn execute-fn {:update-ex-fn (fn [_ _])})]
    (test/async done
      (async/go
        (buzz/put! buzz 1)
        (buzz/put! buzz :throw-ex)
        (buzz/put! buzz 2)
        (is (<! (test-commons/expected-atom-val-chan state 3)))
        (buzz/close! buzz)
        (done)))))
