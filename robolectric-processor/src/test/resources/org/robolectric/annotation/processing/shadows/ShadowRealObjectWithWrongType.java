package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.processing.objects.Dummy;
import org.robolectric.annotation.processing.objects.UniqueDummy;

@Implements(Dummy.class)
public class ShadowRealObjectWithWrongType {

  @RealObject
  UniqueDummy someField;
}
