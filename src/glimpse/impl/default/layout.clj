(ns glimpse.impl.default.layout
  "Functions for manipulating Enlive templates using layout directives."
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]
            [glimpse.impl.enlive-helpers :as eh]
            [camel-snake-kebab.core :refer [->snake_case ->kebab-case-keyword]]))

(defprotocol ILoader
  "Represents an entity that can load Enlive nodes given a request uri and a
  name."
  (load-page [this uri]
    "Loads a page fragment based on the request uri and an optional name.")

  (load-template [this uri name]
    "Loads an entire document based on the request uri and provided name.")

  (load-partial [this uri name]
    "Loads a partial fragment based on the request uri and name."))

(defn template-name
  "Finds the template name from the string. Returns nil if not found."
  [s]
  (when-let [name (second (re-matches #"@template\s+([\w-]+)" (str/trim s)))]
    (->snake_case name)))

(def template-node
  "Selector predicate, matches valid template directives."
  (eh/comment-pred template-name))

(defn find-template-name
  "Finds the template name in the collection of nodes.

  Returns a default value if no template name is found.  Throws an exception if
  more than one template name is found."
  [nodes]
  (let [results (html/select nodes [template-node])]
    (condp = (count results)
      0 "default"
      1 (template-name (:data (first results)))
      (throw (Exception. "More than one template node provided.")))))

(defn container-name
  "Finds the container name from the string.

  Returns :default if the string is a container directive with no name.  Returns
  nil if not found."
  [s]
  (when-let [result (re-matches #"@container\s*([\w-]+)?" (str/trim s))]
    (or (some-> (second result) ->kebab-case-keyword) :default)))

(def container-node
  "Selector predicate, matches valid @container directives."
  (eh/comment-pred container-name))

(defn container-element-node
  "Selector predicate, matches container elements."
  ([]
   :glimpse-container)

  ([container-name]
   [:glimpse-container (html/attr= :name (name container-name))]))

(defn create-container-elements
  "Transforms container directives into element nodes."
  [nodes]
  (html/transform nodes
                  [container-node]
                  #(hash-map :tag :glimpse-container
                             :attrs {:name (name (container-name (:data %)))}
                             :content [])))

(defn within-name
  "Finds the container name from the string.

  Returns nil if not found."
  [s]
  (some-> (re-matches #"@within\s+([\w-]+)" (str/trim s))
          second
          ->kebab-case-keyword))

(def within-starting-node
  "Selector predicate, matches nodes that start a within directive fragment."
  (eh/comment-pred within-name))

(def within-ending-node
  "Selector predicate, matches nodes that end a within directive fragment."
  (eh/comment-matches #"\s*\/within\s*"))

(def within-fragment-selector
  "Fragment selector for within directives."
  {[within-starting-node] [within-ending-node]})

(defn move-within-containers
  "Finds within fragments within nodes, removes them, and places their content
  within the specified containers."
  [nodes]
  (let [fragments (html/select nodes within-fragment-selector)
        nodes (eh/remove-nodes nodes within-fragment-selector)]
    (reduce (fn [ns frag]
              (let [n (within-name (:data (first frag)))
                    content (rest (butlast frag))]
                (html/transform ns
                                [(container-element-node (name n))]
                                (html/append content))))
            nodes
            fragments)))

(defn include-name
  "Finds the include name in the string.

  Returns nil if not found."
  [s]
  (some-> (re-matches #"@include\s+([\w-]+)" (str/trim s))
          second
          ->snake_case))

(def include-node
  "Selector predicate, matches nodes that contain an include directive."
  (eh/comment-pred include-name))

(defn include-all
  "Replaces @include directives with appropriate content fetched via the
  specified loader.  Subject to a max-depth, which defaults to 10."
  ([nodes loader uri]
   (include-all nodes loader uri 10))

  ([nodes loader uri max-depth]
   (when (pos? max-depth)
     (html/transform nodes
                     [include-node]
                     #(let [name (include-name (:data %))
                            fragment (load-partial loader uri name)]
                        (include-all fragment loader uri (dec max-depth)))))))

(defn mark-scopes-as-unbound
  "Adds a data-glimpse-unbound attribute to all scope nodes."
  [nodes]
  (html/transform nodes [(html/attr? :data-scope)]
                  #(assoc-in % [:attrs :data-glimpse-unbound] "")))

(defn construct-view
  "Constructs a complete view given a request uri and loader."
  [uri loader]
  (let [page (load-page loader uri)]
    (-> (load-template loader uri (find-template-name page))
        (html/transform
         [:body]
         (html/append {:type :comment
                       :data "@within default"}
                      (eh/remove-nodes page within-fragment-selector)
                      {:type :comment
                       :data "/within"}
                      (html/select page within-fragment-selector)))
        (include-all loader uri)
        create-container-elements
        move-within-containers
        (eh/replace-with-children [(container-element-node)])
        mark-scopes-as-unbound)))
