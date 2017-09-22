package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import com.example.objects.Dummy;

@Implements(value=Robolectric.Anything.class,
            className="com.example.objects.Dummy"
            )
public class ShadowRealObjectWithCorrectAnything {

  @RealObject Dummy someField;
}
