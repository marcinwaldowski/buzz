(ns buzz.core-test
  (:require [buzz.core :as b]
            [clojure.test :refer :all]
            [buzz.impl.test-commons :refer :all]))

(deftest update-fn-returns-only-state
  (let [init-state 0
        update-fn  (fn [state msg]
                     (+ state msg))
        execute-fn nil
        messages   [1 2]]
    (is (= 3
           (test-buzz init-state
                      update-fn
                      execute-fn
                      messages)))))

(deftest update-fn-erorrs-should-not-break-processing
  (let [init-state 0
        update-fn  (fn [state msg]
                     (case msg
                       :throw-ex (throw-ex)
                       (+ state msg)))
        execute-fn nil
        messages   [1 :throw-ex 2]]
    (is (= 3
           (test-buzz init-state
                      update-fn
                      execute-fn
                      messages)))))
