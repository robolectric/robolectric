package org.robolectric.annotation.processing.shadows;

import com.example.objects.OuterDummy;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** A Shadow that implements a nested class name */
@Implements(className = "com.example.objects.OuterDummy$InnerDummy")
public class ShadowRealObjectWithNestedClassName {

  @RealObject OuterDummy.InnerDummy someField;
}
