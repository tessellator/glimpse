(ns glimpse.middleware
  "Functions to interop with Ring and Compojure."
  (:require [clojure.string :as str]
            [glimpse.views :as glimpse]))

(defn lookup-path
  "Creates a path to use for searching for views.

  Substitutes values of wildcards for nothing in the request URI (e.g., if the
  URI is `/post/1234/comments`, and the route-params are {:id 1234}, then the
  resulting lookup path will be `/post/comments`."
  [request]
  (let [path (reduce (fn [uri param] (str/replace uri (re-pattern (str "/" param)) ""))
                     (:uri request)
                     (vals (dissoc (:route-params request) :*)))]
    (if (empty? path) "/" path)))

(defn create-body
  "Creates an HTML string given the request and using the response value to bind
  to the view."
  [request response]
  (when-let [view (glimpse/view (lookup-path request))]
    (glimpse/render
     (reduce (fn [view [scope data]] (glimpse/bind view scope data))
             view
             (seq response)))))

(defn wrap-glimpse
  "Provides a Glimpse wrapper for Ring.  Must be used *after* route-params are
  populated in the request.

  Looks up the view based on the request URI.  Takes a map of data from the
  route implementation and binds it to the view, using the top-level keys as
  scope names.  Renders the view and returns the resulting HTML."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (when-let [body (create-body request response)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body body}))))
