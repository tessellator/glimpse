(ns glimpse.impl.enlive-helpers
  "Functions to manipulate Enlive templates, especially for comment nodes."
  (:require [net.cgrand.enlive-html :as html]
            [net.cgrand.jsoup :as jsoup])
  (:import [org.jsoup Jsoup]))

(defprotocol IEnliveView
  "A protocol for converting views into Enlive-compatible representations."

  (->enlive [this]
    "Converts the view into Enlive nodes."))

(defn parse-fragment
  "Transforms an HTML string into an Enlive fragment."
  [html-str]
  (-> (Jsoup/parseBodyFragment html-str)
      .body
      jsoup/->nodes
      :content))

(defn parse-document
  "Transforms an HTML string into Enlive nodes for an entire document."
  [html-str]
  (jsoup/->nodes (Jsoup/parse html-str)))

(defn comment-pred
  "Turns a predicate on strings into a predicate-step that selects comment nodes
  that satisfy the predicate."
  [f]
  #(and (html/comment-node %) (f (:data (first %)))))

(defn comment=
  "Selector predicate, tests if the node is a comment and whose data is equal to s."
  [s]
  (comment-pred #(= % s)))

(defn comment-matches
  "Selector predicate, tests if the node is a comment and whose data matches re."
  [re]
  (comment-pred #(re-matches re %)))

(defn remove-nodes
  "Removes from nodes those which match the selector."
  [nodes selector]
  (html/transform nodes selector nil))

(defn replace-with-children
  "Removes selected nodes and replaces them with their respective children."
  [nodes selector]
  (html/transform nodes selector #(:content %)))

(defn node-content
  "Gets the content of a node if found.  Otherwise, returns an empty vector."
  [node]
  (or (:content node) []))
