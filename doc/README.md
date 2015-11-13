# Glimpse Documentation

Welcome to the Glimpse documentation! While the documents here can be read in any order, they are in a numerical order to walk you from high-level concepts through the low-level details in an incremental fashion.

The documentation is broken down as follows:

* The [API docs](https://tessellator.github.io/glimpse) are posted online, but you can generated them locally by cloning the repository and running `lein codox` at the command line.

* The [introduction](01_intro.md) gives a high-level overview of what Glimpse is and how you can expect to use it.

* The [warmup](02_warmup.md) gives a brief tutorial on using Glimpse, while the [in-depth tutorial](03_guestbook.md) demonstrates a more complete application and explores all the little nooks and crannies.

* The next several documents discuss the details and internals of each aspect of Glimpse, discussing [layout directives](04_layout.md), [data binding](05_bindings.md), [modes](06_modes.md), and [Enlive interoperability](07_enlive.md) in turn.

* The [integration](08_integration.md) describes how to integrate Glimpse with different sets of libraries, such as Pedestal and Luminus.

* We finally come to a guide on [extending Glimpse](09_extending_glimpse.md), which describes how to extend the default implementation or build your own from scratch.


If you find the documentation lacking in any way, please let me know or make a [contribution](../CONTRIBUTING.md)!
