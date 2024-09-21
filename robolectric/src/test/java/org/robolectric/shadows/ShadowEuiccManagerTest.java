package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.telephony.euicc.EuiccManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/** Junit test for {@link ShadowEuiccManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public class ShadowEuiccManagerTest {
  private EuiccManager euiccManager;

  @Before
  public void setUp() {
    euiccManager = ApplicationProvider.getApplicationContext().getSystemService(EuiccManager.class);
  }

  @Test
  public void isEnabled() {
    shadowOf(euiccManager).setIsEnabled(true);

    assertThat(euiccManager.isEnabled()).isTrue();
  }

  @Test
  public void isEnabled_whenSetToFalse() {
    shadowOf(euiccManager).setIsEnabled(false);

    assertThat(euiccManager.isEnabled()).isFalse();
  }

  @Test
  public void getEid() {
    String eid = "testEid";
    shadowOf(euiccManager).setEid(eid);

    assertThat(euiccManager.getEid()).isEqualTo(eid);
  }

  @Test
  @Config(minSdk = Q)
  public void createForCardId() {
    int cardId = 1;
    EuiccManager mockEuiccManager = mock(EuiccManager.class);

    shadowOf(euiccManager).setEuiccManagerForCardId(cardId, mockEuiccManager);

    assertThat(euiccManager.createForCardId(cardId)).isEqualTo(mockEuiccManager);
  }

  @Test
  public void euiccManager_activityContextEnabled_differentInstancesRetrieveEids() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      EuiccManager applicationEuiccManager =
          (EuiccManager)
              ApplicationProvider.getApplicationContext().getSystemService(Context.EUICC_SERVICE);
      Activity activity = controller.get();
      EuiccManager activityEuiccManager =
          (EuiccManager) activity.getSystemService(Context.EUICC_SERVICE);

      assertThat(applicationEuiccManager).isNotSameInstanceAs(activityEuiccManager);

      String applicationEid = applicationEuiccManager.getEid();
      String activityEid = activityEuiccManager.getEid();

      assertThat(activityEid).isEqualTo(applicationEid);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
