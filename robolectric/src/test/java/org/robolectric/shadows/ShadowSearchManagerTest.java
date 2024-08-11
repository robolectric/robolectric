package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowSearchManagerTest {

  @Test
  @Config(minSdk = O)
  public void
      searchManager_activityContextEnabled_differentInstancesRetrieveGlobalSearchActivity() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    Activity activity = null;
    try {
      SearchManager applicationSearchManager =
          RuntimeEnvironment.getApplication().getSystemService(SearchManager.class);
      activity = Robolectric.setupActivity(Activity.class);
      SearchManager activitySearchManager = activity.getSystemService(SearchManager.class);

      assertThat(applicationSearchManager).isNotSameInstanceAs(activitySearchManager);

      ComponentName applicationGlobalSearchActivity =
          applicationSearchManager.getGlobalSearchActivity();
      ComponentName activityGlobalSearchActivity = activitySearchManager.getGlobalSearchActivity();

      assertThat(activityGlobalSearchActivity).isEqualTo(applicationGlobalSearchActivity);
    } finally {
      if (activity != null) {
        activity.finish();
      }
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
