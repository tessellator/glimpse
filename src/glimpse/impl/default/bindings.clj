(ns glimpse.impl.default.bindings
  "Functions that provide data binding support to Enlive templates."
  (:require [glimpse.impl.protocols :as gp]
            [glimpse.impl.seq :refer [resize]]
            [net.cgrand.enlive-html :as html]))

(defn scope-selector
  "Selector predicate, matches a top-level or nested scope element."
  [scope]
  (if-not (sequential? scope)
    [(html/attr= :data-scope (name scope))]
    (vec (flatten (interpose :> (map scope-selector scope))))))

(defn prop-node?
  "Gets a value indicating whether the node provided is a property node."
  [node]
  (boolean (get-in node [:attrs :data-prop])))

(defn scope-node?
  "Gets a value indicating whether the node provided is a scope node."
  [node]
  (boolean (get-in node [:attrs :data-scope])))

(defn prototype-node?
  "Gets a value indicating whether the node provided is a prototype node."
  [node]
  (boolean (get-in node [:attrs :data-prototype])))

(defn remove-prototype-nodes
  "Removes all scope nodes that are marked with data-prototype."
  [nodes]
  (html/transform nodes [(html/attr? :data-scope)]
                  #(when (not (prototype-node? %)) %)))

(defn default-version-node?
  "Gets a value indicating whether the node is marked as data-default."
  [node]
  (boolean (get-in node [:attrs :data-default])))

(defn version-node?
  "Gets a value indicating whether the current node contains version data."
  [node]
  (or (boolean (get-in node [:attrs :data-version]))
      (default-version-node? node)))

(defn filter-version
  "Filters a node by a version. Removes all elements that are versioned with a
  name different than the one specified."
  [node version]
  (first
   (if (nil? version)
     (html/transform [node] [(html/pred version-node?)]
                     #(when (default-version-node? %) %))
     (html/transform [node] [(html/pred version-node?)]
                     #(when (get (html/attr-values % :data-version) version) %)))))

(defn bind-prop-map
  "Binds the map to the node. All keys except :content are treated as attributes
  to be appended to node. The :content value, if provided, will replace any existing
  content in the node.

  A nil value will remove the corresponding attribute or content from the node."
  [node m]
  (let [attrs (merge (:attrs node) (dissoc m :content))
        attrs (select-keys attrs (for [[k v] attrs :when v] k))
        content (if (contains? m :content)
                  (some-> (:content m) str)
                  (:content node))]
    (when-not (and (empty? attrs) (nil? content))
      (assoc node :attrs attrs :content content))))

(defn bind-prop
  "Binds the provided data to the node.

  If data is not a map, it is converted to a string and used to replace the
  existing node content.

  If data is a map, all keys except :content are treated as attributes to be
  appended to node. The :content value, if provided, will replace any existing
  content in the node. A nil value will remove the corresponding attribute or
  content from the node."
  [node data]
  (cond
    (nil? data) nil
    (map? data) (bind-prop-map node data)
    :else (bind-prop-map node {:content data})))

(defn version
  "Gets the version from the provided data. The version is expected to exist as
  a value associated with :glimpse.views/version on the data."
  [data]
  (let [version (:glimpse.views/version data)]
    (cond
      (fn? version) (when-let [v (version data)] (name v))
      (nil? version) nil
      :else (name version))))

(defn bind-scope-node
  "Recursively binds data to the specified scope node."
  ([node data]
   (when-not (or (prototype-node? node)
                 (nil? data)
                 (and (sequential? data) (empty? data)))
     (if (sequential? data)
       (mapcat (partial bind-scope-node node) data)
       (reduce
        (fn [subnode [k v]]
          (html/transform subnode
                          [(html/pred #(= (name k)
                                          (or (get-in % [:attrs :data-prop])
                                              (get-in % [:attrs :data-scope]))))]
                          #(if (prop-node? %)
                             (bind-prop % v)
                             (bind-scope-node % v))))
        [(assoc (filter-version node (version data))
                :attrs
                (dissoc (:attrs node) :data-glimpse-unbound))]
        (seq data))))))

(defn bind-scope
  "Binds the provided data to a scope.

  If a scope value is provided, the scope is looked up in the collection of
  nodes provided.  Otherwise, it is assumed that a single node (the scope node)
  is provided and the supplied data will be bound to it.

  Data may be either sequential or a map.  If the data is sequential, the scope
  node is duplicated for and bound to each element in the sequence.  If a map
  is provided, the keys are looked up in the children elements as property or
  scope nodes.  If a property node is found corresponding to the key, its value
  is bound to the node.  If a scope node is found instead, the new scope node
  is bound to the value.

  A version may be provided in order to select the subset of nodes to render,
  according to the data-default and data-version markup. The version may be
  a function that accepts a piece of data and returns a version name string,
  a keyword of the version name, a version name string, nil, or a collection of
  such values. If nil is received, either directly or as the result of calling
  a version function, the unversioned and data-default children nodes will be
  rendered. If version is not a collection, it will be applied uniformly to
  all data; however, if it is a collection, it will apply like a mapping
  operation. If there are fewer versions than data, the data-default value will
  be selected for those data without a corresponding version.

  The version information may also be embedded in the data by using the
  :glimpse.views/version key on the data."
  ([nodes scope data]
   (html/transform nodes
                   (scope-selector scope)
                   #(bind-scope-node % data)))

  ([nodes scope version data]
   (let [data (if (sequential? data) data [data])
         version (if (sequential? version)
                   (resize (count data) version)
                   (repeat version))
         data (map #(assoc %1 :glimpse.views/version %2) data version)]
     (bind-scope nodes scope data))))
