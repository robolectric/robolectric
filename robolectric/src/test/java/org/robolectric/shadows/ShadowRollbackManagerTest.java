package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadow.api.Shadow.extract;

import android.app.Activity;
import android.content.Context;
import android.content.rollback.RollbackInfo;
import android.content.rollback.RollbackManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowRollbackManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public final class ShadowRollbackManagerTest {

  private ShadowRollbackManager instance;

  @Before
  public void setUp() {
    instance =
        extract(
            ApplicationProvider.getApplicationContext().getSystemService(RollbackManager.class));
  }

  @Test
  public void getAvailableRollbacks_empty() {
    assertThat(instance.getAvailableRollbacks()).isEmpty();
  }

  @Test
  public void getAvailableRollbacks_withRollbackInfo() throws Exception {
    instance.addAvailableRollbacks((RollbackInfo) createRollbackInfo());
    assertThat(instance.getAvailableRollbacks()).hasSize(1);
  }

  @Test
  public void getRecentlyCommittedRollbacks_empty() {
    assertThat(instance.getRecentlyCommittedRollbacks()).isEmpty();
  }

  @Test
  public void getRecentlyCommittedRollbacks_withRollbackInfo() throws Exception {
    instance.addRecentlyCommittedRollbacks((RollbackInfo) createRollbackInfo());
    assertThat(instance.getRecentlyCommittedRollbacks()).hasSize(1);
  }

  @Test
  public void getRecentlyCommittedRollbacks_assertListsAreSeparate() throws Exception {
    instance.addAvailableRollbacks((RollbackInfo) createRollbackInfo());
    assertThat(instance.getAvailableRollbacks()).hasSize(1);
    assertThat(instance.getRecentlyCommittedRollbacks()).isEmpty();
  }

  /**
   * Returns a RollbackInfo as Object.
   *
   * <p>Test methods will need to cast this to RollbackInfo. This is necessary, because the
   * TestRunner may not have access to @SystemApi @hide classes like RollbackInfo at initialization.
   */
  private static Object createRollbackInfo() throws Exception {
    return RollbackInfo.class
        .getConstructor(int.class, List.class, boolean.class, List.class, int.class)
        .newInstance(1, ImmutableList.of(), false, ImmutableList.of(), 2);
  }

  @Test
  public void rollbackManager_reloadPersistedData_differentInstancesRetrieveRollbacks() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    Activity activity = null;
    try {
      RollbackManager applicationRollbackManager =
          (RollbackManager)
              RuntimeEnvironment.getApplication().getSystemService(Context.ROLLBACK_SERVICE);
      activity = Robolectric.setupActivity(Activity.class);
      RollbackManager activityRollbackManager =
          (RollbackManager) activity.getSystemService(Context.ROLLBACK_SERVICE);

      assertThat(applicationRollbackManager).isNotSameInstanceAs(activityRollbackManager);

      List<RollbackInfo> applicationAvailableRollbacks =
          applicationRollbackManager.getAvailableRollbacks();
      List<RollbackInfo> activityAvailableRollbacks =
          activityRollbackManager.getAvailableRollbacks();

      assertThat(applicationAvailableRollbacks).isNotNull();
      assertThat(activityAvailableRollbacks).isNotNull();

      assertThat(activityAvailableRollbacks).isEqualTo(applicationAvailableRollbacks);
    } finally {
      if (activity != null) {
        activity.finish();
      }
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
