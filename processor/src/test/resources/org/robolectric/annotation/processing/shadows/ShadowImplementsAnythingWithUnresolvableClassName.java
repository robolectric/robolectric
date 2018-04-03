package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;

/** A class with an unresolvable class name annotation */
@Implements(value = Robolectric.Anything.class, className = "some.Stuff")
public class ShadowImplementsAnythingWithUnresolvableClassName {}
