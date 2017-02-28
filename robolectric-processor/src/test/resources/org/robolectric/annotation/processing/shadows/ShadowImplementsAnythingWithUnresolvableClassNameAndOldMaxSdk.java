package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;

@Implements(className="some.Stuff", maxSdk = 21)
public class ShadowImplementsAnythingWithUnresolvableClassNameAndOldMaxSdk {
  
}
