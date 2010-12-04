---
layout: default
title: Release Notes
---

# Release Notes

-----
## Release 0.9.3 - December 3, 2010
-----

#### Features
- Shadow for <code>AsyncTask</code>
- Integrated contributions for <code>ShadowConnectivityManager</code> and <code>ShadowNetworkInfo</code> - Thanks
macarse!
- A HUGE contribution from the team at [Zoodles](http://www.zoodles.com), the in test SQLite database
(<code>ShadowSQLiteDababase</code>) is backed by h2, reducing the need for database mocking. Thank You Zoodles!
- Robolectric now instantiates applications that are of the type specified in the AndroidManifest.xml - Thanks
Mike Burton!
- The Robolectric jars will now work for projects that use the standard Android SDK (and not the Android Google APIs
version which contains optional APIs such as Google Maps).


#### Bug Fixes
- Windows fixes for path separators
- Windows users can ant build the robolectric library and run the robolectric tests
- Colors that reference other colors
- Robolectric will now work on projects that do not use the Google Maps API and other optional packages.

#### Other
- We have added [Roboguice](http://code.google.com/p/roboguice/) integration to the
[RobolectricSample](http://github.com/pivotal/RobolectricSample) project.
- Support for maven and submission to central is WIP

-----
## Release 0.9.2 - November 24, 2010
-----

#### Features
- Shadow classes for SQLite database (Thanks mportuesi!)
- Lint test for shadow methods that match Android methods but are not annotated with @Implementation
- Shadow support for raw resources (Thanks macarse!)
- Improved quick start documentation

#### Bug Fixes
- Ignore non xml files in layouts directory (Subversion .svn directories were causing cryptic errors, Thanks rlgomes!)

-----
## Release 0.9.1 - November 19, 2010
-----

#### Features
- Integrated support for several new Shadow classes from pull requests -- thank you mportuesi!

#### Bug Fixes
- Fixed JSON "stub!" exceptions.

-----
## Release 0.9 - November 17, 2010
-----

#### Features
- Integrated support for several new Shadow classes from pull requests - Thank you contributors!
- Made the default RobolectricTestRunner robust enough to work without modification for most projects
- Improved the extensibility of RobolectricTestRunner for those projects that do need to add more functionality
- Encapsulated most of the fields on the Shadow classes
- Added support for the android.net.Uri class so that it works the same way in tests that it does in production. This is
a departure from the behavior of the Shadow classes. We did this because Uri is a utility class, and it would be
almost impossible to write a useful Shadow class for it.
- Improved documentation
- Added run configurations to make it easier to get Robolectric working out of the box

#### Bug Fixes
- Fixed support for equals(), hashcode(), and toString()
- Improved the documentation for getting Robolectric to work under Eclipse

-----
## Release 0.8 - November 5, 2010
-----

#### Features
- &lt;include&gt; tags apply their attributes to the imported element
- equals(), hashcode(), and toString() can be overridden on all Shadow classes
- Put a link to the Tracker project in the Robolectric User Guide
- Added support for Eclipse

#### Bug Fixes
- ResourceLoader obtained from context, not stored statically
- Instrumented class cache no longer retains stale entries
