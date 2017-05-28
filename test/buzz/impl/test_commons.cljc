(ns buzz.impl.test-commons
  (:require                 [buzz.core              :as buzz]
                            [clojure.core.async     :as async])
  #?(:cljs (:require-macros [cljs.core.async.macros :as async])))


#?(:clj
   (defmacro async
     "Hack to make Clojurescript async tests work in Clojure."
     [done & body]
     `(let [~done (fn [])
            ~'<!   async/<!!]
        ~@(-> body
              first
              rest))))


(defn expected-atom-val-chan
  "Creates chan which returns one value: true if atom change to given value
  within given time or false otherwise."
  ([atom value]
   (expected-atom-val-chan atom value 1000))
  ([atom value timeout-msecs]
   (let [watcher (async/chan)
         timeout (async/timeout timeout-msecs)]
     (add-watch atom :watch #(async/put! watcher %4))
     (async/put! watcher @atom)
     (async/go-loop []
       (let [[val port] (async/alts! [watcher timeout])]
         (if (or (= port timeout)
                 (= val value))
           (do (remove-watch atom :watch)
               (not= port timeout))
           (recur)))))))


(defn throw-ex
  "Throws some exception."
  []
  (throw (#?(:cljs js/Error. :clj Exception.) "This exception is expected")))
