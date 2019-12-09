package org.robolectric.shadows;

import static android.Manifest.permission.INTERACT_ACROSS_PROFILES;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.CrossProfileApps;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.os.UserHandle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowCrossProfileApps.StartedActivity;
import org.robolectric.shadows.ShadowCrossProfileApps.StartedMainActivity;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public class ShadowCrossProfileAppsTest {

  private final Application application = ApplicationProvider.getApplicationContext();
  private final CrossProfileApps crossProfileApps =
      application.getSystemService(CrossProfileApps.class);

  private final UserHandle userHandle1 = UserHandle.of(10);
  private final UserHandle userHandle2 = UserHandle.of(11);

  @Test
  public void getTargetUserProfiles_noProfilesAdded_shouldReturnEmpty() {
    assertThat(crossProfileApps.getTargetUserProfiles()).isEmpty();
  }

  @Test
  public void getTargetUserProfiles_oneProfileAdded_shouldReturnProfileAdded() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    assertThat(crossProfileApps.getTargetUserProfiles()).containsExactly(userHandle1);
  }

  @Test
  public void getTargetUserProfiles_oneProfileAddedTwice_shouldReturnSingleProfileAdded() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    assertThat(crossProfileApps.getTargetUserProfiles()).containsExactly(userHandle1);
  }

  @Test
  public void getTargetUserProfiles_multipleProfilesAdded_shouldReturnAllProfilesAddedInOrder() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle2);

    assertThat(crossProfileApps.getTargetUserProfiles())
        .containsExactly(userHandle1, userHandle2)
        .inOrder();
  }

  @Test
  public void
      getTargetUserProfiles_multipleProfilesAddedInAlternateOrder_shouldReturnAllProfilesAddedInOrder() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle2);
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    assertThat(crossProfileApps.getTargetUserProfiles())
        .containsExactly(userHandle2, userHandle1)
        .inOrder();
  }

  @Test
  public void
      getTargetUserProfiles_multipleProfilesAddedAndFirstRemoved_shouldReturnSecondProfile() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle2);
    shadowOf(crossProfileApps).removeTargetUserProfile(userHandle1);

    assertThat(crossProfileApps.getTargetUserProfiles()).containsExactly(userHandle2);
  }

  @Test
  public void getTargetUserProfiles_multipleProfilesAddedAndCleared_shouldReturnEmpty() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle2);
    shadowOf(crossProfileApps).clearTargetUserProfiles();

    assertThat(crossProfileApps.getTargetUserProfiles()).isEmpty();
  }

  @Test
  public void getProfileSwitchingLabel_shouldNotBeEmpty() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    CharSequence label = crossProfileApps.getProfileSwitchingLabel(userHandle1);
    assertThat(label.toString()).isNotEmpty();
  }

  @Test
  public void getProfileSwitchingLabel_shouldBeDifferentForDifferentProfiles() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle2);

    assertThat(crossProfileApps.getProfileSwitchingLabel(userHandle1).toString())
        .isNotEqualTo(crossProfileApps.getProfileSwitchingLabel(userHandle2).toString());
  }

  @Test
  public void getProfileSwitchingLabel_userNotAvailable_shouldThrowSecurityException() {
    assertThrowsSecurityException(() -> crossProfileApps.getProfileSwitchingLabel(userHandle1));
  }

  @Test
  public void getProfileSwitchingIconDrawable_shouldNotBeNull() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    Drawable icon = crossProfileApps.getProfileSwitchingIconDrawable(userHandle1);
    assertThat(icon).isNotNull();
  }

  @Test
  public void getProfileSwitchingIconDrawable_shouldBeDifferentForDifferentProfiles() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle2);

    assertThat(crossProfileApps.getProfileSwitchingIconDrawable(userHandle1))
        .isNotEqualTo(crossProfileApps.getProfileSwitchingIconDrawable(userHandle2));
  }

  @Test
  public void getProfileSwitchingIconDrawable_userNotAvailable_shouldThrowSecurityException() {
    assertThrowsSecurityException(
        () -> crossProfileApps.getProfileSwitchingIconDrawable(userHandle1));
  }

  @Test
  public void startMainActivity_launcherActivityInManifest_shouldSucceed() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    ComponentName component =
        ComponentName.createRelative(application, ".shadows.TestActivityAlias");
    crossProfileApps.startMainActivity(component, userHandle1);

    StartedActivity startedActivity = shadowOf(crossProfileApps).peekNextStartedActivity();
    assertThat(startedActivity.getComponentName()).isEqualTo(component);
    assertThat(startedActivity.getUserHandle()).isEqualTo(userHandle1);
    assertThat(startedActivity).isEqualTo(new StartedActivity(component, userHandle1));
  }

  @Test
  public void
      startMainActivity_launcherActivityInManifest_withoutCrossProfilePermission_shouldSucceed() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(application).denyPermissions(INTERACT_ACROSS_PROFILES);

    ComponentName component =
        ComponentName.createRelative(application, ".shadows.TestActivityAlias");
    crossProfileApps.startMainActivity(component, userHandle1);

    StartedActivity startedActivity = shadowOf(crossProfileApps).peekNextStartedActivity();
    assertThat(startedActivity.getComponentName()).isEqualTo(component);
    assertThat(startedActivity.getUserHandle()).isEqualTo(userHandle1);
    assertThat(startedActivity).isEqualTo(new StartedActivity(component, userHandle1));
  }

  @Test
  public void startMainActivity_launcherActivityInManifest_shouldStillAddStartedMainActivity() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    ComponentName component =
        ComponentName.createRelative(application, ".shadows.TestActivityAlias");
    crossProfileApps.startMainActivity(component, userHandle1);

    StartedMainActivity startedMainActivity =
        shadowOf(crossProfileApps).peekNextStartedMainActivity();
    assertThat(startedMainActivity.getComponentName()).isEqualTo(component);
    assertThat(startedMainActivity.getUserHandle()).isEqualTo(userHandle1);
    assertThat(startedMainActivity).isEqualTo(new StartedMainActivity(component, userHandle1));
  }

  @Test
  public void startMainActivity_nonLauncherActivityInManifest_shouldThrowSecurityException() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    ComponentName component = ComponentName.createRelative(application, ".shadows.TestActivity");
    assertThrowsSecurityException(() -> crossProfileApps.startMainActivity(component, userHandle1));
  }

  @Test
  public void startMainActivity_nonExistentActivity_shouldThrowSecurityException() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    ComponentName component =
        ComponentName.createRelative(application, ".shadows.FakeTestActivity");
    assertThrowsSecurityException(() -> crossProfileApps.startMainActivity(component, userHandle1));
  }

  @Test
  public void startMainActivity_userNotAvailable_shouldThrowSecurityException() {
    ComponentName component =
        ComponentName.createRelative(application, ".shadows.TestActivityAlias");
    assertThrowsSecurityException(() -> crossProfileApps.startMainActivity(component, userHandle1));
  }

  @Test
  @Config(sdk = Q)
  public void startActivity_launcherActivityInManifest_shouldSucceed() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(application).grantPermissions(INTERACT_ACROSS_PROFILES);

    ComponentName component =
        ComponentName.createRelative(application, ".shadows.TestActivityAlias");
    crossProfileApps.startActivity(component, userHandle1);

    StartedActivity startedMainActivity = shadowOf(crossProfileApps).peekNextStartedActivity();
    assertThat(startedMainActivity.getComponentName()).isEqualTo(component);
    assertThat(startedMainActivity.getUserHandle()).isEqualTo(userHandle1);
    assertThat(startedMainActivity).isEqualTo(new StartedActivity(component, userHandle1));
  }

  @Test
  @Config(sdk = Q)
  public void startActivity_nonLauncherActivityInManifest_shouldSucceed() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(application).grantPermissions(INTERACT_ACROSS_PROFILES);

    ComponentName component = ComponentName.createRelative(application, ".shadows.TestActivity");

    crossProfileApps.startActivity(component, userHandle1);

    StartedActivity startedMainActivity = shadowOf(crossProfileApps).peekNextStartedActivity();
    assertThat(startedMainActivity.getComponentName()).isEqualTo(component);
    assertThat(startedMainActivity.getUserHandle()).isEqualTo(userHandle1);
    assertThat(startedMainActivity).isEqualTo(new StartedActivity(component, userHandle1));
  }

  @Test
  @Config(sdk = Q)
  public void startActivity_nonExistentActivity_shouldThrowSecurityException() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(application).grantPermissions(INTERACT_ACROSS_PROFILES);

    ComponentName component =
        ComponentName.createRelative(application, ".shadows.FakeTestActivity");
    assertThrowsSecurityException(() -> crossProfileApps.startActivity(component, userHandle1));
  }

  @Test
  @Config(sdk = Q)
  public void startActivity_userNotAvailable_shouldThrowSecurityException() {
    shadowOf(application).grantPermissions(INTERACT_ACROSS_PROFILES);

    ComponentName component =
        ComponentName.createRelative(application, ".shadows.TestActivityAlias");
    assertThrowsSecurityException(() -> crossProfileApps.startActivity(component, userHandle1));
  }

  @Test
  @Config(sdk = Q)
  public void startActivity_withoutPermission_shouldThrowSecurityException() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    shadowOf(application).denyPermissions(INTERACT_ACROSS_PROFILES);

    ComponentName component = ComponentName.createRelative(application, ".shadows.TestActivity");

    assertThrowsSecurityException(() -> crossProfileApps.startActivity(component, userHandle1));
  }

  @Test
  public void addTargetProfile_currentUserHandle_shouldThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> shadowOf(crossProfileApps).addTargetUserProfile(Process.myUserHandle()));
  }

  @Test
  public void peekNextStartedActivity_activityStarted_shouldReturnAndNotConsumeActivity() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    ComponentName component =
        ComponentName.createRelative(application, ".shadows.TestActivityAlias");
    crossProfileApps.startMainActivity(component, userHandle1);

    StartedActivity startedActivity = shadowOf(crossProfileApps).peekNextStartedActivity();

    assertThat(startedActivity).isEqualTo(new StartedActivity(component, userHandle1));
    assertThat(shadowOf(crossProfileApps).peekNextStartedActivity())
        .isSameInstanceAs(startedActivity);
  }

  @Test
  public void peekNextStartedActivity_activityNotStarted_shouldReturnNull() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    assertThat(shadowOf(crossProfileApps).peekNextStartedActivity()).isNull();
  }

  @Test
  public void getNextStartedActivity_activityStarted_shouldReturnAndConsumeActivity() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    ComponentName component =
        ComponentName.createRelative(application, ".shadows.TestActivityAlias");
    crossProfileApps.startMainActivity(component, userHandle1);

    StartedActivity startedActivity = shadowOf(crossProfileApps).getNextStartedActivity();

    assertThat(startedActivity).isEqualTo(new StartedActivity(component, userHandle1));
    assertThat(shadowOf(crossProfileApps).peekNextStartedActivity()).isNull();
  }

  @Test
  public void getNextStartedActivity_activityNotStarted_shouldReturnNull() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    assertThat(shadowOf(crossProfileApps).getNextStartedActivity()).isNull();
  }

  @Test
  public void
      clearNextStartedActivities_activityStarted_shouldClearReferencesToStartedActivities() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    ComponentName component =
        ComponentName.createRelative(application, ".shadows.TestActivityAlias");
    crossProfileApps.startMainActivity(component, userHandle1);

    shadowOf(crossProfileApps).clearNextStartedActivities();

    assertThat(shadowOf(crossProfileApps).peekNextStartedActivity()).isNull();
  }

  @Test
  public void clearNextStartedActivities_activityNotStarted_shouldBeNoOp() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    shadowOf(crossProfileApps).clearNextStartedActivities();

    assertThat(shadowOf(crossProfileApps).peekNextStartedActivity()).isNull();
  }

  private static void assertThrowsSecurityException(Runnable runnable) {
    assertThrows(SecurityException.class, runnable);
  }

  private static <T extends Throwable> void assertThrows(Class<T> clazz, Runnable runnable) {
    try {
      runnable.run();
    } catch (Throwable t) {
      if (clazz.isInstance(t)) {
        // expected
        return;
      } else {
        fail("did not throw " + clazz.getName() + ", threw " + t + " instead");
      }
    }
    fail("did not throw " + clazz.getName());
  }
}
