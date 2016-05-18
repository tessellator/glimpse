(defproject glimpse "0.1.1"
  :description "A view composition library in Clojure supporting a view-first workflow"
  :url "https://github.com/tessellator/glimpse"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [enlive "1.1.6"]
                 [camel-snake-kebab "0.3.2"]]
  :plugins [[lein-codox "0.9.0"]]
  :codox {:source-uri "https://github.com/tessellator/glimpse/blob/master/{filepath}#L{line}"
          :doc-paths []})
