package org.robolectric.shadows;

import static android.Manifest.permission.INTERACT_ACROSS_PROFILES;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest.permission;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.CrossProfileApps;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowCrossProfileApps.StartedActivity;
import org.robolectric.shadows.ShadowCrossProfileApps.StartedMainActivity;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public class ShadowCrossProfileAppsTest {
  private final Application application = ApplicationProvider.getApplicationContext();
  private final UserHandle userHandle1 = UserHandle.of(10);
  private final UserHandle userHandle2 = UserHandle.of(11);

  private CrossProfileApps crossProfileApps = application.getSystemService(CrossProfileApps.class);
  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    crossProfileApps = context.getSystemService(CrossProfileApps.class);
  }

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
  @Config(minSdk = R)
  public void startActivityWithIntent_noComponent_throws() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    setPermissions(INTERACT_ACROSS_PROFILES);

    assertThrows(
        IllegalArgumentException.class,
        () -> crossProfileApps.startActivity(new Intent(), userHandle1, /* activity= */ null));
  }

  @Test
  @Config(minSdk = R)
  public void startActivityWithIntent_startActivityContainsIntent() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    setPermissions(INTERACT_ACROSS_PROFILES);
    ComponentName component = ComponentName.createRelative(application, ".shadows.TestActivity");
    Intent intent = new Intent().setComponent(component);

    crossProfileApps.startActivity(intent, userHandle1, /* activity */ null);
    StartedActivity startedActivity = shadowOf(crossProfileApps).peekNextStartedActivity();

    assertThat(startedActivity).isEqualTo(new StartedActivity(component, userHandle1));
    assertThat(startedActivity.getIntent()).isSameInstanceAs(intent);
    assertThat(startedActivity.getActivity()).isNull();
    assertThat(startedActivity.getOptions()).isNull();
  }

  @Test
  @Config(minSdk = R)
  public void startActivityWithIntentAndOptions_startActivityContainsIntentAndOptions() {
    shadowOf(crossProfileApps).addTargetUserProfile(userHandle1);
    setPermissions(INTERACT_ACROSS_PROFILES);
    ComponentName component = ComponentName.createRelative(application, ".shadows.TestActivity");
    Intent intent = new Intent().setComponent(component);
    Activity activity = new Activity();
    Bundle options = new Bundle();

    crossProfileApps.startActivity(intent, userHandle1, activity, options);
    StartedActivity startedActivity = shadowOf(crossProfileApps).peekNextStartedActivity();

    assertThat(startedActivity).isEqualTo(new StartedActivity(component, userHandle1));
    assertThat(startedActivity.getIntent()).isSameInstanceAs(intent);
    assertThat(startedActivity.getActivity()).isSameInstanceAs(activity);
    assertThat(startedActivity.getOptions()).isSameInstanceAs(options);
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

  @Config(minSdk = R)
  @Test
  public void
      canInteractAcrossProfile_withInteractAcrossProfilesPermissionAndProfile_shouldReturnTrue() {
    Shadows.shadowOf(crossProfileApps).addTargetUserProfile(UserHandle.of(10));
    setPermissions(permission.INTERACT_ACROSS_PROFILES);

    assertThat(crossProfileApps.canInteractAcrossProfiles()).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void
      canInteractAcrossProfile_withInteractAcrossUsersPermissionAndProfile_shouldReturnTrue() {
    Shadows.shadowOf(crossProfileApps).addTargetUserProfile(UserHandle.of(10));
    setPermissions(permission.INTERACT_ACROSS_USERS);

    assertThat(crossProfileApps.canInteractAcrossProfiles()).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void
      canInteractAcrossProfile_withInteractAcrossUsersFullPermissionAndProfile_shouldReturnTrue() {
    Shadows.shadowOf(crossProfileApps).addTargetUserProfile(UserHandle.of(10));
    setPermissions(permission.INTERACT_ACROSS_USERS_FULL);

    assertThat(crossProfileApps.canInteractAcrossProfiles()).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void canInteractAcrossProfile_withAppOpsOnly_shouldReturnTrue() {
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.addTargetUserProfile(UserHandle.of(10));
    shadowCrossProfileApps.setInteractAcrossProfilesAppOp(AppOpsManager.MODE_ALLOWED);

    assertThat(crossProfileApps.canInteractAcrossProfiles()).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void canInteractAcrossProfile_withoutPermission_shouldReturnFalse() {
    Shadows.shadowOf(crossProfileApps).addTargetUserProfile(UserHandle.of(10));

    assertThat(crossProfileApps.canInteractAcrossProfiles()).isFalse();
  }

  @Config(minSdk = R)
  @Test
  public void canInteractAcrossProfile_withoutProfile_shouldReturnFalse() {
    setPermissions(permission.INTERACT_ACROSS_PROFILES);

    assertThat(crossProfileApps.canInteractAcrossProfiles()).isFalse();
  }

  @Config(minSdk = R)
  @Test
  public void setInteractAcrossProfilesAppOp_withoutPermissions_shouldThrowException() {
    try {
      crossProfileApps.setInteractAcrossProfilesAppOp(
          context.getPackageName(), AppOpsManager.MODE_ALLOWED);
      fail("Should throw SecurityException");
    } catch (SecurityException ex) {
      // Exactly what we would expect!
    }
  }

  @Config(minSdk = R)
  @Test
  public void setInteractAcrossProfilesAppOp_withPermissions_shouldChangeAppOpsAndSendBroadcast() {
    AtomicBoolean receivedBroadcast = new AtomicBoolean(false);
    context.registerReceiver(
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            if (CrossProfileApps.ACTION_CAN_INTERACT_ACROSS_PROFILES_CHANGED.equals(
                intent.getAction())) {
              receivedBroadcast.set(true);
            }
          }
        },
        new IntentFilter(CrossProfileApps.ACTION_CAN_INTERACT_ACROSS_PROFILES_CHANGED));
    Shadows.shadowOf(crossProfileApps).addTargetUserProfile(UserHandle.of(10));
    setPermissions(permission.INTERACT_ACROSS_USERS, permission.CONFIGURE_INTERACT_ACROSS_PROFILES);
    crossProfileApps.setInteractAcrossProfilesAppOp(
        context.getPackageName(), AppOpsManager.MODE_ALLOWED);
    // Remove permissions, or canInteractAcrossProfiles will return true without the AppOps.
    setPermissions();
    assertThat(crossProfileApps.canInteractAcrossProfiles()).isTrue();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(receivedBroadcast.get()).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void setInteractAcrossProfilesAppOp_onShadow_shouldChangeAppOpsAndSendBroadcast() {
    AtomicBoolean receivedBroadcast = new AtomicBoolean(false);
    context.registerReceiver(
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            if (CrossProfileApps.ACTION_CAN_INTERACT_ACROSS_PROFILES_CHANGED.equals(
                intent.getAction())) {
              receivedBroadcast.set(true);
            }
          }
        },
        new IntentFilter(CrossProfileApps.ACTION_CAN_INTERACT_ACROSS_PROFILES_CHANGED));
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.addTargetUserProfile(UserHandle.of(10));
    shadowCrossProfileApps.setInteractAcrossProfilesAppOp(AppOpsManager.MODE_ALLOWED);
    assertThat(crossProfileApps.canInteractAcrossProfiles()).isTrue();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(receivedBroadcast.get()).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void canRequestInteractAcrossProfile_withPermission_withTargetProfile_shouldReturnTrue() {
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.setHasRequestedInteractAcrossProfiles(true);
    shadowCrossProfileApps.addTargetUserProfile(UserHandle.of(10));

    assertThat(crossProfileApps.canRequestInteractAcrossProfiles()).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void canRequestInteractAcrossProfile_withPermission_withoutTarget_shouldReturnFalse() {
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.setHasRequestedInteractAcrossProfiles(true);

    assertThat(crossProfileApps.canRequestInteractAcrossProfiles()).isFalse();
  }

  @Config(minSdk = R)
  @Test
  public void canRequestInteractAcrossProfile_withoutPermission_withTarget_shouldReturnFalse() {
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.addTargetUserProfile(UserHandle.of(10));

    assertThat(crossProfileApps.canRequestInteractAcrossProfiles()).isFalse();
  }

  @Config(minSdk = R)
  @Test
  public void canRequestInteractAcrossProfile_withoutPermissionOrTarget_shouldReturnFalse() {
    assertThat(crossProfileApps.canRequestInteractAcrossProfiles()).isFalse();
  }

  @Config(minSdk = R)
  @Test
  public void canRequestInteractAcrossProfile_withTarget_requestedAppOp_shouldReturnTrue() {
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.addTargetUserProfile(UserHandle.of(10));
    shadowCrossProfileApps.setInteractAcrossProfilesAppOp(AppOpsManager.MODE_ALLOWED);

    assertThat(crossProfileApps.canRequestInteractAcrossProfiles()).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void
      createRequestInteractAcrossProfilesIntent_withPermissionAndTarget_shouldReturnRecognisedIntent() {
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.addTargetUserProfile(UserHandle.of(10));
    shadowCrossProfileApps.setInteractAcrossProfilesAppOp(AppOpsManager.MODE_ALLOWED);

    Intent intent = crossProfileApps.createRequestInteractAcrossProfilesIntent();

    assertThat(shadowCrossProfileApps.isRequestInteractAcrossProfilesIntent(intent)).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void createRequestInteractAcrossProfilesIntent_withoutPermission_shouldThrowException() {
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.addTargetUserProfile(UserHandle.of(10));

    try {
      crossProfileApps.createRequestInteractAcrossProfilesIntent();
      fail("SecurityException was expected.");
    } catch (SecurityException ex) {
      // Exactly what we would expect!
    }
  }

  @Config(minSdk = R)
  @Test
  public void createRequestInteractAcrossProfilesIntent_withoutTarget_shouldThrowException() {
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.setInteractAcrossProfilesAppOp(AppOpsManager.MODE_ALLOWED);

    try {
      crossProfileApps.createRequestInteractAcrossProfilesIntent();
      fail("SecurityException was expected.");
    } catch (SecurityException ex) {
      // Exactly what we would expect!
    }
  }

  @Config(minSdk = R)
  @Test
  public void isRequestInteractAcrossProfilesIntent_fromBadIntents_shouldReturnFalse() {
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);

    assertThat(shadowCrossProfileApps.isRequestInteractAcrossProfilesIntent(new Intent()))
        .isFalse();
    assertThat(
            shadowCrossProfileApps.isRequestInteractAcrossProfilesIntent(
                new Intent(Intent.ACTION_SHOW_APP_INFO)))
        .isFalse();
  }

  @Ignore("Requires an exported activity in a manifest")
  @Config(minSdk = R)
  @Test
  public void startActivity_withAppOps_shouldStartActivityForUser() {
    UserHandle handle = UserHandle.of(10);
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.addTargetUserProfile(handle);
    shadowCrossProfileApps.setInteractAcrossProfilesAppOp(AppOpsManager.MODE_ALLOWED);

    ComponentName componentName =
        ComponentName.createRelative(context, ".ShadowCrossProfileAppRTest$TestActivity");
    crossProfileApps.startActivity(componentName, handle);

    assertThat(shadowCrossProfileApps.peekNextStartedActivity())
        .isEqualTo(new StartedActivity(componentName, handle));
  }

  @Config(minSdk = R)
  @Test
  public void startActivity_withoutAppOps_shouldThrowException() {
    UserHandle handle = UserHandle.of(10);
    ShadowCrossProfileApps shadowCrossProfileApps = Shadow.extract(crossProfileApps);
    shadowCrossProfileApps.addTargetUserProfile(handle);
    shadowCrossProfileApps.setInteractAcrossProfilesAppOp(AppOpsManager.MODE_ERRORED);

    ComponentName componentName =
        ComponentName.createRelative(context, ".ShadowCrossProfileAppRTest$TestActivity");

    try {
      crossProfileApps.startActivity(componentName, handle);
      fail("SecurityException was expected.");
    } catch (SecurityException ex) {
      // Exactly what we would expect!
    }
  }

  private void setPermissions(String... permissions) {
    PackageInfo packageInfo =
        shadowOf(context.getPackageManager())
            .getInternalMutablePackageInfo(context.getPackageName());
    packageInfo.requestedPermissions = permissions;
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
