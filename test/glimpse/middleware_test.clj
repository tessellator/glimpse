(ns glimpse.middleware-test
  (:require [glimpse.middleware :refer :all]
            [clojure.test :refer :all]))

(deftest test-lookup-path
  (testing "replaces discovered route-param values if found in the uri"
    (are [expected uri route-params] (= expected (lookup-path {:uri uri :route-params route-params}))
      "/" "/" {}
      "/" "/1234" {:id "1234"}
      "/posts" "/posts/1234" {:id "1234"}
      "/posts/edit" "/posts/1234/edit" {:id "1234"}
      "/posts/1234/edit" "/posts/1234/edit" {:id "2345"}
      "/posts/comments" "/posts/1234/comments/4321" {:post-id "1234" :comment-id "4321"}))

  (testing "ignores the :* wildcard in route-params"
    (is (= "/post/1234" (lookup-path {:uri "/post/1234" :route-params {:* "1234"}})))))
