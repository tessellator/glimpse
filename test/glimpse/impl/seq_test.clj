(ns glimpse.impl.seq-test
  (:require [glimpse.impl.seq :refer :all]
            [clojure.test :refer :all]))

(deftest test-resize
  (is (= [] (resize 0 [1])))
  (is (= [1] (resize 1 [1])))
  (is (= [1 nil] (resize 2 [1])))
  (is (= [1 2] (resize 2 [1] 2))))
