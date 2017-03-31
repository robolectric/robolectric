package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import com.example.objects.Dummy;
import com.example.objects.UniqueDummy;

@Implements(Dummy.class)
public class ShadowRealObjectWithWrongType {

  @RealObject
  UniqueDummy someField;
}
