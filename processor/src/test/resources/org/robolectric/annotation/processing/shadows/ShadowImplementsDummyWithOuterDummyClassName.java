package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import com.example.objects.Dummy;

@Implements(value = Dummy.class,
            className="com.example.objects.OuterDummy")
public class ShadowImplementsDummyWithOuterDummyClassName {
  
}
