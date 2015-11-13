# Contributing to Glimpse

So you want to help make Glimpse better? Great! I'm happy that you want to help, and I greatly appreciate the time and energy you invest in it. I firmly believe that building a great product requires contribution from people of varying opinions and skill sets, and I would love to foster an environment for making Glimpse great here.

To that end, I've put forth some guidelines for contributing to Glimpse. Following these guidelines will streamline the process of incorporating your contribution. And it will make my life easier. :)

I built Glimpse to help smooth the on-boarding process for building web applications in Clojure and help grow the Clojure community. I sincerely want more people building cool stuff and pushing the envelope, and I think Clojure is a great community for that. If we all just follow [Wheaton's Law](https://twitter.com/wilw/status/5966220832), we'll get along just fine.

Thanks!


## Reporting issues

If you find a bug in the source or documentation or if you have an idea for a new feature, please feel free to [open an issue](https://github.com/tessellator/glimpse/issues). I am certainly interested having a discussion with you!

Is there some reason you don't want to create an issue? I totally get it - I'm pretty shy and terrified of making a fool of myself, so I usually lurk in communities rather than jumping in! Whatever the reason, send me a DM on [Twitter](https://twitter.com/tessellator) and we'll go from there.


## Contributing code and documentation

So you want to hack on some code or documentation? Great! I always love seeing how other people approach and solve problems, and I'm eager to work with you to incorporate your work into Glimpse. Following are some things to keep in mind before clicking that "Create pull request" button.


### Consider creating an issue first

If the contribution that you want to make is significant, please consider creating an issue first. This allows for us to discuss the issue and make sure that it is what is best for Glimpse before you invest a lot of your time and energy into it. There may already be someone working on something you want (and maybe you can help him or her!), or there may be an existing solution for it elsewhere.


### Test your code

I think testing your code is important, and I try to test Glimpse as best as I can.  If you make a change that isn't absolutely trivial to determine as correct on casual inspection, please write one or more tests to show that it works as you intend - and don't forget the edge cases. ...and I'm admittedly bad at this, so please fuss at me if I don't follow my own guidelines! :)

Glimpse uses [clojure.test](https://clojure.github.io/clojure/clojure.test-api.html), and I'm willing to incorporate [test.check](https://github.com/clojure/test.check) if anyone wants to use it.


### Sign your commits

If you want to contribute code, that's great and I'd love to have it.  However, I also need to ensure that what you provide complies with the [EPL](LICENSE) under which this software is distributed.  In order to do that, I ask that you read the [Developer Certificate of Origin](http://developercertificate.org/) and sign off on your commit if the change is significant.  The DCO is listed in its entirety below:

```
Developer Certificate of Origin
Version 1.1

Copyright (C) 2004, 2006 The Linux Foundation and its contributors.
660 York Street, Suite 102,
San Francisco, CA 94110 USA

Everyone is permitted to copy and distribute verbatim copies of this
license document, but changing it is not allowed.


Developer's Certificate of Origin 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I
have the right to submit it under the open source license
indicated in the file; or

(b) The contribution is based upon previous work that, to the best
of my knowledge, is covered under an appropriate open source
license and I have the right under that license to submit that
work with modifications, whether created in whole or in part
by me, under the same open source license (unless I am
permitted to submit under a different license), as indicated
in the file; or

(c) The contribution was provided directly to me by some other
person who certified (a), (b) or (c) and I have not modified
it.

(d) I understand and agree that this project and the contribution
are public and that a record of the contribution (including all
personal information I submit with it, including my sign-off) is
maintained indefinitely and may be redistributed consistent with
this project or the open source license(s) involved.
```

If you agree and all the code and/or documentation meets the criteria specified, all you have to do is add a line to your commit:

`Signed-off-by: {Your name} <{Your email}>`

As an example, my sign off would be `Signed-off-by: Chad Taylor <taylor.thomas.c@gmail.com>`.  If you already have your name and email address set in git, you can add this line automatically by using the `-s` switch on `git commit`.


### Submit a pull request

Please [create a pull request](https://help.github.com/articles/creating-a-pull-request/) when you're ready to have your contribution considered for integration into the `master` branch.

Things I will look for in a pull request before merging:

1. Does this change affect Glimpse positively?
2. Does the commit message sufficiently cover the changes?
3. If warranted, are there tests to cover the changes, and do all tests pass?
4. Is the code submitted (if any) reasonable and well-factored?
5. Is the commit signed?

If the answer to all of these is yes, I'll merge your pull request into `master`! If not, I'll work with you to get the issues worked out. Thanks in advance for working with me to keep Glimpse as high-quality as we can. :)
