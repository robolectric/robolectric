package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.processing.objects.Dummy;

@Implements(className="some.Stuff", maxSdk = 21)
public class ShadowImplementsAnythingWithUnresolvableClassNameAndOldMaxSdk {
  
}
