(ns glimpse.impl.protocols
  "Provides the protocols for an implementation of Glimpse.")

(defprotocol IGlimpseImplementation
  "Represents an implementation of Glimpse that provides the functionality
  defined by the API in `glimpse.views`."

  (get-mode [this]
    "Returns a keyword that describes the current mode.

     Supported modes are implementation-dependent, but all implementations
     should provide support for at least :prototype and :production modes.")

  (set-mode! [this mode opts]
    "Configures the mode.

     Valid modes are implementation-dependent, but every implementation must
     support :prototype and :production modes.")

  (construct-view [this uri opts]
    "Constructs a view based on the provided uri and options.")

  (not-found [this uri]
    "A function that is called when no view is found for the specified uri."))

(defprotocol IView
  "Represents a view to which data can be bound and can be rendered."
  (bind [this scope-selector data opts]
    "Binds data to the view with the given scope selector and data.

    The scope selector can be a keyword naming the scope or a vector of keywords
    naming nested scopes.")

  (render [this opts]
    "Renders the view.

    In the default implementation, HTML is created and returned as a string."))
