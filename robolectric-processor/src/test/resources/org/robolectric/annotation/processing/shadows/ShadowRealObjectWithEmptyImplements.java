package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements
public class ShadowRealObjectWithEmptyImplements {

  @RealObject String someField;
}
