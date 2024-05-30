package org.robolectric.annotation.processing.shadows;

import com.example.objects.Dummy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.InDevelopment;

@Implements(Dummy.class)
public final class ShadowImplementsInDevelopment {

  @Implementation
  @InDevelopment
  protected Object doSomething() {
    return null;
  }
}
