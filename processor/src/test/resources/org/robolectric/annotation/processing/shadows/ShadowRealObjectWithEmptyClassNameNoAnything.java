package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(className="")
public class ShadowRealObjectWithEmptyClassNameNoAnything {

  @RealObject String someField;
}
