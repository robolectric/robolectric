package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.processing.objects.Dummy;

@Implements(className="org.robolectric.annotation.processing.objects.Dummy")
public class ShadowRealObjectWithCorrectClassName {

  @RealObject Dummy someField;
}
