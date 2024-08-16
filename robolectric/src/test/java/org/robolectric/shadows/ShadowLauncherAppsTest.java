package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IncrementalStatesInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherActivityInfoInternal;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Robolectric test for {@link ShadowLauncherApps}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O_MR1)
public class ShadowLauncherAppsTest {
  private static final String TEST_PACKAGE_NAME = "test-package";
  private static final String TEST_PACKAGE_NAME_2 = "test-package2";
  private static final String TEST_PACKAGE_NAME_3 = "test-package3";
  private static final UserHandle USER_HANDLE = UserHandle.CURRENT;
  private LauncherApps launcherApps;

  private static class DefaultCallback extends LauncherApps.Callback {
    @Override
    public void onPackageRemoved(String packageName, UserHandle user) {}

    @Override
    public void onPackageAdded(String packageName, UserHandle user) {}

    @Override
    public void onPackageChanged(String packageName, UserHandle user) {}

    @Override
    public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {}

    @Override
    public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {}
  }

  @Before
  public void setup() {
    launcherApps =
        (LauncherApps)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.LAUNCHER_APPS_SERVICE);
  }

  @ForType(ShortcutInfo.class)
  private interface ReflectorShortcutInfo {
    @Accessor("mPackageName")
    void setPackage(String packageName);
  }

  private ShadowLooper shadowLooper(Looper looper) {
    return Shadow.extract(looper);
  }

  @Test
  public void testIsPackageEnabled() {
    assertThat(launcherApps.isPackageEnabled(TEST_PACKAGE_NAME, USER_HANDLE)).isFalse();

    shadowOf(launcherApps).addEnabledPackage(USER_HANDLE, TEST_PACKAGE_NAME);
    assertThat(launcherApps.isPackageEnabled(TEST_PACKAGE_NAME, USER_HANDLE)).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void getShortcutConfigActivityList_getsShortcutsForPackageName() {
    LauncherActivityInfo launcherActivityInfo1 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME, USER_HANDLE);
    LauncherActivityInfo launcherActivityInfo2 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME_2, USER_HANDLE);
    shadowOf(launcherApps).addShortcutConfigActivity(USER_HANDLE, launcherActivityInfo1);
    shadowOf(launcherApps).addShortcutConfigActivity(USER_HANDLE, launcherActivityInfo2);

    assertThat(launcherApps.getShortcutConfigActivityList(TEST_PACKAGE_NAME, USER_HANDLE))
        .contains(launcherActivityInfo1);
  }

  @Test
  @Config(minSdk = O)
  public void getShortcutConfigActivityList_getsShortcutsForUserHandle() {
    LauncherActivityInfo launcherActivityInfo1 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME, USER_HANDLE);
    LauncherActivityInfo launcherActivityInfo2 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME, UserHandle.of(10));
    shadowOf(launcherApps).addShortcutConfigActivity(USER_HANDLE, launcherActivityInfo1);
    shadowOf(launcherApps).addShortcutConfigActivity(UserHandle.of(10), launcherActivityInfo2);

    assertThat(launcherApps.getShortcutConfigActivityList(TEST_PACKAGE_NAME, UserHandle.of(10)))
        .contains(launcherActivityInfo2);
  }

  @Test
  @Config(minSdk = O)
  public void getShortcutConfigActivityList_packageNull_getsShortcutFromAllPackagesForUser() {
    LauncherActivityInfo launcherActivityInfo1 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME, USER_HANDLE);
    LauncherActivityInfo launcherActivityInfo2 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME_2, USER_HANDLE);
    LauncherActivityInfo launcherActivityInfo3 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME_3, UserHandle.of(10));
    shadowOf(launcherApps).addShortcutConfigActivity(USER_HANDLE, launcherActivityInfo1);
    shadowOf(launcherApps).addShortcutConfigActivity(USER_HANDLE, launcherActivityInfo2);
    shadowOf(launcherApps).addShortcutConfigActivity(UserHandle.of(10), launcherActivityInfo3);

    assertThat(launcherApps.getShortcutConfigActivityList(null, USER_HANDLE))
        .containsExactly(launcherActivityInfo1, launcherActivityInfo2);
  }

  @Test
  @Config(minSdk = L, maxSdk = M)
  public void testGetActivityListPreN() {
    assertThat(launcherApps.getActivityList(TEST_PACKAGE_NAME, USER_HANDLE)).isEmpty();

    ResolveInfo info =
        ShadowResolveInfo.newResolveInfo(TEST_PACKAGE_NAME, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
    LauncherActivityInfo launcherActivityInfo =
        ReflectionHelpers.callConstructor(
            LauncherActivityInfo.class,
            ClassParameter.from(Context.class, ApplicationProvider.getApplicationContext()),
            ClassParameter.from(ResolveInfo.class, info),
            ClassParameter.from(UserHandle.class, USER_HANDLE),
            ClassParameter.from(long.class, System.currentTimeMillis()));
    shadowOf(launcherApps).addActivity(USER_HANDLE, launcherActivityInfo);
    assertThat(launcherApps.getActivityList(TEST_PACKAGE_NAME, USER_HANDLE))
        .contains(launcherActivityInfo);
  }

  @Test
  @Config(minSdk = N)
  public void testGetActivityList() {
    assertThat(launcherApps.getActivityList(TEST_PACKAGE_NAME, USER_HANDLE)).isEmpty();

    LauncherActivityInfo launcherActivityInfo1 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME, USER_HANDLE);

    shadowOf(launcherApps).addActivity(USER_HANDLE, launcherActivityInfo1);
    assertThat(launcherApps.getActivityList(TEST_PACKAGE_NAME, USER_HANDLE))
        .contains(launcherActivityInfo1);
  }

  @Test
  @Config(minSdk = N)
  public void testGetActivityList_packageNull_getsActivitiesFromAllPackagesForUser() {
    LauncherActivityInfo launcherActivityInfo1 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME, USER_HANDLE);
    LauncherActivityInfo launcherActivityInfo2 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME_2, USER_HANDLE);
    LauncherActivityInfo launcherActivityInfo3 =
        createLauncherActivityInfo(TEST_PACKAGE_NAME_3, UserHandle.of(10));
    shadowOf(launcherApps).addActivity(USER_HANDLE, launcherActivityInfo1);
    shadowOf(launcherApps).addActivity(USER_HANDLE, launcherActivityInfo2);
    shadowOf(launcherApps).addActivity(UserHandle.of(10), launcherActivityInfo3);

    assertThat(launcherApps.getActivityList(null, USER_HANDLE))
        .containsExactly(launcherActivityInfo1, launcherActivityInfo2);
  }

  @Test
  @Config(minSdk = L)
  public void testIsActivityEnabled() {
    ComponentName c1 = new ComponentName(ApplicationProvider.getApplicationContext(), "Activity1");
    ComponentName c2 = new ComponentName(ApplicationProvider.getApplicationContext(), "Activity2");
    ComponentName c3 = new ComponentName("other", "Activity1");
    assertThat(launcherApps.isActivityEnabled(c1, USER_HANDLE)).isFalse();

    shadowOf(launcherApps).setActivityEnabled(USER_HANDLE, c1);
    assertThat(launcherApps.isActivityEnabled(c1, USER_HANDLE)).isTrue();
    assertThat(launcherApps.isActivityEnabled(c2, USER_HANDLE)).isFalse();
    assertThat(launcherApps.isActivityEnabled(c3, USER_HANDLE)).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void testGetApplicationInfo_packageNotFound() throws Exception {
    Throwable throwable =
        assertThrows(
            NameNotFoundException.class,
            () -> launcherApps.getApplicationInfo(TEST_PACKAGE_NAME, 0, USER_HANDLE));

    assertThat(throwable)
        .hasMessageThat()
        .isEqualTo(
            "Package " + TEST_PACKAGE_NAME + " not found for user " + USER_HANDLE.getIdentifier());
  }

  @Test
  public void testGetApplicationInfo_incorrectPackage() throws Exception {
    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.name = "Test app";
    shadowOf(launcherApps).addApplicationInfo(USER_HANDLE, TEST_PACKAGE_NAME_2, applicationInfo);

    Throwable throwable =
        assertThrows(
            NameNotFoundException.class,
            () -> launcherApps.getApplicationInfo(TEST_PACKAGE_NAME, 0, USER_HANDLE));

    assertThat(throwable)
        .hasMessageThat()
        .isEqualTo(
            "Package " + TEST_PACKAGE_NAME + " not found for user " + USER_HANDLE.getIdentifier());
  }

  @Test
  public void testGetApplicationInfo_findsApplicationInfo() throws Exception {
    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.name = "Test app";
    shadowOf(launcherApps).addApplicationInfo(USER_HANDLE, TEST_PACKAGE_NAME, applicationInfo);

    assertThat(launcherApps.getApplicationInfo(TEST_PACKAGE_NAME, 0, USER_HANDLE))
        .isEqualTo(applicationInfo);
  }

  @Test
  public void testCallbackFiresWhenShortcutAddedOrRemoved() throws Exception {
    final Boolean[] wasCalled = new Boolean[] {false};
    final CountDownLatch latch1 = new CountDownLatch(1);
    final CountDownLatch latch2 = new CountDownLatch(2);

    final String packageName = ApplicationProvider.getApplicationContext().getPackageName();

    HandlerThread handlerThread = new HandlerThread("test");
    handlerThread.start();
    try {
      LauncherApps.Callback callback =
          new DefaultCallback() {
            @Override
            public void onShortcutsChanged(
                String packageName, List<ShortcutInfo> shortcuts, UserHandle user) {
              assertEquals(shortcuts.get(0).getPackage(), packageName);
              wasCalled[0] = true;
              latch1.countDown();
              latch2.countDown();
            }
          };
      launcherApps.registerCallback(callback, new Handler(handlerThread.getLooper()));
      shadowOf(launcherApps)
          .addDynamicShortcut(
              new ShortcutInfo.Builder(ApplicationProvider.getApplicationContext(), "ID").build());
      shadowLooper(handlerThread.getLooper()).idle();

      latch1.await(1, TimeUnit.SECONDS);
      assertTrue(wasCalled[0]);

      wasCalled[0] = false;
      launcherApps.pinShortcuts(packageName, new ArrayList<>(), Process.myUserHandle());
      shadowLooper(handlerThread.getLooper()).idle();
      latch2.await(1, TimeUnit.SECONDS);
      assertTrue(wasCalled[0]);
    } finally {
      handlerThread.quit();
    }
  }

  @Test
  public void testGetShortcuts() {
    final ShortcutInfo shortcut1 =
        new ShortcutInfo.Builder(ApplicationProvider.getApplicationContext(), "ID1").build();
    final ShortcutInfo shortcut2 =
        new ShortcutInfo.Builder(ApplicationProvider.getApplicationContext(), "ID2").build();

    shadowOf(launcherApps).addDynamicShortcut(shortcut1);
    shadowOf(launcherApps).addDynamicShortcut(shortcut2);

    assertThat(getPinnedShortcuts(null, null)).containsExactly(shortcut1, shortcut2);
  }

  @Test
  public void testGetShortcutsWithFilters() {
    String myPackage = ApplicationProvider.getApplicationContext().getPackageName();
    String otherPackage = "other";
    ComponentName c1 = new ComponentName(ApplicationProvider.getApplicationContext(), "Activity1");
    ComponentName c2 = new ComponentName(ApplicationProvider.getApplicationContext(), "Activity2");
    ComponentName c3 = new ComponentName(otherPackage, "Activity1");

    final ShortcutInfo shortcut1 =
        new ShortcutInfo.Builder(ApplicationProvider.getApplicationContext(), "ID1")
            .setActivity(c1)
            .build();
    final ShortcutInfo shortcut2 =
        new ShortcutInfo.Builder(ApplicationProvider.getApplicationContext(), "ID2")
            .setActivity(c2)
            .build();
    final ShortcutInfo shortcut3 =
        new ShortcutInfo.Builder(ApplicationProvider.getApplicationContext(), "ID3")
            .setActivity(c3)
            .build();
    reflector(ReflectorShortcutInfo.class, shortcut3).setPackage(otherPackage);

    shadowOf(launcherApps).addDynamicShortcut(shortcut1);
    shadowOf(launcherApps).addDynamicShortcut(shortcut2);
    shadowOf(launcherApps).addDynamicShortcut(shortcut3);

    assertThat(getPinnedShortcuts(otherPackage, null)).containsExactly(shortcut3);
    assertThat(getPinnedShortcuts(myPackage, null)).containsExactly(shortcut1, shortcut2);
    assertThat(getPinnedShortcuts(null, c1)).containsExactly(shortcut1);
    assertThat(getPinnedShortcuts(null, c2)).containsExactly(shortcut2);
    assertThat(getPinnedShortcuts(null, c3)).containsExactly(shortcut3);
  }

  @Test
  public void testHasShortcutHostPermission() {
    shadowOf(launcherApps).setHasShortcutHostPermission(true);
    assertThat(launcherApps.hasShortcutHostPermission()).isTrue();
  }

  @Test
  public void testHasShortcutHostPermission_returnsFalseByDefault() {
    assertThat(launcherApps.hasShortcutHostPermission()).isFalse();
  }

  @Test
  @Config(minSdk = P)
  public void getSuspendedPackageLauncherExtras_returnsBundle() {
    Bundle bundle = new Bundle();
    bundle.putInt("suspended_app", 5);
    shadowOf(launcherApps)
        .addSuspendedPackageLauncherExtras(USER_HANDLE, TEST_PACKAGE_NAME_2, bundle);

    assertThat(launcherApps.getSuspendedPackageLauncherExtras(TEST_PACKAGE_NAME_2, USER_HANDLE))
        .isEqualTo(bundle);
  }

  @Test
  @Config(minSdk = P)
  public void getSuspendedPackageLauncherExtras_returnsEmptyBundle() {
    Throwable throwable =
        assertThrows(
            NameNotFoundException.class,
            () -> launcherApps.getSuspendedPackageLauncherExtras(TEST_PACKAGE_NAME, USER_HANDLE));

    assertThat(throwable)
        .hasMessageThat()
        .isEqualTo(
            "Suspended package extras for  "
                + TEST_PACKAGE_NAME
                + " not found for user "
                + USER_HANDLE.getIdentifier());
  }

  @Test
  @Config(minSdk = Q)
  public void getProfiles_returnsMainProfileByDefault() {
    assertThat(launcherApps.getProfiles()).containsExactly(UserHandle.of(0));
  }

  @Test
  @Config(minSdk = Q)
  public void getProfiles_returnsAllProfiles() {
    UserManager userManager =
        (UserManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.USER_SERVICE);

    shadowOf(userManager).addProfile(UserHandle.myUserId(), 10, "profile10", /* profileFlags= */ 0);
    shadowOf(userManager).addProfile(UserHandle.myUserId(), 11, "profile11", /* profileFlags= */ 0);

    assertThat(launcherApps.getProfiles())
        .containsExactly(UserHandle.of(0), UserHandle.of(10), UserHandle.of(11));
  }

  private List<ShortcutInfo> getPinnedShortcuts(String packageName, ComponentName activity) {
    ShortcutQuery query = new ShortcutQuery();
    query.setQueryFlags(ShortcutQuery.FLAG_MATCH_DYNAMIC | ShortcutQuery.FLAG_MATCH_PINNED);
    query.setPackage(packageName);
    query.setActivity(activity);
    return launcherApps.getShortcuts(query, Process.myUserHandle());
  }

  private LauncherActivityInfo createLauncherActivityInfo(
      String packageName, UserHandle userHandle) {
    ActivityInfo info = new ActivityInfo();
    info.packageName = packageName;
    info.name = packageName;
    info.nonLocalizedLabel = packageName;
    if (RuntimeEnvironment.getApiLevel() <= R) {
      return ReflectionHelpers.callConstructor(
          LauncherActivityInfo.class,
          ClassParameter.from(Context.class, ApplicationProvider.getApplicationContext()),
          ClassParameter.from(ActivityInfo.class, info),
          ClassParameter.from(UserHandle.class, userHandle));
    } else if (RuntimeEnvironment.getApiLevel() <= TIRAMISU) {
      LauncherActivityInfoInternal launcherActivityInfoInternal =
          ReflectionHelpers.callConstructor(
              LauncherActivityInfoInternal.class,
              ClassParameter.from(ActivityInfo.class, info),
              ClassParameter.from(IncrementalStatesInfo.class, null));

      return ReflectionHelpers.callConstructor(
          LauncherActivityInfo.class,
          ClassParameter.from(Context.class, ApplicationProvider.getApplicationContext()),
          ClassParameter.from(UserHandle.class, userHandle),
          ClassParameter.from(LauncherActivityInfoInternal.class, launcherActivityInfoInternal));
    } else {

      LauncherActivityInfoInternal launcherActivityInfoInternal =
          ReflectionHelpers.callConstructor(
              LauncherActivityInfoInternal.class,
              ClassParameter.from(ActivityInfo.class, info),
              ClassParameter.from(IncrementalStatesInfo.class, null),
              ClassParameter.from(UserHandle.class, userHandle));

      return ReflectionHelpers.callConstructor(
          LauncherActivityInfo.class,
          ClassParameter.from(Context.class, ApplicationProvider.getApplicationContext()),
          ClassParameter.from(LauncherActivityInfoInternal.class, launcherActivityInfoInternal));
    }
  }
}
