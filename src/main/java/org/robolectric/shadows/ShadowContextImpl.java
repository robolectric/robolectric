package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;

@Implements(value = Robolectric.Anything.class, className = ShadowContextImpl.CLASS_NAME)
public class ShadowContextImpl extends ShadowContext {
  public static final String CLASS_NAME = "android.app.ContextImpl";
}
