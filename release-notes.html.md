---
layout: default
title: Robolectric Release Notes
---

# Release Notes

-----
## Release 0.8 - November 5, 2010
-----

#### Features
- <include> tags apply their attributes to the imported element
- equals(), hashcode(), and toString() can be overridden on all Shadow classes
- Put a link to the Tracker project in the Robolectric User Guide
- Added support for Eclipse

#### Bug Fixes
- ResourceLoader obtained from context, not stored statically
- Instrumented class cache no longer retains stale entries
