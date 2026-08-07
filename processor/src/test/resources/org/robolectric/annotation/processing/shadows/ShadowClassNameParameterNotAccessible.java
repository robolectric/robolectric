package org.robolectric.annotation.processing.shadows;

import com.example.objects.DummyClassName;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implements;

@Implements(value = DummyClassName.class)
public class ShadowClassNameParameterNotAccessible {
  protected void methodWithParameter(
      @ClassName("com.example.objects.DummyClassName$PackagePrivateNested") Object param) {}
}
