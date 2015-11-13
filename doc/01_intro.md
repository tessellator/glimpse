# Introduction to Glimpse

Glimpse is a view composition library in Clojure that supports a view-first workflow. This makes it very useful for working with designers and building mock-up prototypes.

This document provides a high-level overview of Glimpse. I recommend reading this document and then working through the [warmup tutorial](02_warmup.md). Afterward, you will have a handle on most of what Glimpse provides.

## Building Views
Glimpse is a library that is concerned with building views for your application. While the API defined in [`glimpse.views`](https://tessellator.github.io/glimpse/glimpse.views.html) does not specify any particular output format, Glimpse ships with support for building HTML views. This is achieved by using [convention over configuration](https://en.wikipedia.org/wiki/Convention_over_configuration) to define where views are located and how they are constructed. By default, the views are located in the `resources/views` folder.

There are three types of views in Glimpse, and each type has a distinct location and/or naming scheme.  The three types of views are specified in the following table.

View Type | Description
--------- | -----------
Page | A page specifies the main content of a view for a response. The file `index.html` is treated as a matching when the folder root is requested (e.g., a URI `/tweets/` will map to the file `resources/views/tweets/index.html`).  Pages cannot be overridden.
Template | A template contains the skeleton of the page (e.g., the `<head>` information, and a simple body). Templates must appear in a folder named `_templates`, and `default.html` is the default template used. Templates may be specified per page, and they may be overridden.
Partial | A partial contains a part of a view. Partials are useful for defining reusable chunks of HTML that may be referenced in several pages. A file containing a partial must begin with an underscore (e.g., `_partial.html` or `_my_partial.html`). Partials may be overridden.

"Overriding" occurs when two files share the same name but are located in different directories. The file with the most specific path (i.e., the deepest path) is preferred when constructing views, and so it is said to "override" the file with the less specific path.

Layout directives provide declarative composition instructions, and they are implemented as specially-named HTML comments. The layout directives are described in the following table.

Directive | Description
--------- | -----------
`@template` | Describes which template should be used when building a view. Optional; defaults to "default". If provided more than once, throws an exception. Example: `<!-- @template my-template -->`
`@container` | Creates a placeholder into which content may be placed using `@within` directives. May be named or unnamed. Example: `<!-- @container my-container -->`
`@within` | Specifies content to be placed into a container. Must be closed with a `/within` comment. All content between the opening directive and closing directive will be appended in the named container. Example: `<!-- @within my-container --><p>Some content</p><!-- /within -->`.
`@include` | Includes a partial into the current document. Takes the name of the partial and search for a file shares the name with a preceding underscore. Example: `<!-- @include tweet -->`; looks for `_tweet.html`.


## Data Binding

When designing views, you may specify heirarchical data structures to which data may later be bound. This is achieved by specifying a scope and its properties.

A data scope is a named location in a view into which data will later be inserted. To declare a scope attach a `data-scope` attribute to an element. The scope includes all the children elements.

A data property refers to an element within a scope to which a specific, named piece of data will be bound. A property is defined by attaching a `data-prop` attribute to the desired element.

To bind the data, use the [`glimpse.views/bind`](https://tessellator.github.io/glimpse/glimpse.views.html#var-bind) function, which accepts a keyword name of the scope (or a vector of keywords to get to a nested scope) and the data to bind to the view. That data may be a map whose keys match the props, or a vector of such maps. If a vector of data is provided, then the scope is duplicated to match the number of data points to bind. Following is a simple example:

```
<article data-scope="post">
  <header data-prop="title">Post Title</header>
  <p data-prop="text">Post text goes here</p>
  <span data-prop="author">Author</span>
</article>
```

```
(glimpse/bind :post {:author "Abraham Lincoln"
                     :title "The Gettysburg Address"
		     :text "Four score and seven years ago..."})
```

After binding occurs, the output is as follows:

```
<article data-scope="post">
  <header data-prop="title">The Gettysburg Address</header>
  <p data-prop="text">Four score and seven years ago...</p>
  <span data-prop="author">Abraham Lincoln</span>
</article>
```


## Modes
In order to support view-first workflows, Glimpse may be configured to run in different modes. A mode provides an execution context appropriate for a given environment. While supported modes are implementation-specified, all implementations must provide two basic modes: prototype and production.

`:prototype` - Prototype mode is a very flexible mode that generates a default view when a backing route or component definition does not exist for a given path.

`:production` - This is the default mode. In production mode, exceptions are generated when a view path is not found or data is not applied correctly to the view.

The mode is set by [`glimpse.views/set-mode!`](https://tessellator.github.io/glimpse/glimpse.views.html#var-set-mode.21), which accepts a keyword argument.