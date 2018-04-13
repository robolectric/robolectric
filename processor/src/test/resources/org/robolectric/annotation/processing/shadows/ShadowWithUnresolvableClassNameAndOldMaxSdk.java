package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;

/** A Shadow that implements an unresolvable class name and an old max SDK */
@Implements(className = "some.Stuff", maxSdk = 21)
public class ShadowWithUnresolvableClassNameAndOldMaxSdk {}
