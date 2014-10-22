package org.robolectric.shadows;

import android.content.ContentProvider;
import android.content.Context;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(ContentProvider.class)
public class ShadowContentProvider {

  @Implementation
  public final Context getContext() {
    return Robolectric.application;
  }

}
