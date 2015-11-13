(ns glimpse.impl.default.bindings-test
  (:require [clojure.test :refer :all]
            [glimpse.impl.default.bindings :refer :all]
            [glimpse.impl.enlive-helpers :as eh]
            [net.cgrand.enlive-html :as html]))

(defn parse-el [s] (first (eh/parse-fragment s)))
(defn render [nodes] (apply str (html/emit* nodes)))

(deftest test-bind-prop-map
  (let [a (parse-el "<a class=\"my_class\" href=\"#\">Default content</a>")]
    (are [expected el b] (= expected (render (bind-prop-map el b)))
      "<a class=\"my_class\" href=\"#\">my_content</a>" a {:content "my_content"}
      "<a class=\"my_class\" href=\"my_link\">Default content</a>" a {:href "my_link"}
      "<a class=\"my_class\" href=\"#\" new-attr=\"new-value\">Default content</a>" a {:new-attr "new-value"}
      "<a class=\"my_class\" href=\"#\">3</a>" a {:content 3}
      "<a class=\"my_class\" href=\"#\"></a>" a {:content nil}
      "<a class=\"my_class\">Default content</a>" a {:href nil}
      "<a class=\"my_class\"></a>" a {:href nil :content nil}
      "" a {:class nil :href nil :content nil}
      "" (parse-el "<span>Default content</span>") {:content nil}
      "<a class=\"my_class\" href=\"#\">Default content</a>" a {:new-attr nil})))

(deftest test-bind-prop
  (with-redefs [bind-prop-map (fn [node m] [:fn node m])]
    (is (= nil (bind-prop :node nil)))
    (is (= [:fn :node {:key :value}] (bind-prop :node {:key :value})))
    (is (= [:fn :node {:content :value}] (bind-prop :node :value)))))

(deftest test-bind-scope
  (testing "without nested scopes"
    (let [post (eh/parse-fragment (str "<div data-scope=\"post\">"
                                       "<a data-prop=\"title\" href=\"#\">Title</a>"
                                       "<p data-prop=\"body\">Default content</p>"
                                       "</div>"))]
      (are [expected bindings] (= expected (render (bind-scope post :post bindings)))
        "" nil

        (str "<div data-scope=\"post\">"
             "<a data-prop=\"title\" href=\"my_link\">My Title</a>"
             "<p data-prop=\"body\">My content</p>"
             "</div>")
        {:title {:href "my_link" :content "My Title"} :body "My content"}

        (str "<div data-scope=\"post\">"
             "<a data-prop=\"title\" href=\"post_link_1\">Title 1</a>"
             "<p data-prop=\"body\">First content</p>"
             "</div>"
             "<div data-scope=\"post\">"
             "<a data-prop=\"title\" href=\"post_link_2\">Title 2</a>"
             "<p data-prop=\"body\">Second content</p>"
             "</div>")
        [{:title {:href "post_link_1" :content "Title 1"} :body "First content"}
         {:title {:href "post_link_2" :content "Title 2"} :body "Second content"}])))

  (testing "with nested scopes"
    (let [post (eh/parse-fragment (str "<div data-scope=\"post\">"
                                       "<a data-prop=\"title\" href=\"#\">Title</a>"
                                       "<p data-prop=\"body\">Default content</p>"
                                       "<div data-scope=\"comment\">"
                                       "<p>Author:<span data-prop=\"author\">Some author</span></p>"
                                       "<p data-prop=\"body\">Comment body</p>"
                                       "</div>"
                                       "</div>"))]
      (testing "with a single post"
       (are [expected bindings] (= expected (render (bind-scope post :post bindings)))
         (str "<div data-scope=\"post\">"
              "<a data-prop=\"title\" href=\"post_link\">My Title</a>"
              "<p data-prop=\"body\">My content</p>"
              "</div>")
         {:title {:href "post_link" :content "My Title"} :body "My content" :comment nil}

         (str "<div data-scope=\"post\">"
              "<a data-prop=\"title\" href=\"post_link\">My Title</a>"
              "<p data-prop=\"body\">My content</p>"
              "</div>")
         {:title {:href "post_link" :content "My Title"} :body "My content" :comment []}

         (str "<div data-scope=\"post\">"
              "<a data-prop=\"title\" href=\"post_link\">My Title</a>"
              "<p data-prop=\"body\">My content</p>"
              "<div data-scope=\"comment\">"
              "<p>Author:<span data-prop=\"author\">President Lincoln</span></p>"
              "<p data-prop=\"body\">Fourscore and seven years ago...</p>"
              "</div>"
              "</div>")
         {:title {:href "post_link" :content "My Title"}
          :body "My content"
          :comment {:author "President Lincoln" :body "Fourscore and seven years ago..."}}

         (str "<div data-scope=\"post\">"
              "<a data-prop=\"title\" href=\"post_link\">My Title</a>"
              "<p data-prop=\"body\">My content</p>"
              "<div data-scope=\"comment\">"
              "<p>Author:<span data-prop=\"author\">President Lincoln</span></p>"
              "<p data-prop=\"body\">Fourscore and seven years ago...</p>"
              "</div>"
              "<div data-scope=\"comment\">"
              "<p>Author:<span data-prop=\"author\">President Roosevelt</span></p>"
              "<p data-prop=\"body\">The only thing we have to fear is fear itself.</p>"
              "</div>"
              "</div>")
         {:title {:href "post_link" :content "My Title"}
          :body "My content"
          :comment [{:author "President Lincoln" :body "Fourscore and seven years ago..."}
                    {:author "President Roosevelt" :body "The only thing we have to fear is fear itself."}]}))

      (testing "with multiple posts"
        (are [expected bindings] (= expected (render (bind-scope post :post bindings)))

          (str "<div data-scope=\"post\">"
               "<a data-prop=\"title\" href=\"post_link\">My Title</a>"
               "<p data-prop=\"body\">My content</p>"
               "<div data-scope=\"comment\">"
               "<p>Author:<span data-prop=\"author\">President Lincoln</span></p>"
               "<p data-prop=\"body\">Fourscore and seven years ago...</p>"
               "</div>"
               "<div data-scope=\"comment\">"
               "<p>Author:<span data-prop=\"author\">President Roosevelt</span></p>"
               "<p data-prop=\"body\">The only thing we have to fear is fear itself.</p>"
               "</div>"
               "</div>"
               "<div data-scope=\"post\">"
               "<a data-prop=\"title\" href=\"second_post_link\">My Second Title</a>"
               "<p data-prop=\"body\">My second content</p>"
               "</div>")
          [{:title {:href "post_link" :content "My Title"}
             :body "My content"
             :comment [{:author "President Lincoln" :body "Fourscore and seven years ago..."}
                       {:author "President Roosevelt" :body "The only thing we have to fear is fear itself."}]}
           {:title {:href "second_post_link" :content "My Second Title"}
            :body "My second content"
            :comment []}]

                    (str "<div data-scope=\"post\">"
               "<a data-prop=\"title\" href=\"post_link\">My Title</a>"
               "<p data-prop=\"body\">My content</p>"
               "<div data-scope=\"comment\">"
               "<p>Author:<span data-prop=\"author\">President Lincoln</span></p>"
               "<p data-prop=\"body\">Fourscore and seven years ago...</p>"
               "</div>"
               "<div data-scope=\"comment\">"
               "<p>Author:<span data-prop=\"author\">President Roosevelt</span></p>"
               "<p data-prop=\"body\">The only thing we have to fear is fear itself.</p>"
               "</div>"
               "</div>"
               "<div data-scope=\"post\">"
               "<a data-prop=\"title\" href=\"second_post_link\">My Second Title</a>"
               "<p data-prop=\"body\">My second content</p>"
               "<div data-scope=\"comment\">"
               "<p>Author:<span data-prop=\"author\">Albert Einstein</span></p>"
               "<p data-prop=\"body\">Education is what remains after one has forgotten what one has learned in school.</p>"
               "</div>"
               "</div>")
          [{:title {:href "post_link" :content "My Title"}
             :body "My content"
             :comment [{:author "President Lincoln" :body "Fourscore and seven years ago..."}
                       {:author "President Roosevelt" :body "The only thing we have to fear is fear itself."}]}
           {:title {:href "second_post_link" :content "My Second Title"}
            :body "My second content"
            :comment {:author "Albert Einstein"
                      :body "Education is what remains after one has forgotten what one has learned in school."}}]))))

  (testing "that data-prototype nodes are removed on binding"
    (let [partial (eh/parse-fragment "<div data-scope=\"post\"><span data-prop=\"author\">Author</span></div><div data-scope=\"post\" data-prototype><span data-prop=\"author\">Author</span></div>")]
      (is (= "<div data-scope=\"post\"><span data-prop=\"author\">Einstein</span></div>"
             (apply str (html/emit* (bind-scope partial :post {:author "Einstein"}))))))))
