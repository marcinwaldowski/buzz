(ns buzz.test-run
  (:require-macros [doo.runner])
  (:require        [doo.runner]
                   [buzz.core-test]))


(doo.runner/doo-tests 'buzz.core-test)
