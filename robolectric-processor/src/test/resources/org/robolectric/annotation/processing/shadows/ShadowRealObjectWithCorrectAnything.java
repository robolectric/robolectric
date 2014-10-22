package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.processing.objects.Dummy;

@Implements(value=Robolectric.Anything.class,
            className="org.robolectric.annotation.processing.objects.Dummy"
            )
public class ShadowRealObjectWithCorrectAnything {

  @RealObject Dummy someField;
}
