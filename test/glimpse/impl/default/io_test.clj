(ns glimpse.impl.default.io-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [glimpse.impl.default.io :refer :all]))

(defn check-path
  "Checks against a list of sample filenames for a given project."
  [path]
  (some #{path}
        #{"views/_templates/default.html"
          "views/index.html"
          "views/post.html"
          "views/_post.html"
          "views/admin/_post.html"
          "views/admin/index.html"
          "views/admin/_templates/default.html"
          "views/admin/other/index.html"
          "views/posts/create.html"}))

(deftest test-get-path
  (are [expected actual] (= expected actual)
    "asdf/test.txt" (get-path "asdf" "test.txt")
    "" (get-path "")))

(deftest test-page-resource
  (with-redefs [io/resource check-path]
    (testing "with an implicit index"
      (is (= (page-resource "views" "/") "views/index.html"))
      (are [page] (= page "views/admin/index.html")
        (page-resource "views" "/admin/")
        (page-resource "views" "/admin")))

    (testing "with an explicit lookup name"
      (is (= (page-resource "views" "/post") "views/post.html")))

    (testing "returns nil if page is not found"
      (is (nil? (page-resource "views" "/admin/post")))
      (is (nil? (page-resource "views" "/posts/"))))))

(deftest test-template-resource
  (with-redefs [io/resource check-path]
    (testing "when templates are found"
      (are [expected uri name] (= (template-resource "views" uri name) expected)
        "views/_templates/default.html" "/" "default"
        "views/_templates/default.html" "/posts/create" "default"
        "views/admin/_templates/default.html" "/admin/" "default"
        "views/admin/_templates/default.html" "/admin/create_user" "default"))

    (testing "when templates are not found"
      (is (nil? (template-resource "views" "/" "other"))))))

(deftest test-partial-resource
  (with-redefs [io/resource check-path]
    (testing "when the partial is found"
      (are [expected uri name] (= (partial-resource "views" uri name) expected)
        "views/_post.html" "/" "post"
        "views/_post.html" "/posts/create" "post"
        "views/admin/_post.html" "/admin/" "post"
        "views/admin/_post.html" "/admin/other/" "post"))

    (testing "when the partial is not found"
      (nil? (partial-resource "views" "/" "some_component")))))
