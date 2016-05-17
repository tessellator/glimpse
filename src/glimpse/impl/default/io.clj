(ns glimpse.impl.default.io
  "Functions that find HTML resources."
  (:require [clojure.java.io :as io]
            [clojure.string :refer [blank?]])
  (:import [java.nio.file Paths]))

(defn get-path
  "Creates a path given the individual parts of the path."
  [first & more]
  (.toString (Paths/get first (into-array String more))))

(defn page-resource
  "Gets the HTML page resource for a given uri.

  If the uri ends with a '/', a file named 'index.html' is assumed to be the
  intended resource. If a file is not found based on the name, a folder
  matching the name is searched for index.html."
  ([root uri]
   (if (.endsWith uri "/")
     (page-resource root uri "index")
     (let [f (io/file uri)]
       (page-resource root (.getParent f) (.getName f)))))

  ([root uri name]
   (or (io/resource (get-path root uri (str name ".html")))
       (io/resource (get-path root uri (str name "/index.html"))))))

(defn template-resource
  "Gets the HTML template resource specified by name for the given uri."
  [root uri name]
  (let [uri-parent (cond
                     (= uri "/") ""
                     (.endsWith uri "/") (.toString (io/file uri))
                     :else (.getParent (io/file uri)))
        path (get-path root uri-parent "_templates" (str name ".html"))
        res (io/resource path)]
    (cond
      (not (nil? res)) res
      (blank? uri-parent) nil
      :else (template-resource root uri-parent name))))

(defn partial-resource
  "Gets the HTML partial resource specified by name for the given uri."
  [root uri name]
  (let [path (get-path root uri (str "_" name ".html"))
        res (io/resource path)]
    (cond
      (not (nil? res)) res
      (blank? uri) nil
      :else (partial-resource root (or (.getParent (io/file uri)) "") name))))
