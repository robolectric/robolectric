package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import com.example.objects.Dummy;

@Implements(className="com.example.objects.Dummy")
public class ShadowRealObjectWithCorrectClassName {

  @RealObject Dummy someField;
}
