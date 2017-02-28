package org.robolectric.annotation.processing.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import com.example.objects.Dummy;

@Implements(Dummy.class)
public class ShadowResetterWithParameters {

  
  @Resetter
  public static void resetter_method(String param) {}
}
