(ns buzz.test-run
  (:require [doo.runner :refer-macros [doo-tests]]
            [buzz.core-test]))

(doo-tests 'buzz.core-test)
