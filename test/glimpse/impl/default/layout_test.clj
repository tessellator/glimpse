(ns glimpse.impl.default.layout-test
  (:require [glimpse.impl.default.layout :refer :all]
            [glimpse.impl.enlive-helpers :as eh]
            [net.cgrand.enlive-html :as html]
            [clojure.test :refer :all]))

(deftest test-template-name
  (testing "cases that should successfully parse"
    (are [input]
        (= "my_template" (template-name input))
      "@template my_template"
      " @template my_template "
      "@template my-template"))

  (testing "cases that should not successfully parse"
    (are [input]
        (nil? (template-name input))
      ""
      "template_name"
      "@template"
      "@template "
      "@template my template"
      "@template my/template")))

(deftest test-find-template-name
  (testing "when no template node is found"
    (is (= "default" (find-template-name []))))

  (testing "when a single template node is found"
    (are [partial]
        (= "my_template" (find-template-name (eh/parse-fragment partial)))
      "<!-- @template my-template -->"
      "<div><span><!-- @template my-template --></span></div>"))

  (testing "when more than one template node is found"
    (is (thrown-with-msg? Exception
                          #"More than one template node provided."
                          (find-template-name (eh/parse-fragment "<!-- @template one --><!-- @template two -->"))))))

(deftest test-container-name
  (testing "valid container nodes containing a name yield the name as a keyword"
    (are [input] (= :my-template (container-name input))
      "@container my-template"
      " @container my-template "
      "@container my_template"))

  (testing "valid container nodes without a name yield :default"
    (are [input] (= :default (container-name input))
      "@container"
      " @container "))

  (testing "invalid container nodes yield nil"
    (are [input] (nil? (container-name input))
      ""
      "container"
      "container name"
      "@container some/name")))

(deftest test-create-container-elements
  (let [h "<span><!--@container a--><a href=\"url\">linky</a></span><!--@container b-->"]
    (is (= [{:tag :span
             :attrs nil
             :content [{:tag :glimpse-container
                        :attrs {:name "a"}
                        :content []}
                       {:tag :a
                        :attrs {:href "url"}
                        :content ["linky"]}]}
            {:tag :glimpse-container
             :attrs {:name "b"}
             :content []}]
           (create-container-elements (eh/parse-fragment h))))))

(deftest test-within-name
  (testing "valid within directives"
    (are [input] (= :my-template (within-name input))
      "@within my-template"
      " @within my-template "
      "@within my_template"))

  (testing "invalid within directives"
    (are [input] (nil? (within-name input))
      ""
      "within"
      "@within"
      "within name"
      "name"
      "@within my/name")))

(deftest test-move-within-containers
  (testing "within directives that do not match a container have nodes removed"
    (let [h "<!-- @within my-test --><span>Some nodes</span><!-- /within -->"]
      (is (= [] (move-within-containers (eh/parse-fragment h))))))

  (testing "within directives that match a container have their content move"
    (let [h "<!-- @container my-test --><!-- @within my-test --><span>Some content</span><!-- /within -->"]
      (is (= [{:tag :glimpse-container
               :attrs {:name "my-test"}
               :content [{:tag :span
                          :attrs nil
                          :content ["Some content"]}]}]
             (-> (eh/parse-fragment h) create-container-elements move-within-containers)))))

  (testing "within directives that match more than one container move their content to all"
    (let [h "<!--@container a--><span>deeper <!--@container a--></span><!--@within a --><p>Some content</p><!-- /within -->"]
      (is (= [{:tag :glimpse-container
               :attrs {:name "a"}
               :content [{:tag :p
                          :attrs nil
                          :content ["Some content"]}]}
              {:tag :span
               :attrs nil
               :content ["deeper "
                         {:tag :glimpse-container
                          :attrs {:name "a"}
                          :content [{:tag :p
                                     :attrs nil
                                     :content ["Some content"]}]}]}]
             (-> (eh/parse-fragment h) create-container-elements move-within-containers))))))

(deftest test-include-name
  (testing "valid @include directives"
    (are [input] (= (include-name input) "my_input")
      "@include my_input"
      " @include my_input "
      "@include my-input"))

  (testing "invalid @include directives"
    (are [input] (nil? (include-name input))
      ""
      "include"
      "include my_input"
      "@include "
      "@include my/input")))

(deftest test-include-all
    (let [m {"x" (eh/parse-fragment "<p>X content</p>")
             "y" (eh/parse-fragment "<p>Y content</p><!-- @include z -->")
             "z" (eh/parse-fragment "<p>Z content</p>")
             "rec" (eh/parse-fragment "<p>rec</p><!--@include rec-->")}
          loader (reify ILoader
                   (load-page [this uri]
                     (load-partial this uri uri))

                   (load-template [_ uri name])

                   (load-partial [_ uri name]
                     (get m name)))]
      (testing "include directives are replaced by corresponding content"
        (is (= (apply str
                      (-> "<span>a</span><!-- @include x --><span>b</span>"
                          eh/parse-fragment
                          (include-all loader "uri")
                          html/emit*))
               "<span>a</span><p>X content</p><span>b</span>")))

      (testing "nested include directives are replaced by corresponding content"
        (is (= (apply str
                      (-> "<span>a</span><!-- @include x --><span>b<!--@include y--></span>"
                          eh/parse-fragment
                          (include-all loader "uri")
                          html/emit*))
               "<span>a</span><p>X content</p><span>b<p>Y content</p><p>Z content</p></span>")))

      (testing "nesting is stopped after a max depth is reached"
        (is (= (apply str
                      (-> "<span>a</span><!--@include rec--><span>b</span>"
                          eh/parse-fragment
                          (include-all loader "uri" 3)
                          html/emit*))
               "<span>a</span><p>rec</p><p>rec</p><span>b</span>")))))

(deftest test-construct-view
  (let [m {"default" "<html><head><title>Glimpse</title></head><body><!--@container--><h1>Footer</h1><!-- @container footer --></body></html>"
           "index" "<p>I am some content.</p><!--@within footer--><p>I am in the footer!</p><!--/within--><!--@include reusable-->"
           "reusable" "<p>I am reusable!</p>"}
        loader (reify ILoader
                 (load-page [_ uri] (eh/parse-fragment (get m uri)))
                 (load-template [_ _ name] (eh/parse-document (get m name)))
                 (load-partial [_ _ name] (eh/parse-fragment (get m name))))]
    (is (= "<html><head><title>Glimpse</title></head><body><p>I am some content.</p><p>I am reusable!</p><h1>Footer</h1><p>I am in the footer!</p></body></html>"
           (apply str (html/emit* (construct-view "index" loader)))))))
