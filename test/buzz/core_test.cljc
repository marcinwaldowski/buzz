(ns buzz.core-test
  (:require                 [clojure.test           :refer [deftest is testing]]
                            [clojure.core.async     :as async]
                            [buzz.core              :as buzz]
                            [buzz.impl.test-commons :as test-commons])
  #?(:clj  (:require        [buzz.impl.test-commons :as test]))
  #?(:cljs (:require-macros [cljs.test              :as test]
                            [cljs.core.async.macros :as async])))


(def ^:private *test-opts
  {:update-ex-fn (fn [_ _])})


(defn ^:private test-update-fn
  [state [msg-type & vals]]
  (case msg-type
    :add-one      (inc state)
    :add-v0       (+ state (first vals))
    :add-v1       [(+ state (first vals))]
    :add-v2       [(+ state (first vals)) nil]
    :throw-ex     (test-commons/throw-ex)
    :add-exp      (let [[b n] vals]
                    [state [:exp :add-v0 b n]])
    :add-throw-ex [state [:throw-ex]]))


(defn ^:private test-execute-fn
  [[cmd-type & vals]]
  (case cmd-type
    :exp (async/go
           (let [[msg-type b n] vals
                 res            (reduce * (repeat n b))]
             [msg-type res]))
    :throw-ex (test-commons/throw-ex)))


(deftest update-fn-returns-only-state
  (let [state      (atom 0)
        buzz       (buzz/buzz state test-update-fn test-execute-fn)]
    (test/async done
      (async/go
        (buzz/put! buzz [:add-one])
        (buzz/put! buzz [:add-v0 2])
        (buzz/put! buzz [:add-v1 4])
        (buzz/put! buzz [:add-v2 8])
        (is (<! (test-commons/expected-atom-val-chan state 15)))
        (buzz/close! buzz)
        (done)))))


(deftest update-fn-errors-should-not-break-processing
  (let [state      (atom 0)
        buzz       (buzz/buzz state test-update-fn test-execute-fn *test-opts)]
    (test/async done
      (async/go
        (buzz/put! buzz [:add-v0 1])
        (buzz/put! buzz [:throw-ex])
        (buzz/put! buzz [:add-v1 2])
        (is (<! (test-commons/expected-atom-val-chan state 3)))
        (buzz/close! buzz)
        (done)))))


(deftest update-fn-returns-state-and-command
  (let [state      (atom 0)
        buzz       (buzz/buzz state test-update-fn test-execute-fn)]
    (test/async done
      (async/go
        (buzz/put! buzz [:add-exp 2 8])
        (is (<! (test-commons/expected-atom-val-chan state 256)))
        (buzz/close! buzz)
        (done)))))

(deftest execute-fn-errors-should-not-break-processing
  (let [state      (atom 0)
        buzz       (buzz/buzz state test-update-fn test-execute-fn *test-opts)]
    (test/async done
      (async/go
        (buzz/put! buzz [:add-v0 1])
        (buzz/put! buzz [:add-throw-ex])
        (buzz/put! buzz [:add-v1 2])
        (is (<! (test-commons/expected-atom-val-chan state 3)))
        (buzz/close! buzz)
        (done)))))
