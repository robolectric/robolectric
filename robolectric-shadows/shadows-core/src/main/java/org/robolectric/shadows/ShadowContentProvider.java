package org.robolectric.shadows;

import android.content.ContentProvider;
import android.content.Context;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.content.ContentProvider}.
 */
@Implements(ContentProvider.class)
public class ShadowContentProvider {

  @Implementation
  public final Context getContext() {
    return RuntimeEnvironment.application;
  }
}
