(ns glimpse.impl.default-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [glimpse.impl.default]
            [glimpse.impl.default.layout :as layout]
            [glimpse.impl.enlive-helpers :as eh])
  (:import [glimpse.impl.default PrototypeLoader]))

(defn check-path
  [path]
  (some #{path}
        #{"views/index.html"
          "views/posts.html"
          "views/_post.html"
          "views/_templates/default.html"}))

(defn default-page [path]
  (eh/parse-fragment
   (str "<h1>Uh-oh! No page for this request path was found.</h1>"
        "<p>Try creating a view at \"resources/" path  "\" and reloading this page.</p>")))

(defn default-template [path]
  (eh/parse-document
   (str "<html>"
        "<head><title>Glimpse: 404</title></head>"
        "<body>"
        "<h1>Uh-oh! No template for this request path was found.</h1>"
        "<p>Try creating a template at \"resources/" path "\" and reloading this page.</p>"
        "<hr>"
        "<!-- @container -->"
        "</body>"
        "</html>")))

(defn default-partial [name path]
  (eh/parse-fragment
   (str "<h1>Uh-oh! No partial \"" name "\" for this request path was found.</h1>"
        "<p>Try creating a partial at \"resources/" path "\" and reloading this page.</p>")))

(deftest test-prototype-load-page
  (with-redefs [io/resource check-path
                slurp identity]
    (let [loader (PrototypeLoader. "views")]
      (testing "when the page is found"
        (is (= '("views/index.html") (layout/load-page loader "/")))
        (is (= '("views/posts.html") (layout/load-page loader "/posts"))))
      (testing "when the page is not found"
        (is (= (default-page "views/admin/index.html")
               (layout/load-page loader "/admin/")))
        (is (= (default-page "views/posts/create.html")
               (layout/load-page loader "/posts/create")))))))

(deftest test-prototype-load-template
  (with-redefs [io/resource check-path
                slurp identity]
    (let [loader (PrototypeLoader. "views")]
      (testing "when the template is found"
        (is (= (eh/parse-document "views/_templates/default.html")
               (layout/load-template loader "/" "default"))))
      (testing "when the template is not found"
        (is (= (default-template "views/_templates/other.html")
               (layout/load-template loader "/some_url" "other")))))))

(deftest test-prototype-load-partial
  (with-redefs [io/resource check-path
                slurp identity]
    (let [loader (PrototypeLoader. "views")]
      (testing "when the partial is found"
        (is (= (eh/parse-fragment "views/_post.html")
               (layout/load-partial loader "/" "post"))))
      (testing "when the partial is not found"
        (is (= (default-partial "component" "views/_component.html")
               (layout/load-partial loader "/" "component")))))))
