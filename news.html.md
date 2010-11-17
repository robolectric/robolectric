---
  layout: default
  title: Robolectric Downloads
---

# Events in the life of the Robolectric project

## November 17, 2010
* The snapshot download and site documentation have been updated to reflect recent changes in the API. We have eliminated
the need to implement the TestHelperInterface and create customized test runners that install them. Instead, most
projects should be able to use the default RobolectricTestRunner. While those projects that need customized test
runner functionality can add it directly onto a RobolectricTestRunner subclass without the need to
create and register a separate TestHelper class.

