# Glimpse

```
When I was a child
I caught a fleeting glimpse
Out of the corner of my eye.

I turned to look but it was gone
I cannot put my finger on it now
The child is grown,
The dream is gone.

I have become comfortably numb.


— Pink Floyd, "Comfortably Numb"
```

Glimpse is a view composition library in Clojure supporting a view-first workflow. It is inspired by [Pakyow](http://www.pakyow.org).


## Installation
Add the following dependency to your `project.clj` file:

[![Clojars Project] (http://clojars.org/glimpse/latest-version.svg)] (http://clojars.org/glimpse)


## Supported Clojure Versions

Glimpse requires Clojure 1.7+.


## Usage
Following is the interesting portion of a small Ring/Compojure application using Glimpse.

```
<h1>Hello from Glimpse</h1>

<article data-scope="post">
  <header data-prop="title">Post Title</header>
  <p data-prop="text">Post text goes here</p>
  <span data-prop="author">Author</span>
</article>
```

```clojure
(ns hello-world.core
  (:require [compojure.core :refer :all]
            [glimpse.views :as glimpse]))

(defroutes app
  (GET "/" [] (-> (glimpse/view "/")
                  (glimpse/bind :post {:author "Abraham Lincoln"
                                       :title "The Gettysburg Address"
                                       :text "Four score and seven years ago..."})
                  (glimpse/render))))
```

Please refer to the [warmup](doc/02_warmup.md) for a complete example.  You can find the API documentation [here](https://tessellator.github.io/glimpse) and other documentation [here](doc/).

## License

Copyright © 2015 Thomas C. Taylor and contributors.

Distributed under the Eclipse Public License, the same as Clojure.
