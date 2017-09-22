package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import com.example.objects.UniqueDummy;

@Implements(className="com.example.objects.Dummy")
public class ShadowRealObjectWithIncorrectClassName {

  @RealObject UniqueDummy someField;
}
