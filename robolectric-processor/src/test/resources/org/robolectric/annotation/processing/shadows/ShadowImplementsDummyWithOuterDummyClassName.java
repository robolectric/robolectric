package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.processing.objects.Dummy;

@Implements(value = Dummy.class,
            className="org.robolectric.annotation.processing.objects.OuterDummy")
public class ShadowImplementsDummyWithOuterDummyClassName {
  
}
