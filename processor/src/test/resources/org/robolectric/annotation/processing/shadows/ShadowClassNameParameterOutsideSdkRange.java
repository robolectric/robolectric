package org.robolectric.annotation.processing.shadows;

import com.example.objects.DummyClassName;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implements;

@Implements(value = DummyClassName.class, maxSdk = 1)
public class ShadowClassNameParameterOutsideSdkRange {
  protected void methodWithParameter(@ClassName("com.example.objects.Dummy") Object param) {}
}
