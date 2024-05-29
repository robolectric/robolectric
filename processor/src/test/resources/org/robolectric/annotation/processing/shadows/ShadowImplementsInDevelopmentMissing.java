package org.robolectric.annotation.processing.shadows;

import com.example.objects.Dummy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Dummy.class)
public final class ShadowImplementsInDevelopmentMissing {

  @Implementation
  protected Object doSomething() {
    return null;
  }
}
