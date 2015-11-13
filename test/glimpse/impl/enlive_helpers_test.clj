(ns glimpse.impl.enlive-helpers-test
  (:require [glimpse.impl.enlive-helpers :refer :all]
            [clojure.test :refer :all]
            [net.cgrand.enlive-html :as html]))

(deftest test-parse-fragment
  (let [h "<span><a href=\"some_url\">some link</a></span><!-- my comment -->"]
    (is (= [{:tag :span,
             :attrs nil,
             :content [{:tag :a,
                        :attrs {:href "some_url"}
                        :content ["some link"]}]}
            {:type :comment, :data " my comment "}]
           (parse-fragment h)))))

(deftest test-comment-pred
  (let [html-str "<span>
                    <a href=\"some_url\">some link</a>
                    <!-- some comment -->
                  </span>
                  <!-- @test -->"]
    (is (= [{:type :comment :data " some comment "}
            {:type :comment :data " @test "}]
           (html/select (parse-fragment html-str) [(comment-pred identity)]))
        "did not find all comment nodes")

    (is (= [{:type :comment :data " @test "}]
           (html/select (parse-fragment html-str)
                        [(comment-pred #(= % " @test "))]))
        "did not filter correct nodes")))

(deftest test-remove-nodes
  (let [h "<span><a href=\"some_url\">some link</a></span><!-- my comment -->"]
    (is (= [{:tag :span,
             :attrs nil,
             :content [{:tag :a,
                        :attrs {:href "some_url"}
                        :content ["some link"]}]}]
           (remove-nodes (parse-fragment h) [html/comment-node])))))

(deftest test-replace-with-children
  (let [h "<span><a href=\"some_url\">some link</a><!--c--></span><!-- my comment -->"]
    (is (= [{:tag :a
             :attrs {:href "some_url"}
             :content ["some link"]}
            {:type :comment
             :data "c"}
            {:type :comment
             :data " my comment "}]
           (replace-with-children (parse-fragment h) [:span])))))
