package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.CrossProfileApps;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.os.UserHandle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowCrossProfileApps.StartedMainActivity;

@RunWith(AndroidJUnit4.class)
@Config(sdk = P)
public class ShadowCrossProfileAppsTest {

  private final Context context = ApplicationProvider.getApplicationContext();
  private final CrossProfileApps crossProfileApps =
      context.getSystemService(CrossProfileApps.class);

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

    ComponentName component = ComponentName.createRelative(context, ".shadows.TestActivityAlias");
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

    ComponentName component = ComponentName.createRelative(context, ".shadows.TestActivity");
    assertThrowsSecurityException(() -> crossProfileApps.startMainActivity(component, userHandle1));
  }

  @Test
  public void startMainActivity_nonExistentActivityShouldThrowSecurityException() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);

    ComponentName component = ComponentName.createRelative(context, ".shadows.FakeTestActivity");
    assertThrowsSecurityException(() -> crossProfileApps.startMainActivity(component, userHandle1));
  }

  @Test
  public void startMainActivity_userNotAvailable_shouldThrowSecurityException() {
    ComponentName component = ComponentName.createRelative(context, ".shadows.TestActivityAlias");
    assertThrowsSecurityException(() -> crossProfileApps.startMainActivity(component, userHandle1));
  }

  @Test
  public void addTargetProfile_currentUserHandle_shouldThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> shadowOf(crossProfileApps).addTargetUserProfile(Process.myUserHandle()));
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
