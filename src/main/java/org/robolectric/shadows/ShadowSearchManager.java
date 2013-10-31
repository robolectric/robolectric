package org.robolectric.shadows;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;

@Implements(SearchManager.class)
public class ShadowSearchManager {

  @Implementation
  public SearchableInfo getSearchableInfo(ComponentName componentName) {
    // Prevent Robolectric from calling through
    return null;
  }
}
