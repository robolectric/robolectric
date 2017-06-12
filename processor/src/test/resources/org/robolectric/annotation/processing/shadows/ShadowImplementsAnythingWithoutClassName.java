package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;

@Implements(value = Robolectric.Anything.class)
public class ShadowImplementsAnythingWithoutClassName {
  
}
