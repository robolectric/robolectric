---
  layout: default
  title: Robolectric Downloads
---

# Events in the life of the Robolectric project

## November 19, 2010
* Release! Version 0.9.1 is now available (see the [Release Notes](release-notes.html) for details.)
* [JavaDoc](http://pivotal.github.com/robolectric/javadoc/) has been added!

## November 17, 2010
* Release! Version 0.9 is now available (see the [Release Notes](release-notes.html) for details.)
* Made what we hope is our last big breaking API change by encapsulating all of the fields in the Shadow classes. The
snapshot download and RobolectricSample project have been updated to reflect this change.
* The snapshot download and site documentation have been updated to reflect recent changes in the API. We have eliminated
the need to implement the TestHelperInterface and create customized test runners that install them. Instead, most
projects should be able to use the default RobolectricTestRunner. While those projects that need customized test
runner functionality can add it directly onto a RobolectricTestRunner subclass without the need to
create and register a separate TestHelper class.

