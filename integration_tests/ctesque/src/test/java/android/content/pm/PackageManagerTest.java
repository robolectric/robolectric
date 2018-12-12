package android.content.pm;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_SERVICES;
import static android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SdkSuppress;
import androidx.test.runner.AndroidJUnit4;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestService;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Compatibility test for {@link PackageManager} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public final class PackageManagerTest {
  private Context context;
  private PackageManager pm;

  @Before
  public void setup() throws Exception {
    context = InstrumentationRegistry.getTargetContext();
    pm = context.getPackageManager();
  }

  @After
  public void tearDown() {
    pm.setApplicationEnabledSetting(
        context.getPackageName(), COMPONENT_ENABLED_STATE_DEFAULT, DONT_KILL_APP);
    pm.setComponentEnabledSetting(
        new ComponentName(context, "org.robolectric.TestActivity"),
        COMPONENT_ENABLED_STATE_DEFAULT,
        DONT_KILL_APP);
    pm.setComponentEnabledSetting(
        new ComponentName(context, "org.robolectric.DisabledTestActivity"),
        COMPONENT_ENABLED_STATE_DEFAULT,
        DONT_KILL_APP);
  }

  @Test
  @Config(minSdk = O)
  @SdkSuppress(minSdkVersion = O)
  public void isInstantApp_shouldNotBlowup() {
    assertThat(context.getPackageManager().isInstantApp()).isFalse();
  }

  @Test
  public void getPackageInfo() throws Exception {
    if (inRobolectric()) {
      // Doesn't work properly yet
      return;
    }
    PackageInfo info =
        pm.getPackageInfo(
            context.getPackageName(), MATCH_DISABLED_COMPONENTS | GET_ACTIVITIES | GET_SERVICES);

    assertThat(info.activities).hasLength(2);
    assertThat(info.services).hasLength(1);

    assertThat(info.activities[0].name).isEqualTo("org.robolectric.TestActivity");
    assertThat(info.activities[0].applicationInfo.packageName).isEqualTo("org.robolectric");
    assertThat(info.activities[0].enabled).isTrue();
    assertThat(info.activities[1].name).isEqualTo("org.robolectric.DisabledTestActivity");
    assertThat(info.activities[1].applicationInfo.packageName).isEqualTo("org.robolectric");
    assertThat(info.activities[1].enabled).isFalse();

    assertThat(info.services[0].name).isEqualTo("org.robolectric.TestService");
    assertThat(info.services[0].applicationInfo.packageName).isEqualTo("org.robolectric");
    assertThat(info.services[0].enabled).isTrue();
  }

  @Test
  public void getPackageInfo_noFlagsGetNoComponents() throws Exception {
    if (inRobolectric()) {
      // Doesn't work properly yet
      return;
    }
    PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
    assertThat(info.activities).isNull();
    assertThat(info.services).isNull();
  }

  @Test
  public void getPackageInfo_skipsDisabledComponents() throws Exception {
    if (inRobolectric()) {
      // Doesn't work properly yet
      return;
    }
    PackageInfo info = pm.getPackageInfo(context.getPackageName(), GET_ACTIVITIES);

    assertThat(info.activities).hasLength(1);
    assertThat(info.activities[0].name).isEqualTo("org.robolectric.TestActivity");
  }

  @Test
  // Note that this doesn't work for activities as activities in Robolectric are created on demand.
  public void getComponent_partialName() throws Exception {
    ComponentName activityName = new ComponentName(context, ".TestService");

    try {
      pm.getServiceInfo(activityName, 0);
      fail("Expected NameNotFoundException");
    } catch (NameNotFoundException expected) {
    }
  }

  @Test
  public void getComponent_validName() throws Exception {
    ComponentName componentName = new ComponentName(context, "org.robolectric.TestService");
    ServiceInfo info = pm.getServiceInfo(componentName, 0);

    assertThat(info).isNotNull();
  }

  @Test
  public void getComponent_validName_queryWithMoreFlags() throws Exception {
    ComponentName componentName = new ComponentName(context, "org.robolectric.TestService");
    ServiceInfo info = pm.getServiceInfo(componentName, MATCH_DISABLED_COMPONENTS);

    assertThat(info).isNotNull();
  }

  @Test
  public void queryIntentServices_noFlags() throws Exception {
    List<ResolveInfo> result = pm.queryIntentServices(new Intent(context, TestService.class), 0);

    assertThat(result).hasSize(1);
  }

  @Test
  public void getCompoent_disabledComponent_doesntInclude() throws Exception {
    if (inRobolectric()) {
      // Doesn't work properly yet
      return;
    }
    ComponentName disabledActivityName =
        new ComponentName(context, "org.robolectric.DisabledTestActivity");

    try {
      pm.getActivityInfo(disabledActivityName, 0);
      fail("NameNotFoundException expected");
    } catch (NameNotFoundException expected) {
    }
  }

  @Test
  public void getCompoent_disabledComponent_include() throws Exception {
    ComponentName disabledActivityName =
        new ComponentName(context, "org.robolectric.DisabledTestActivity");

    ActivityInfo info = pm.getActivityInfo(disabledActivityName, MATCH_DISABLED_COMPONENTS);
    assertThat(info).isNotNull();
    assertThat(info.enabled).isFalse();
  }

  @Test
  public void getPackageInfo_programmaticallyDisabledComponent_noFlags_notReturned()
      throws Exception {
    if (inRobolectric()) {
      // Doesn't work properly yet
      return;
    }
    ComponentName activityName = new ComponentName(context, "org.robolectric.TestActivity");
    pm.setComponentEnabledSetting(activityName, COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP);

    try {
      pm.getActivityInfo(activityName, 0);
      fail("NameNotFoundException expected");
    } catch (NameNotFoundException expected) {
    }
  }

  @Test
  public void getPackageInfo_programmaticallyDisabledComponent_withFlags_returned()
      throws Exception {
    ComponentName activityName = new ComponentName(context, "org.robolectric.TestActivity");
    pm.setComponentEnabledSetting(activityName, COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP);

    ActivityInfo info = pm.getActivityInfo(activityName, MATCH_DISABLED_COMPONENTS);
    assertThat(info).isNotNull();
    // WHAT?? Seems like we always get the manifest value for ComponentInfo.enabled
    assertThat(info.enabled).isTrue();
    assertThat(info.isEnabled()).isTrue();
  }

  @Test
  public void getPackageInfo_programmaticallyEnabledComponent_returned() throws Exception {
    ComponentName activityName = new ComponentName(context, "org.robolectric.DisabledTestActivity");
    pm.setComponentEnabledSetting(activityName, COMPONENT_ENABLED_STATE_ENABLED, DONT_KILL_APP);

    ActivityInfo info = pm.getActivityInfo(activityName, 0);
    assertThat(info).isNotNull();
    // WHAT?? Seems like we always get the manifest value for ComponentInfo.enabled
    assertThat(info.enabled).isFalse();
    assertThat(info.isEnabled()).isFalse();
  }

  @Test
  @Config(maxSdk = 23)
  @SdkSuppress(maxSdkVersion = 23)
  public void getPackageInfo_disabledAplication_stillReturned_below24() throws Exception {
    if (inRobolectric()) {
      // Doesn't work properly yet
      return;
    }
    pm.setApplicationEnabledSetting(
        context.getPackageName(), COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP);

    PackageInfo packageInfo =
        pm.getPackageInfo(context.getPackageName(), GET_SERVICES | GET_ACTIVITIES);

    assertThat(packageInfo.applicationInfo.enabled).isFalse();

    // Seems that although disabled app makes everything disabled it is still returned with its
    // manifest state below API 23
    assertThat(packageInfo.activities).hasLength(1);
    assertThat(packageInfo.services).hasLength(1);

    assertThat(packageInfo.activities[0].enabled).isTrue();
    assertThat(packageInfo.services[0].enabled).isTrue();
    assertThat(packageInfo.activities[0].isEnabled()).isFalse();
    assertThat(packageInfo.services[0].isEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = 24)
  @SdkSuppress(minSdkVersion = 24)
  public void getPackageInfo_disabledAplication_stillReturned_after24() throws Exception {
    if (inRobolectric()) {
      // Doesn't work properly yet
      return;
    }
    pm.setApplicationEnabledSetting(
        context.getPackageName(), COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP);

    PackageInfo packageInfo =
        pm.getPackageInfo(context.getPackageName(), GET_SERVICES | GET_ACTIVITIES);

    assertThat(packageInfo.applicationInfo.enabled).isFalse();

    // seems that since API 24 it is isEnabled() and not enabled that gets something into default
    // result
    assertThat(packageInfo.activities).isNull();
    assertThat(packageInfo.services).isNull();
  }

  @Test
  public void getPackageInfo_disabledAplication_withFlags_returnedEverything() throws Exception {
    if (inRobolectric()) {
      // Doesn't work properly yet
      return;
    }
    pm.setApplicationEnabledSetting(
        context.getPackageName(), COMPONENT_ENABLED_STATE_DISABLED, DONT_KILL_APP);

    PackageInfo packageInfo =
        pm.getPackageInfo(
            context.getPackageName(), GET_SERVICES | GET_ACTIVITIES | MATCH_DISABLED_COMPONENTS);

    assertThat(packageInfo.applicationInfo.enabled).isFalse();
    assertThat(packageInfo.activities).hasLength(2);
    assertThat(packageInfo.services).hasLength(1);
    assertThat(packageInfo.activities[0].enabled).isTrue(); // default enabled flag
  }

  private static boolean inRobolectric() {
    try {
      Class.forName("org.robolectric.RuntimeEnvironment");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
