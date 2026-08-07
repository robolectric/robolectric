package org.robolectric.annotation.processing.shadows;

import com.example.objects.DummyClassName;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implements;

@Implements(value = DummyClassName.class)
public class ShadowClassNameParameterNonExisting {
  protected void methodWithParameter(
      @ClassName("com.example.objects.NonExistingDummy") Object param) {}
}
