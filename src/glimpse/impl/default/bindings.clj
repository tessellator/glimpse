(ns glimpse.impl.default.bindings
  "Functions that provide data binding support to Enlive templates."
  (:require [glimpse.impl.protocols :as gp]
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
  is bound to the value."
  ([nodes scope data]
   (html/transform nodes (scope-selector scope) #(bind-scope % data)))

  ([node data]
   (when-not (or (prototype-node? node)
                 (nil? data)
                 (and (sequential? data) (empty? data)))
     (if (sequential? data)
       (mapcat (partial bind-scope node) data)
       (reduce
        (fn [subnode [k v]]
          (html/transform subnode
                          [(html/pred #(= (name k)
                                          (or (get-in % [:attrs :data-prop])
                                              (get-in % [:attrs :data-scope]))))]
                          #(if (prop-node? %)
                             (bind-prop % v)
                             (bind-scope % v))))
        [(assoc node :attrs (dissoc (:attrs node) :data-glimpse-unbound))]
        (seq data))))))
