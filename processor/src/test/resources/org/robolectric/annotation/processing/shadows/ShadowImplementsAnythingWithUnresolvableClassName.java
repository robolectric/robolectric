package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;

@Implements(value = Robolectric.Anything.class,
            className="some.Stuff")
public class ShadowImplementsAnythingWithUnresolvableClassName {
  
}
