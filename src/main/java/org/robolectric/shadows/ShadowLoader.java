package org.robolectric.shadows;

import android.support.v4.content.Loader;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;

@Implements(Loader.class)
public class ShadowLoader<D> {
  @RealObject private Loader<D> realObject;

  @Implementation
  public void forceLoad() {
    onForceLoad();
  }

  @Implementation
  protected void onForceLoad() {
  }
}
