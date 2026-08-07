package org.robolectric.annotation.processing.shadows;

import com.example.objects.DummyClassName;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implements;

@Implements(value = DummyClassName.class)
public class ShadowClassNameParameterNestedExisting {
  protected void methodWithParameter(
      @ClassName("com.example.objects.DummyClassName$PublicNested") Object param) {}
}
