(ns buzz.impl.test-commons
  (:require [buzz.core :as b]
            [clojure.core.async :refer [chan put! timeout alts! close! to-chan
                                        #?@(:cljs [<!]
                                            :clj  [<!! go go-loop])]])
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                            [cljs.test :refer [async]])))

#?(:clj
   (defmacro async
     "Hack to make Clojurescript async tests work in Clojure."
     [done & body]
     `(let [~done (fn [])
            ~'<!   <!!]
        ~@(-> body
              first
              rest))))

(defn expected-atom-val-chan
  "Creates chan which returns one value: true if atom change to given value
  within given time or false otherwise."
  ([atom value]
   (expected-atom-val-chan atom value 1000))
  ([atom value timeout-msecs]
   (let [watcher (chan)
         timeout (timeout timeout-msecs)]
     (add-watch atom :watch #(put! watcher %4))
     (put! watcher @atom)
     (go-loop []
       (let [[val port] (alts! [watcher timeout])]
         (if (or (= port timeout)
                 (= val value))
           (do (remove-watch atom :watch)
               (not= port timeout))
           (recur)))))))

(defn throw-ex
  "Throws some exception."
  []
  (throw (#?(:cljs js/Error. :clj Exception.) "This exception is expected")))
