package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import com.example.objects.Dummy;

@Implements(Dummy.class)
public class ShadowRealObjectWithCorrectType {

  @RealObject Dummy someField;
}
