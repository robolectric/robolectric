package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(value=Robolectric.Anything.class)
public class ShadowRealObjectWithMissingClassName {

  @RealObject String someField;
}
