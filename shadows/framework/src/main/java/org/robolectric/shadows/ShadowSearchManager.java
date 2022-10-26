package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SystemApi;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(SearchManager.class)
public class ShadowSearchManager {

  @RealObject private SearchManager searchManager;

  @Implementation
  protected SearchableInfo getSearchableInfo(ComponentName componentName) {
    // Prevent Robolectric from calling through
    return null;
  }

  @Implementation(minSdk = M)
  @SystemApi
  protected void launchAssist(Bundle bundle) {
    Intent intent = new Intent(Intent.ACTION_ASSIST);
    intent.putExtras(bundle);
    getContext().sendBroadcast(intent);
  }

  private Context getContext() {
    return reflector(ReflectorSearchManager.class, searchManager).getContext();
  }

  /** Reflector interface for {@link SearchManager}'s internals. */
  @ForType(SearchManager.class)
  private interface ReflectorSearchManager {

    @Accessor("mContext")
    Context getContext();
  }
}
