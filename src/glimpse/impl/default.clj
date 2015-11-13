(ns glimpse.impl.default
  "Provides a default, extensible implementation of Glimpse."
  (:require [glimpse.impl.protocols :refer :all]
            [glimpse.impl.default.bindings :as bindings]
            [glimpse.impl.default.io :refer :all]
            [glimpse.impl.default.layout :as layout]
            [glimpse.impl.enlive-helpers :as eh]
            [net.cgrand.enlive-html :as html]))

(defmulti construct
  "A multimethod for constructing a mode for the default Glimpse implementation.

  A method will receive three arguments: the mode as a keyword, the view path,
  and an options map.  The result must be a map with the following:

  :construct-view => a function that accepts a uri and creates a view
  :not-found      => a function that accepts a uri for which no view is found"
  (fn [mode _ _] mode))

(deftype PrototypeLoader [root]
  layout/ILoader
  (load-page [this uri]
    (eh/parse-fragment
     (or (some-> (page-resource root uri) slurp)
         (let [path (str root uri (when (.endsWith uri "/") "index") ".html")]
          (str "<h1>Uh-oh! No page for this request path was found.</h1>"
               "<p>Try creating a view at \"resources/" path "\" and reloading this page.</p>")))))

  (load-template [this uri name]
    (eh/parse-document
     (or (some-> (template-resource root uri name) slurp)
         (str "<html>"
              "<head><title>Glimpse: 404</title><head>"
              "<body>"
              "<h1>Uh-oh! No template for this request path was found.</h1>"
              "<p>Try creating a template at \""
              (str "resources/" root "/_templates/" name ".html")
              "\" and reloading this page.</p>"
              "<hr>"
              "<!-- @container -->"
              "</body>"
              "</html>"))))

  (load-partial [this uri name]
    (eh/parse-fragment
     (or (some-> (partial-resource root uri name) slurp)
         (str "<h1>Uh-oh! No partial \"" name "\" for this request path was found.</h1>"
              "<p>Try creating a partial at \""
              (str "resources/" root "/_" name ".html")
              "\" and reloading this page.</p>")))))

(deftype ProductionLoader [root]
  layout/ILoader
  (load-page [this uri]
    (when-let [res (page-resource root uri)]
      (eh/parse-fragment (slurp res))))

  (load-template [this uri name]
    (when-let [res (template-resource root uri name)]
      (eh/parse-document (slurp res))))

  (load-partial [this uri name]
    (when-let [res (partial-resource root uri name)]
      (eh/parse-fragment (slurp res)))))


(deftype View [view]
  IView
  (bind [this scope-selector data opts]
    (View. (bindings/bind-scope view scope-selector data)))

  (render [this opts]
    (apply str (html/emit* view)))

  eh/IEnliveView
  (->enlive [this]
    view))

(deftype DefaultGlimpseImplementation [root options]
  IGlimpseImplementation
  (get-mode [_]
    (:mode @options))

  (set-mode! [_ mode opts]
    (reset! options (assoc (construct mode root opts) :mode mode))
    mode)

  (construct-view [_ uri _]
    ((:construct-view @options) uri))

  (not-found [_ uri]
    ((:not-found @options) uri)))

(defmethod construct :prototype [_ root _]
  (let [loader (PrototypeLoader. root)
        create-view #(View. (layout/construct-view % loader))]
    {:construct-view create-view
     :not-found create-view}))

(defmethod construct :production [_ root _]
  (let [cache (atom {})
        loader (ProductionLoader. root)]
    {:construct-view (fn [uri]
                       (let [c @cache]
                         (if (contains? c uri)
                           (get c uri)
                           (let [nodes (layout/construct-view uri loader)
                                 nodes (bindings/remove-prototype-nodes nodes)
                                 view (View. nodes)]
                             (swap! cache assoc uri view)
                             view))))
     :not-found (fn [_])}))
