package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** A Shadow with a RealObject field that implements an empty class name */
@Implements(className = "")
public class ShadowRealObjectWithEmptyClassName {

  @RealObject String someField;
}
