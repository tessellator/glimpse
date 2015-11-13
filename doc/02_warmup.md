# Glimpse Warmup

This warmup exercise introduces Glimpse through the development and deployment of a simple Twitter-based application. Since it demonstrates the basic functionality as briefly as possible, it glosses over some low-level details. If you wish to see an example that spares no detail, please refer to the [Glimpse in-depth tutorial](03_glimpse_in_depth.md).

This tutorial is based on the [Pakyow warmup](http://pakyow.org/warmup).


## Creating the Project
This tutorial uses [Leiningen](http://leiningen.org) for project management, so you will need to install and configure it before continuing.

To create the project, simply input the following command at a terminal:

`lein new glimpse-app warmup`

After the command completes, you will have a Glimpse project in a folder named 'warmup'.  The project is a set of files with the following structure:

```
warmup
|-- Procfile
|-- README.md
|-- dev
|   |-- warmup
|       |-- dev.clj
|-- project.clj
|-- resources
|   |-- public
|   |   |-- README.md
|   |-- views
|       |-- _templates
|       |   |-- default.html
|       |-- index.html
|-- src
    |-- warmup
        |-- routes.clj
        |-- web.clj
```

Don't sweat the details right now; we'll discuss each part in turn, and you will be very familiar with each piece by the end of the tutorial.

Right now, though, we need to start prototyping our views...

## Prototyping the Views
Glimpse supports a view-first workflow, so let's jump in and start prototyping our views!  Make sure that your terminal is in the `warmup` directory, and run the following command:

`lein prototype`

It may take a few moments to start up since Leiningen will download any libraries you need.  The command will start up a web server and open a browser window pointed to the local server endpoint.  If everything worked, you should see a page that simply says "Hello, world!".

Open your favorite editor and point it to the project folder. The `resources/views` folder contains all the files needed to create views. If you open the `index.html` file, you'll find the obligatory salutation. Go ahead and change `world` to `Glimpse` and save the file. Your browser should automatically refresh, and show the next content.

### A Stubbed Tweet (...or Three...)
Let's get started with making a landing page that displays a list of interesting tweets.  Replace the contents of `index.html` with the following:

```
<h1>Recent Interesting Tweets</h1>

<article data-scope="tweet">
  <hr>
  <header data-scope="user">
    <img src="http://placehold.it/50x50" data-prop="profile-image">
    <span data-prop="name">Author Name</span> - @<span data-prop="screen-name">handle</span>
  </header>
  <p data-prop="text">The tweet text goes here</p>
  <a href="/tweets/show" data-prop="link">Details</a>
</article>

<article data-scope="tweet" data-prototype>
  <hr>
  <header data-scope="user">
    <img src="http://placehold.it/50x50" data-prop="profile-image">
    <span data-prop="name">Another Author</span> - @<span data-prop="screen-name">otherHandle</span>
  </header>
  <p data-prop="text">Another tweet</p>
  <a href="/tweets/show" data-prop="link">Details</a>
</article>

<article data-scope="tweet" data-prototype>
  <hr>
  <header data-scope="user">
    <img src="http://placehold.it/50x50" data-prop="profile-image">
    <span data-prop="name">Last One</span> - @<span data-prop="screen-name">alwaysLast</span>
  </header>
  <p data-prop="text">The final tweet</p>
  <a href="/tweets/show" data-prop="link">Details</a>
</article>
```

Save the file, and the browser should once again refresh automatically.


#### Wait, what are all these `data-*` attributes?
Glimpse uses a set of data attributes to declare the structure and contents of the views. There are currently three different data attributes supported by Glimpse: `data-scope`, `data-prop`, and `data-prototype`.  `data-scope` describes a named data element that should be bound to the current HTML element and its children. Note that data scopes can be nested, as they are in the listing above. The `data-prop` attribute tells which property of the scope should be applied to the current element. Finally, `data-prototype` indicates that the current scope is intended for prototyping purposes only and should be thrown out (without data binding) in the final version of the view.

The structure of the scopes and props define the structure of the data to be bound to the view, and this will be discussed in greater detail when we build the server logic.


### Extracting into a Partial
It is often useful to reuse chunks of a view in several pages, and the tweet markup is a good example of this. In order to support reuse, Glimpse supports defining partials that can be included in pages. To create a partial, simply create a file under the `views` folder that begins with an underscore.  Partials can be defined in subdirectories, but they must be defined _at or above_ the uppermost directory containing a page that uses the partial.  This is to support override, which we'll see in action shortly.

For now, though, let's create a new file `_tweet.html` in the `views` folder and place all the tweet-related markup from `index.html` into it. Save both files and *poof* we no longer see any of the tweet data in the browser! Now, we need to tell the page to include the partial. We do that by using one of the layout directives provided by Glimpse. A layout directive is a specially formatted comment that provides an instruction on how to construct the view. In this case, we want to use an `@include` directive.  Add the following line at the end of `index.html`:

`<!-- @include tweet -->`

This tells the page to include the "tweet" partial - the file called `_tweet.html`. We won't discuss them here, but the other directives are `@template`, `@container`, and `@within`. They are used in the [in-depth tutorial](03_glimpse_in_depth.md).

At this point, we should appear to be right where we were before this subsection. But we'll reuse the partial in the next subsection, so it's all good. :)

### Creating a Details Page
Are you curious about what will happen if you click the "Details" link? 404? Go ahead - try it!

Well, was that what you expected? When we started Glimpse at the terminal with the command `lein prototype`, it configured Glimpse to run in a prototyping mode. Glimpse supports two modes out of the box: prototype and production. In prototype mode, Glimpse catches when you try to use a file that is not created and creates error messages such as the one you see now to be as helpful as possible. However, Glimpse does not exhibit this behavior in production mode. Instead, you will get the 404 response that you would expect.

Let's resolve the issue by creating a new file `tweets/show.html` under `views`. While we're at it, let's go ahead and give it the following markup:

```
<a href="/"><< Back to List</a>

<!-- @include tweet -->
```

Well, this works, but not only does it not really show the detail that we want, but we also do not want to see more than one tweet on this page. Let's _override_ the tweet partial to make it look like what want. Remember when I said that paritals must be defined _at or above_ the level at which they are used? This is where that becomes important.

When a view is loading and needs to find a partial, it looks for the partial in its current directory. If the partial is not found, it moves up a directory and searches there. This continues until the root folder (in this case `views`) is checked, and returns a 404 if it cannot be found. Because of this behavior, you can override a partial by placing a different implementation with the same name in a more deeply nested folder.

Let's create a new partial in `tweets/_tweet.html` and give it the following markup:

```
<article data-scope="tweet">
  <header data-scope="user">
    <img src="http://placehold.it/50x50" data-prop="profile-image">
    <span data-prop="name">Author Name</span> - @<span data-prop="screen-name">handle</span>
  </header>

  <p data-prop="text">The tweet text goes here</p>
  <p>Favorited <span data-prop="favorite-count">#</span> time(s)</p>
  <p>Retweeted <span data-prop="retweet-count">#</span> time(s)</p>
  <a href="#" data-prop="twitter-link">View on Twitter</a>
</article>
```

You'll notice that, without having to change `show.html`, that the markup is updated to reflect the overridding version of the tweet partial.

At this point, the prototype for our simple application is complete. It is navigable, and can be demonstrated _without writing the first line of code_. Pretty cool, I'd say! But now, we need to head over to the server and add the logic there...

If you want a little bit of a challenge, try to reduce the number of repeated elements across the tweet partials.  (Hint: you can include a partial in another partial.)


## Adding the Server Logic
Now it's time to add the server logic. We'll use Ring and Compojure to set up routes to our views, and we'll use the Glimpse API to build and populate them.

### Updating the Project
In order to interface with Twitter API, we'll use the [twitter-api library](https://github.com/adamwynne/twitter-api). We'll also need [camel-snake-kebab](https://github.com/qerub/camel-snake-kebab), which is a handy library for converting between word cases. If you haven't already, go ahead and kill the server started for prototyping views. Then open `project.clj` and add `[twitter-api "0.7.8"]` and `[camel-snake-kebab "0.3.2"]` as dependencies.

### Running a REPL
It is convenient to work with a REPL while developing your application, so let's go ahead and start one now. To do so, run `lein repl` from the command line.

The REPL will start in the `warmup.dev` namespace, which has a few useful functions for managing the server. Start the server by executing `(start-server)` at the REPL. A new browser tab will open and point to the root path.

You'll notice that the server is still running in prototype mode; that is configured when a REPL is launched. The [`set-mode!`](https://tessellator.github.io/glimpse/glimpse.views.html#var-set-mode.21) function allows you to change modes dynamically by providing a keyword specifying the mode (e.g., `:production` or `:prototype`) and any additional options. Options are not currently used but are reserved for future use and custom implementations.

Production mode is the default for Glimpse, but there are some configuration options set in `project.clj` that set the mode to prototype mode when starting your application with `lein prototype` or with a REPL.

Let's switch over to production mode by entering the following at the REPL:

```
(glimpse/set-mode! :production)
```

Since we have changed the state of the system from the REPL (and not changed a file), you'll have to refresh the browser to see the change. You should get a 404 response containing "Not Found" as the content. This is because we have not yet defined any routes in our application. Let's do that now.

### Defining the Routes
The `glimpse-app` template uses [Compojure](https://github.com/weavejester/compojure) for defining routes, and they are defined in `routes.clj`. There is currently no route defined, so let's fix that. Add the following to the `warmup-routes` definition:

`(GET "/" [] "Hello")`

Note that since this changes the route definitions, we need to restart the server. We can do that by executing `(restart-server)` at the REPL. Refresh your browser, and you should see "Hello" as the page content.

Call the [`view`](https://tessellator.github.io/glimpse/glimpse.views.html#var-view) function with a URI to the desired page as an argument to load the view. The view at this point is in an intermediate form that may be transformed before being rendered with the [`render`](https://tessellator.github.io/glimpse/glimpse.views.html#var-render) function. We don't want to change anything yet, so replace the route we defined before with the following:

`(GET "/" [] (glimpse/render (glimpse/view "/")))`

In this case, we pass the URI "/" because we want the root `index.html` file to render. URIs ending with "/" are automatically extended to include "index" at the end, so the provided URI looks for a view located at "/index" under the `views` folder. The default implementation of Glimpse currently only supports loading HTML files, so the file "/index.html" will be loaded.

Restart the browser and refresh the browser and you'll see the stub for a single tweet. What happened to the other two? Remember that we are now in production mode and that the other tweet entries contained `data-prototype` attributes. In production mode, scopes marked as prototypes are discarded. The two tweet stubs were removed and now we're left with just the first one.

We're just one step away from having our application exhibit interesting behavior. Next, we need to bind data to the view.

### Binding Data to the View
Now that we can load and render views, we need to be able to bind data to them. We can do that with the [`bind`](https://tessellator.github.io/glimpse/glimpse.views.html#var-bind) function. `bind` takes three arguments: the view returned from the `view` function, a scope selector, and data to bind to the view. You may also provide options, but they are not currently supported in Glimpse (reserved for future use and custom implementations). The function will return a new version of the view with the data bound to it in the specified scope.

The scope selector will be used to select nodes from the view and may be either a keyword of the scope name (e.g., `:tweet` finds nodes with `data-scope="tweet"`) or a vector of keywords containing the names of successively nested scopes (e.g., `[:tweet :user]`).

The data to bind to the scope may be either a map or a sequence of maps. If the data comprise a sequence of maps, then the scope will be duplicated for and bound to each map in the sequence. When the data are a map, the keys in the map will map to corresponding properties under the scope. The value for a property may similary taken on different forms. If `nil`, the property element is removed from the view. If the value is a map, then the keys are associated with the attributes of the property element (e.g., `{:href "#"}` yields `href="#"` on the element declared as the associated `data-prop`. The key `:content` is special and will be used to populate the content of the element. Finally, if the element is a value, then the value is used as the content of the property element.

This is easiest to see with an example, so let's try it out now. In `routes.html`, replace the root route with the following:

```
(GET "/" [] (glimpse/render
              (glimpse/bind (glimpse/view "/")
                             :tweet
                             {:user {:profile-image {:src "http://placehold.it/100x100"}
                                     :name "Abraham Lincoln"
                                     :screen-name "HonestAbe"}
                              :text "Four score and seven years ago..."
                              :link {:href "http://bit.ly/1bFJewr"
                                     :content "The Gettysburg Address"}})))
```

Call `(restart-server)` once again, and you will see all the information displayed. I encourage you to spend a little time looking at the mapping from the data to the view to see how the scopes and props play out. Try binding a vector of maps to the view and see how the scopes repeat.

### Interacting with Twitter
Now that we know how to bind data to our views, we're finally ready to pull some data from Twitter and use it to populate our application! First, you'll need to create a Twitter App over at https://apps.twitter.com/.  After you create the app, click the "Keys and Access Tokens" tab and click the "Create Access Token" button.

Create a new file in the `src/warmup` folder called `twitter.clj` and use the following code:

```
(ns warmup.twitter
  (:require [twitter.oauth :refer [make-oauth-creds]]
            [twitter.callbacks.handlers :refer :all]
            [twitter.api.restful :as rest]
            [twitter.api.search :refer [search]])
  (:import [twitter.callbacks.protocols SyncSingleCallback]))

(def callback (SyncSingleCallback. response-return-body
                                   response-throw-error
                                   exception-rethrow))

(def credentials (make-oauth-creds *consumer-key*
                                   *consumer-secret*
                                   *access-token*
                                   *access-token-secret*))

(defn get-tweets [hashtag]
  (:statuses (search :oauth-creds credentials
                     :params {:q hashtag}
                     :callbacks callback)))

(defn get-tweet [id]
  (rest/statuses-show-id :oauth-creds credentials
                         :params {:id id}
                         :callbacks callback))
```

You'll need to provide the consumer key, consumer secret, access token, and access token secret from the "Keys and Access Tokens" tab under the Application Management page on https://apps.twitter.com. You can simply replace the variable name with a string containing the data from Twitter for the time being.

Once you have the keys in place, try out the functions at the REPL! Try entering `(warmup.twitter/get-tweets "#programming")` and sifting through the results.

Let's bind the data to the view by adding a new requirement on `warmup.web` of `[warmup.twitter :as twitter]` and replacing the route with the following code and then restarting the server:

```
(GET "/" [] (glimpse/render
              (glimpse/bind (glimpse/view "/")
                            :tweet
                            (twitter/get-tweets "#programming"))))
```

This does bind _some_ of the data, but not all of it is bound correctly. Let's fix that by creating a function to format tweets in the `warmup.twitter` namespace:

```
(defn format-tweet [tweet]
  (-> (transform-keys ->kebab-case-keyword tweet)
      (assoc-in [:user :profile-image :src] (get-in tweet [:user :profile_image_url_https]))
      (assoc-in [:link :href] (str "/tweets/show/" (:id_str tweet)))
      (assoc-in [:twitter-link :href] (str "https://twitter.com/statuses/" (:id_str tweet)))))
```

You will have to add the following entries to the `:require` statement in `twitter.clj`:

```
[camel-snake-kebab.core :refer :all]
[camel-snake-kebab.extras :refer [transform-keys]]
```

Finally, update the root route definition to be as follows:

```
(GET "/" [] (glimpse/render
              (glimpse/bind (glimpse/view "/")
                            :tweet
                            (map twitter/format-tweet (twitter/get-tweets "#programming")))))
```

Our index view is finally finished! Let's add a route for our other view using everything we've built so far:

```
(GET "/tweets/show/:id" [id]
  (glimpse/render
    (glimpse/bind (glimpse/view "/tweets/show")
                  :tweet
                  (twitter/format-tweet (twitter/get-tweet id)))))
```

At this point, the application is complete! But we repeat quite a bit of information in the route definitions. Let's clean that up a little...


### DRYing Out the Routes with Ring Middleware
Glimpse provides some [middleware functions](https://tessellator.github.io/glimpse/glimpse.middleware.html) to make working with Ring and Compojure a little nicer experience. All you need to do is update the `warmup.web` ns declaration and application definition to be as follows:

```
(ns warmup.web
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.server.standalone :refer [serve]]
            [compojure.core :refer [defroutes routes ANY wrap-routes]]
            [compojure.route :as route]
            [glimpse.views :as glimpse]
            [glimpse.middleware :refer [wrap-glimpse]]
            [warmup.routes :refer [warmup-routes]])
  (:gen-class))

(def application (-> (routes warmup-routes app-routes)
                     (wrap-routes wrap-glimpse)
                     (wrap-defaults site-defaults)))
```

This will update the application to 1) automatically identify the view path based on the URI, 2) load the view, 3) given a map, use the keys as scope names and perform bind operations for each, and finally 4) render the view. Let's take a look at what this means for our application.

First, Glimpse will automatically identify the view route based on the URI. This is achieved by removing matched route parameters from the URI and using what's left as a path to a view. For example, if we receive a request with the URI "/tweets/show/1234", and we have a member in the request called route-params set as `{:id 1234}`, then the resulting lookup path would be "/tweets/show" and would find the file `tweets/show.html` under the `views` directory. The view will be loaded from this path.

Now, after your code executes, it assumes that you will provide a map whose keys are the names of the scope to which you want to bind the corresponding values. In our case here, we would wrap the data structure coming out of `warmup.twitter` in a map with a key `:tweet`.

...and that's it! Glimpse will perform all the binding and rendering for you.  Here is what `routes.clj` should look like when you're done:

```
(ns warmup.routes
    (:require [compojure.core :refer :all]
              [glimpse.views :as glimpse]
              [warmup.twitter :as twitter]))

(defroutes warmup-routes
  (GET "/" [] {:tweet (map twitter/format-tweet (twitter/get-tweets "#programming"))})
  (GET "/tweets/show/:id" [id] {:tweet (twitter/format-tweet (twitter/get-tweet id))}))
```

If you restart the server one last time, you can browse through the application like before. Now we just need to deploy the application...


## Deploying to Heroku
Well, we're almost there! There are just a few more steps to follow to getting your application deployed. We'll deploy to Heroku. If you need more info than I provide here, check out [Getting Started with Clojure on Heroku](https://devcenter.heroku.com/articles/getting-started-with-clojure#introduction).

First, we need to ensure that you have the project stored in a git repository. If you do not, you can create one by putting `git init` in a terminal whose working directory is the warmup project directory. Then you can create your first commit with `git add .; git commit -m "initial commit"`. (Normally you would take your time and write a good commit message.)

Next, run `heroku create` which will create a random name for your app. Finally, deploy with `git push heroku master`. After everything completes building, you can browser to the URL created for the project.

Congratulations! You have now built and deployed your first Glimpse application!

## End Stuff
So that's it...what did you think?  I'd love to hear about any issues you encountered as well as any ideas you might have for new features!  Please feel free to [open an issue](https://github.com/tessellator/glimpse/issues/new) or reach out on [Twitter](https://twitter.com/tessellator).
