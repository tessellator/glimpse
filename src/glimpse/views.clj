(ns glimpse.views
  "Functions for creating, binding, and rendering views with Glimpse."
  (:require [glimpse.impl.protocols :as gp]
            [glimpse.impl.default :as default])
  (:import [glimpse.impl.default DefaultGlimpseImplementation]))

(def ^{:dynamic true :private true}
  *current-implementation* (DefaultGlimpseImplementation.
                             "views"
                             (atom (assoc (default/construct :production "views" {})
                                          :mode :production))))

(defn set-current-implementation
  "Configures Glimpse to use the specified implementation."
  [g]
  (set! *current-implementation* g))

(defn get-mode
  "Returns a keyword that describes the current mode."
  []
  (gp/get-mode *current-implementation*))

(defn set-mode!
  "Configures the mode of operation."
  [m & opts]
  (gp/set-mode! *current-implementation* m opts))

(defn view
  "Creates a view based on the path."
  [path & opts]
  (gp/construct-view *current-implementation* path opts))

(defn render
  "Renders the view to HTML."
  [view & opts]
  (gp/render view opts))

(defn bind
  "Bind the data to the specified scope in the view."
  [view scope-selector data & opts]
  (gp/bind view scope-selector data opts))

(defn not-found
  "Provides functionality when a view for the specified uri is not found."
  [uri]
  (gp/not-found *current-implementation* uri))
