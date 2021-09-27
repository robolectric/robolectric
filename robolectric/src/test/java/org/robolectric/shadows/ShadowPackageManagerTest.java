package org.robolectric.shadows;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.SUSPEND_APPS;
import static android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP;
import static android.content.pm.ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA;
import static android.content.pm.ApplicationInfo.FLAG_ALLOW_TASK_REPARENTING;
import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.content.pm.ApplicationInfo.FLAG_HAS_CODE;
import static android.content.pm.ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_VM_SAFE_MODE;
import static android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.FLAG_PERMISSION_GRANTED_BY_DEFAULT;
import static android.content.pm.PackageManager.FLAG_PERMISSION_SYSTEM_FIXED;
import static android.content.pm.PackageManager.FLAG_PERMISSION_USER_FIXED;
import static android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS;
import static android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.content.pm.PackageManager.SIGNATURE_FIRST_NOT_SIGNED;
import static android.content.pm.PackageManager.SIGNATURE_MATCH;
import static android.content.pm.PackageManager.SIGNATURE_NEITHER_SIGNED;
import static android.content.pm.PackageManager.SIGNATURE_NO_MATCH;
import static android.content.pm.PackageManager.SIGNATURE_SECOND_NOT_SIGNED;
import static android.content.pm.PackageManager.SIGNATURE_UNKNOWN_PACKAGE;
import static android.content.pm.PackageManager.VERIFICATION_ALLOW;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Robolectric.setupActivity;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.Manifest;
import android.Manifest.permission_group;
import android.app.Activity;
import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.ModuleInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.OnPermissionsChangedListener;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PermissionGroup;
import android.content.pm.PackageStats;
import android.content.pm.PathPermission;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.pm.SuspendDialogInfo;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.Process;
import android.provider.DocumentsContract;
import android.telecom.TelecomManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.content.pm.ApplicationInfoBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GetInstallerPackageNameMode;
import org.robolectric.annotation.GetInstallerPackageNameMode.Mode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowPackageManager.PackageSetting;
import org.robolectric.shadows.ShadowPackageManager.ResolveInfoComparator;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.TestUtil;

@RunWith(AndroidJUnit4.class)
public class ShadowPackageManagerTest {

  private static final String TEST_PACKAGE_NAME = "com.some.other.package";
  private static final String TEST_PACKAGE_LABEL = "My Little App";
  private static final String TEST_APP_PATH = "/values/app/application.apk";
  private static final String TEST_PACKAGE2_NAME = "com.a.second.package";
  private static final String TEST_PACKAGE2_LABEL = "A Second App";
  private static final String TEST_APP2_PATH = "/values/app/application2.apk";
  private static final Object USER_ID = 1;
  private static final String REAL_TEST_APP_ASSET_PATH = "assets/exampleapp.apk";
  private static final String REAL_TEST_APP_PACKAGE_NAME = "org.robolectric.exampleapp";
  private static final String TEST_PACKAGE3_NAME = "com.a.third.package";

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Context context;
  private PackageManager packageManager;

  private final ArgumentCaptor<PackageStats> packageStatsCaptor =
      ArgumentCaptor.forClass(PackageStats.class);

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    packageManager = context.getPackageManager();
  }

  @After
  public void tearDown() {
    ShadowPackageManager.reset();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void packageInstallerCreateSession() throws Exception {
    PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));

    PackageInstaller.SessionInfo sessionInfo = packageInstaller.getSessionInfo(sessionId);
    assertThat(sessionInfo.isActive()).isTrue();

    assertThat(sessionInfo.appPackageName).isEqualTo("packageName");

    packageInstaller.abandonSession(sessionId);

    assertThat(packageInstaller.getSessionInfo(sessionId)).isNull();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void packageInstallerOpenSession() throws Exception {
    PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
    int sessionId = packageInstaller.createSession(createSessionParams("packageName"));

    PackageInstaller.Session session = packageInstaller.openSession(sessionId);

    assertThat(session).isNotNull();
  }

  private static PackageInstaller.SessionParams createSessionParams(String appPackageName) {
    PackageInstaller.SessionParams params =
        new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
    params.setAppPackageName(appPackageName);
    return params;
  }

  @Test
  public void packageInstallerAndGetPackageArchiveInfo() {
    ApplicationInfo appInfo = new ApplicationInfo();
    appInfo.flags = ApplicationInfo.FLAG_INSTALLED;
    appInfo.packageName = TEST_PACKAGE_NAME;
    appInfo.sourceDir = TEST_APP_PATH;
    appInfo.name = TEST_PACKAGE_LABEL;

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = appInfo;
    shadowOf(packageManager).installPackage(packageInfo);

    PackageInfo packageInfoResult = packageManager.getPackageArchiveInfo(TEST_APP_PATH, 0);
    assertThat(packageInfoResult).isNotNull();
    ApplicationInfo applicationInfo = packageInfoResult.applicationInfo;
    assertThat(applicationInfo).isInstanceOf(ApplicationInfo.class);
    assertThat(applicationInfo.packageName).isEqualTo(TEST_PACKAGE_NAME);
    assertThat(applicationInfo.sourceDir).isEqualTo(TEST_APP_PATH);
  }

  @Test
  public void applicationFlags() throws Exception {
    int flags = packageManager.getApplicationInfo("org.robolectric", 0).flags;
    assertThat((flags & FLAG_ALLOW_BACKUP)).isEqualTo(FLAG_ALLOW_BACKUP);
    assertThat((flags & FLAG_ALLOW_CLEAR_USER_DATA)).isEqualTo(FLAG_ALLOW_CLEAR_USER_DATA);
    assertThat((flags & FLAG_ALLOW_TASK_REPARENTING)).isEqualTo(FLAG_ALLOW_TASK_REPARENTING);
    assertThat((flags & FLAG_DEBUGGABLE)).isEqualTo(FLAG_DEBUGGABLE);
    assertThat((flags & FLAG_HAS_CODE)).isEqualTo(FLAG_HAS_CODE);
    assertThat((flags & FLAG_RESIZEABLE_FOR_SCREENS)).isEqualTo(FLAG_RESIZEABLE_FOR_SCREENS);
    assertThat((flags & FLAG_SUPPORTS_LARGE_SCREENS)).isEqualTo(FLAG_SUPPORTS_LARGE_SCREENS);
    assertThat((flags & FLAG_SUPPORTS_NORMAL_SCREENS)).isEqualTo(FLAG_SUPPORTS_NORMAL_SCREENS);
    assertThat((flags & FLAG_SUPPORTS_SCREEN_DENSITIES)).isEqualTo(FLAG_SUPPORTS_SCREEN_DENSITIES);
    assertThat((flags & FLAG_SUPPORTS_SMALL_SCREENS)).isEqualTo(FLAG_SUPPORTS_SMALL_SCREENS);
    assertThat((flags & FLAG_VM_SAFE_MODE)).isEqualTo(FLAG_VM_SAFE_MODE);
  }

  /**
   * Tests the permission grants of this test package.
   *
   * <p>These grants are defined in the test package's AndroidManifest.xml.
   */
  @Test
  public void testCheckPermission_thisPackage() throws Exception {
    String thisPackage = context.getPackageName();
    assertEquals(
        PERMISSION_GRANTED,
        packageManager.checkPermission("android.permission.INTERNET", thisPackage));
    assertEquals(
        PERMISSION_GRANTED,
        packageManager.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", thisPackage));
    assertEquals(
        PERMISSION_GRANTED,
        packageManager.checkPermission("android.permission.GET_TASKS", thisPackage));

    assertEquals(
        PERMISSION_DENIED,
        packageManager.checkPermission("android.permission.ACCESS_FINE_LOCATION", thisPackage));
    assertEquals(
        PERMISSION_DENIED,
        packageManager.checkPermission(
            "android.permission.ACCESS_FINE_LOCATION", "random-package"));
  }

  /**
   * Tests the permission grants of other packages. These packages are added to the PackageManager
   * by calling {@link ShadowPackageManager#addPackage}.
   */
  @Test
  public void testCheckPermission_otherPackages() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.requestedPermissions =
        new String[] {"android.permission.INTERNET", "android.permission.SEND_SMS"};
    // Grant one of the permissions.
    packageInfo.requestedPermissionsFlags =
        new int[] {REQUESTED_PERMISSION_GRANTED, 0 /* this permission isn't granted */};
    shadowOf(packageManager).installPackage(packageInfo);

    assertEquals(
        PERMISSION_GRANTED,
        packageManager.checkPermission("android.permission.INTERNET", TEST_PACKAGE_NAME));
    assertEquals(
        PERMISSION_DENIED,
        packageManager.checkPermission("android.permission.SEND_SMS", TEST_PACKAGE_NAME));
    assertEquals(
        PERMISSION_DENIED,
        packageManager.checkPermission("android.permission.READ_SMS", TEST_PACKAGE_NAME));
  }

  /**
   * Tests the permission grants of other packages. These packages are added to the PackageManager
   * by calling {@link ShadowPackageManager#addPackage}.
   */
  @Test
  public void testCheckPermission_otherPackages_grantedByDefault() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.requestedPermissions =
        new String[] {"android.permission.INTERNET", "android.permission.SEND_SMS"};
    shadowOf(packageManager).installPackage(packageInfo);

    // Because we didn't specify permission grant state in the PackageInfo object, all requested
    // permissions are automatically granted. See ShadowPackageManager.grantPermissionsByDefault()
    // for the explanation.
    assertEquals(
        PERMISSION_GRANTED,
        packageManager.checkPermission("android.permission.INTERNET", TEST_PACKAGE_NAME));
    assertEquals(
        PERMISSION_GRANTED,
        packageManager.checkPermission("android.permission.SEND_SMS", TEST_PACKAGE_NAME));
    assertEquals(
        PERMISSION_DENIED,
        packageManager.checkPermission("android.permission.READ_SMS", TEST_PACKAGE_NAME));
  }

  @Test
  @Config(minSdk = M)
  public void testGrantRuntimePermission() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.requestedPermissions =
        new String[] {"android.permission.SEND_SMS", "android.permission.READ_SMS"};
    packageInfo.requestedPermissionsFlags = new int[] {0, 0}; // Not granted by default
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.uid = 12345;
    shadowOf(packageManager).installPackage(packageInfo);

    OnPermissionsChangedListener listener = mock(OnPermissionsChangedListener.class);
    packageManager.addOnPermissionsChangeListener(listener);

    packageManager.grantRuntimePermission(
        TEST_PACKAGE_NAME, "android.permission.SEND_SMS", Process.myUserHandle());

    verify(listener, times(1)).onPermissionsChanged(12345);
    assertThat(packageInfo.requestedPermissionsFlags[0]).isEqualTo(REQUESTED_PERMISSION_GRANTED);
    assertThat(packageInfo.requestedPermissionsFlags[1]).isEqualTo(0);

    packageManager.grantRuntimePermission(
        TEST_PACKAGE_NAME, "android.permission.READ_SMS", Process.myUserHandle());

    verify(listener, times(2)).onPermissionsChanged(12345);
    assertThat(packageInfo.requestedPermissionsFlags[0]).isEqualTo(REQUESTED_PERMISSION_GRANTED);
    assertThat(packageInfo.requestedPermissionsFlags[1]).isEqualTo(REQUESTED_PERMISSION_GRANTED);
  }

  @Test
  @Config(minSdk = M)
  public void testGrantRuntimePermission_packageNotFound() throws Exception {
    try {
      packageManager.grantRuntimePermission(
          "com.unknown.package", "android.permission.SEND_SMS", Process.myUserHandle());
      fail("Exception expected");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = M)
  public void testGrantRuntimePermission_doesntRequestPermission() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.requestedPermissions =
        new String[] {"android.permission.SEND_SMS", "android.permission.READ_SMS"};
    packageInfo.requestedPermissionsFlags = new int[] {0, 0}; // Not granted by default
    shadowOf(packageManager).installPackage(packageInfo);

    try {
      packageManager.grantRuntimePermission(
          // This permission is not granted to the package.
          TEST_PACKAGE_NAME, "android.permission.RECEIVE_SMS", Process.myUserHandle());
      fail("Exception expected");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = M)
  public void testRevokeRuntimePermission() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.requestedPermissions =
        new String[] {"android.permission.SEND_SMS", "android.permission.READ_SMS"};
    packageInfo.requestedPermissionsFlags =
        new int[] {REQUESTED_PERMISSION_GRANTED, REQUESTED_PERMISSION_GRANTED};
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.uid = 12345;
    shadowOf(packageManager).installPackage(packageInfo);

    OnPermissionsChangedListener listener = mock(OnPermissionsChangedListener.class);
    packageManager.addOnPermissionsChangeListener(listener);

    packageManager.revokeRuntimePermission(
        TEST_PACKAGE_NAME, "android.permission.SEND_SMS", Process.myUserHandle());

    verify(listener, times(1)).onPermissionsChanged(12345);
    assertThat(packageInfo.requestedPermissionsFlags[0]).isEqualTo(0);
    assertThat(packageInfo.requestedPermissionsFlags[1]).isEqualTo(REQUESTED_PERMISSION_GRANTED);

    packageManager.revokeRuntimePermission(
        TEST_PACKAGE_NAME, "android.permission.READ_SMS", Process.myUserHandle());

    verify(listener, times(2)).onPermissionsChanged(12345);
    assertThat(packageInfo.requestedPermissionsFlags[0]).isEqualTo(0);
    assertThat(packageInfo.requestedPermissionsFlags[1]).isEqualTo(0);
  }

  @Test
  @Config(minSdk = M)
  public void getPermissionFlags_whenNoPackagePermissionFlagsProvided_returnsZero() {
    // Don't add any permission flags
    int flags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());

    assertThat(flags).isEqualTo(0);
  }

  @Test
  @Config(minSdk = M)
  public void getPermissionFlags_whenPackagePermissionFlagsProvided_returnsPermissionFlags() {
    // Add the SYSTEM_FIXED permission flag
    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED,
        FLAG_PERMISSION_SYSTEM_FIXED,
        Process.myUserHandle());

    int flags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());

    assertThat(flags).isEqualTo(FLAG_PERMISSION_SYSTEM_FIXED);
  }

  @Test
  @Config(minSdk = M)
  public void getPermissionFlags_whenPackagePermissionFlagsProvidedForDiffPermission_returnsZero() {
    // Add the SYSTEM_FIXED permission flag to the READ_SMS permission
    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED,
        FLAG_PERMISSION_SYSTEM_FIXED,
        Process.myUserHandle());

    int flags =
        packageManager.getPermissionFlags(READ_CONTACTS, TEST_PACKAGE_NAME, Process.myUserHandle());

    assertThat(flags).isEqualTo(0);
  }

  @Test
  @Config(minSdk = M)
  public void getPermissionFlags_whenPermissionFlagsProvidedForDifferentPackage_returnsZero() {
    // Add the SYSTEM_FIXED permission flag to the READ_SMS permission for TEST_PACKAGE_NAME
    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED,
        FLAG_PERMISSION_SYSTEM_FIXED,
        Process.myUserHandle());

    int flags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE2_NAME, Process.myUserHandle());

    assertThat(flags).isEqualTo(0);
  }

  @Test
  @Config(minSdk = M)
  public void updatePermissionFlags_whenNoFlagMaskProvided_doesNotUpdateFlags() {
    // Check that we have no permission flags set beforehand
    int oldFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(oldFlags).isEqualTo(0);

    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        /* flagMask= */ 0,
        FLAG_PERMISSION_SYSTEM_FIXED,
        Process.myUserHandle());

    int newFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(newFlags).isEqualTo(0);
  }

  @Test
  @Config(minSdk = M)
  public void updatePermissionFlags_whenPackageHasOnePermissionFlagTurnedOn_updatesFlagToBeOn() {
    // Check that we have no permission flags set beforehand
    int oldFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(oldFlags).isEqualTo(0);

    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED,
        FLAG_PERMISSION_SYSTEM_FIXED,
        Process.myUserHandle());

    int newFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(newFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(FLAG_PERMISSION_SYSTEM_FIXED);
  }

  @Test
  @Config(minSdk = M)
  public void updatePermissionFlags_whenPackageHasOnePermissionFlagTurnedOff_updatesFlagToBeOff() {
    // Check that we have one permission flag set beforehand
    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED,
        FLAG_PERMISSION_SYSTEM_FIXED,
        Process.myUserHandle());
    int oldFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(oldFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(FLAG_PERMISSION_SYSTEM_FIXED);

    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED,
        /* flagValues= */ 0,
        Process.myUserHandle());

    int newFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(newFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(0);
  }

  @Test
  @Config(minSdk = M)
  public void
      updatePermissionFlags_whenPackageHasMultiplePermissionFlagsTurnedOn_updatesFlagsToBeOn() {
    // Check that we have no permission flags set beforehand
    int oldFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(oldFlags).isEqualTo(0);

    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        Process.myUserHandle());

    int newFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(newFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(FLAG_PERMISSION_SYSTEM_FIXED);
    assertThat(newFlags & FLAG_PERMISSION_GRANTED_BY_DEFAULT)
        .isEqualTo(FLAG_PERMISSION_GRANTED_BY_DEFAULT);
  }

  @Test
  @Config(minSdk = M)
  public void
      updatePermissionFlags_whenPackageHasMultiplePermissionFlagsTurnedOff_updatesFlagsToBeOff() {
    // Check that we have one permission flag set beforehand
    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        Process.myUserHandle());
    int oldFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(oldFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(FLAG_PERMISSION_SYSTEM_FIXED);
    assertThat(oldFlags & FLAG_PERMISSION_GRANTED_BY_DEFAULT)
        .isEqualTo(FLAG_PERMISSION_GRANTED_BY_DEFAULT);

    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        /* flagValues= */ 0,
        Process.myUserHandle());

    int newFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(newFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(0);
    assertThat(newFlags & FLAG_PERMISSION_GRANTED_BY_DEFAULT).isEqualTo(0);
  }

  @Test
  @Config(minSdk = M)
  public void
      updatePermissionFlags_whenPackageHasMultiplePermissionFlagsTurnedOn_turnOneFlagOff_onlyAffectsOneFlag() {
    // Check that we have one permission flag set beforehand
    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        Process.myUserHandle());
    int oldFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(oldFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(FLAG_PERMISSION_SYSTEM_FIXED);
    assertThat(oldFlags & FLAG_PERMISSION_GRANTED_BY_DEFAULT)
        .isEqualTo(FLAG_PERMISSION_GRANTED_BY_DEFAULT);

    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED,
        /* flagValues= */ 0,
        Process.myUserHandle());

    int newFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(newFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(0);
    // The GRANTED_BY_DEFAULT flag should be untouched
    assertThat(newFlags & FLAG_PERMISSION_GRANTED_BY_DEFAULT)
        .isEqualTo(FLAG_PERMISSION_GRANTED_BY_DEFAULT);
  }

  @Test
  @Config(minSdk = M)
  public void
      updatePermissionFlags_whenPackageHasMultiplePermissionFlagsTurnedOn_turnDiffFlagOn_doesNotAffectOtherFlags() {
    // Check that we have one permission flag set beforehand
    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        Process.myUserHandle());
    int oldFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(oldFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(FLAG_PERMISSION_SYSTEM_FIXED);
    assertThat(oldFlags & FLAG_PERMISSION_GRANTED_BY_DEFAULT)
        .isEqualTo(FLAG_PERMISSION_GRANTED_BY_DEFAULT);

    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_USER_FIXED,
        FLAG_PERMISSION_USER_FIXED,
        Process.myUserHandle());

    int newFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    // The SYSTEM_FIXED and GRANTED_BY_DEFAULT flags should not be affected
    assertThat(newFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(FLAG_PERMISSION_SYSTEM_FIXED);
    assertThat(newFlags & FLAG_PERMISSION_GRANTED_BY_DEFAULT)
        .isEqualTo(FLAG_PERMISSION_GRANTED_BY_DEFAULT);
    assertThat(newFlags & FLAG_PERMISSION_USER_FIXED).isEqualTo(FLAG_PERMISSION_USER_FIXED);
  }

  @Test
  @Config(minSdk = M)
  public void updatePermissionFlags_forDifferentPermission_doesNotAffectOriginalPermissionFlags() {
    // Check that we have one permission flag set beforehand
    packageManager.updatePermissionFlags(
        READ_SMS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        FLAG_PERMISSION_SYSTEM_FIXED | FLAG_PERMISSION_GRANTED_BY_DEFAULT,
        Process.myUserHandle());
    int oldSmsFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(oldSmsFlags & FLAG_PERMISSION_SYSTEM_FIXED).isEqualTo(FLAG_PERMISSION_SYSTEM_FIXED);
    assertThat(oldSmsFlags & FLAG_PERMISSION_GRANTED_BY_DEFAULT)
        .isEqualTo(FLAG_PERMISSION_GRANTED_BY_DEFAULT);

    packageManager.updatePermissionFlags(
        READ_CONTACTS,
        TEST_PACKAGE_NAME,
        FLAG_PERMISSION_USER_FIXED,
        FLAG_PERMISSION_USER_FIXED,
        Process.myUserHandle());

    int newSmsFlags =
        packageManager.getPermissionFlags(READ_SMS, TEST_PACKAGE_NAME, Process.myUserHandle());
    // Check we haven't changed the permission flags of the READ_SMS permission
    assertThat(oldSmsFlags).isEqualTo(newSmsFlags);
    int contactsFlags =
        packageManager.getPermissionFlags(READ_CONTACTS, TEST_PACKAGE_NAME, Process.myUserHandle());
    assertThat(contactsFlags & FLAG_PERMISSION_USER_FIXED).isEqualTo(FLAG_PERMISSION_USER_FIXED);
  }

  @Test
  public void testQueryBroadcastReceiverSucceeds() {
    Intent intent = new Intent("org.robolectric.ACTION_RECEIVER_PERMISSION_PACKAGE");
    intent.setPackage(context.getPackageName());

    List<ResolveInfo> receiverInfos =
        packageManager.queryBroadcastReceivers(intent, PackageManager.GET_RESOLVED_FILTER);
    assertThat(receiverInfos).isNotEmpty();
    assertThat(receiverInfos.get(0).activityInfo.name)
        .isEqualTo("org.robolectric.ConfigTestReceiverPermissionsAndActions");
    assertThat(receiverInfos.get(0).activityInfo.permission)
        .isEqualTo("org.robolectric.CUSTOM_PERM");
    assertThat(receiverInfos.get(0).filter.getAction(0))
        .isEqualTo("org.robolectric.ACTION_RECEIVER_PERMISSION_PACKAGE");
  }

  @Test
  public void testQueryBroadcastReceiverFailsForMissingPackageName() {
    Intent intent = new Intent("org.robolectric.ACTION_ONE_MORE_PACKAGE");
    List<ResolveInfo> receiverInfos =
        packageManager.queryBroadcastReceivers(intent, PackageManager.GET_RESOLVED_FILTER);
    assertThat(receiverInfos).isEmpty();
  }

  @Test
  public void testQueryBroadcastReceiver_matchAllWithoutIntentFilter() {
    Intent intent = new Intent();
    intent.setPackage(context.getPackageName());
    List<ResolveInfo> receiverInfos =
        packageManager.queryBroadcastReceivers(intent, PackageManager.GET_INTENT_FILTERS);
    assertThat(receiverInfos).hasSize(7);

    for (ResolveInfo receiverInfo : receiverInfos) {
      assertThat(receiverInfo.activityInfo.name)
          .isNotEqualTo("com.bar.ReceiverWithoutIntentFilter");
    }
  }

  @Test
  public void testGetPackageInfo_ForReceiversSucceeds() throws Exception {
    PackageInfo receiverInfos =
        packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_RECEIVERS);

    assertThat(receiverInfos.receivers).isNotEmpty();
    assertThat(receiverInfos.receivers[0].name)
        .isEqualTo("org.robolectric.ConfigTestReceiver.InnerReceiver");
    assertThat(receiverInfos.receivers[0].permission).isEqualTo("com.ignored.PERM");
  }

  private static class ActivityWithConfigChanges extends Activity {}

  @Test
  public void getActivityMetaData_configChanges() throws Exception {
    Activity activity = setupActivity(ShadowPackageManagerTest.ActivityWithConfigChanges.class);

    ActivityInfo activityInfo =
        activity.getPackageManager().getActivityInfo(activity.getComponentName(), 0);

    int configChanges = activityInfo.configChanges;
    assertThat(configChanges & ActivityInfo.CONFIG_SCREEN_LAYOUT)
        .isEqualTo(ActivityInfo.CONFIG_SCREEN_LAYOUT);
    assertThat(configChanges & ActivityInfo.CONFIG_ORIENTATION)
        .isEqualTo(ActivityInfo.CONFIG_ORIENTATION);

    // Spot check a few other possible values that shouldn't be in the flags.
    assertThat(configChanges & ActivityInfo.CONFIG_FONT_SCALE).isEqualTo(0);
    assertThat(configChanges & ActivityInfo.CONFIG_SCREEN_SIZE).isEqualTo(0);
  }

  /** MCC + MNC are always present in config changes since Oreo. */
  @Test
  @Config(minSdk = O)
  public void getActivityMetaData_configChangesAlwaysIncludesMccAndMnc() throws Exception {
    Activity activity = setupActivity(ShadowPackageManagerTest.ActivityWithConfigChanges.class);

    ActivityInfo activityInfo =
        activity.getPackageManager().getActivityInfo(activity.getComponentName(), 0);

    int configChanges = activityInfo.configChanges;
    assertThat(configChanges & ActivityInfo.CONFIG_MCC).isEqualTo(ActivityInfo.CONFIG_MCC);
    assertThat(configChanges & ActivityInfo.CONFIG_MNC).isEqualTo(ActivityInfo.CONFIG_MNC);
  }

  @Test
  public void getPermissionInfo_withMinimalFields() throws Exception {
    PermissionInfo permission =
        packageManager.getPermissionInfo("org.robolectric.permission_with_minimal_fields", 0);
    assertThat(permission.labelRes).isEqualTo(0);
    assertThat(permission.descriptionRes).isEqualTo(0);
    assertThat(permission.protectionLevel).isEqualTo(PermissionInfo.PROTECTION_NORMAL);
  }

  @Test
  public void getPermissionInfo_addedPermissions() throws Exception {
    PermissionInfo permissionInfo = new PermissionInfo();
    permissionInfo.name = "manually_added_permission";
    shadowOf(packageManager).addPermissionInfo(permissionInfo);
    PermissionInfo permission = packageManager.getPermissionInfo("manually_added_permission", 0);
    assertThat(permission.name).isEqualTo("manually_added_permission");
  }

  @Test
  public void getPermissionGroupInfo_fromManifest() throws Exception {
    PermissionGroupInfo permissionGroupInfo =
        context
            .getPackageManager()
            .getPermissionGroupInfo("org.robolectric.package_permission_group", 0);
    assertThat(permissionGroupInfo.name).isEqualTo("org.robolectric.package_permission_group");
  }

  @Test
  public void getPermissionGroupInfo_extraPermissionGroup() throws Exception {
    PermissionGroupInfo newCameraPermission = new PermissionGroupInfo();
    newCameraPermission.name = permission_group.CAMERA;
    shadowOf(packageManager).addPermissionGroupInfo(newCameraPermission);

    assertThat(packageManager.getPermissionGroupInfo(permission_group.CAMERA, 0).name)
        .isEqualTo(newCameraPermission.name);
  }

  @Test
  public void getAllPermissionGroups_fromManifest() throws Exception {
    List<PermissionGroupInfo> allPermissionGroups = packageManager.getAllPermissionGroups(0);
    assertThat(allPermissionGroups).hasSize(1);
    assertThat(allPermissionGroups.get(0).name)
        .isEqualTo("org.robolectric.package_permission_group");
  }

  @Test
  public void getAllPermissionGroups_duplicateInExtraPermissions() throws Exception {
    assertThat(packageManager.getAllPermissionGroups(0)).hasSize(1);

    PermissionGroupInfo overriddenPermission = new PermissionGroupInfo();
    overriddenPermission.name = "org.robolectric.package_permission_group";
    shadowOf(packageManager).addPermissionGroupInfo(overriddenPermission);
    PermissionGroupInfo newCameraPermission = new PermissionGroupInfo();
    newCameraPermission.name = permission_group.CAMERA;
    shadowOf(packageManager).addPermissionGroupInfo(newCameraPermission);

    List<PermissionGroupInfo> allPermissionGroups = packageManager.getAllPermissionGroups(0);
    assertThat(allPermissionGroups).hasSize(2);
  }

  @Test
  public void getAllPermissionGroups_duplicatePermission() throws Exception {
    assertThat(packageManager.getAllPermissionGroups(0)).hasSize(1);

    // Package 1
    Package pkg = new Package(TEST_PACKAGE_NAME);
    ApplicationInfo appInfo = pkg.applicationInfo;
    appInfo.flags = ApplicationInfo.FLAG_INSTALLED;
    appInfo.packageName = TEST_PACKAGE_NAME;
    appInfo.sourceDir = TEST_APP_PATH;
    appInfo.name = TEST_PACKAGE_LABEL;
    PermissionGroupInfo contactsPermissionGroupInfoApp1 = new PermissionGroupInfo();
    contactsPermissionGroupInfoApp1.name = Manifest.permission_group.CONTACTS;
    PermissionGroup contactsPermissionGroupApp1 =
        new PermissionGroup(pkg, contactsPermissionGroupInfoApp1);
    pkg.permissionGroups.add(contactsPermissionGroupApp1);
    PermissionGroupInfo storagePermissionGroupInfoApp1 = new PermissionGroupInfo();
    storagePermissionGroupInfoApp1.name = permission_group.STORAGE;
    PermissionGroup storagePermissionGroupApp1 =
        new PermissionGroup(pkg, storagePermissionGroupInfoApp1);
    pkg.permissionGroups.add(storagePermissionGroupApp1);

    shadowOf(packageManager).addPackageInternal(pkg);

    // Package 2, contains one permission group that is the same
    Package pkg2 = new Package(TEST_PACKAGE2_NAME);
    ApplicationInfo appInfo2 = pkg2.applicationInfo;
    appInfo2.flags = ApplicationInfo.FLAG_INSTALLED;
    appInfo2.packageName = TEST_PACKAGE2_NAME;
    appInfo2.sourceDir = TEST_APP2_PATH;
    appInfo2.name = TEST_PACKAGE2_LABEL;
    PermissionGroupInfo contactsPermissionGroupInfoApp2 = new PermissionGroupInfo();
    contactsPermissionGroupInfoApp2.name = Manifest.permission_group.CONTACTS;
    PermissionGroup contactsPermissionGroupApp2 =
        new PermissionGroup(pkg2, contactsPermissionGroupInfoApp2);
    pkg2.permissionGroups.add(contactsPermissionGroupApp2);
    PermissionGroupInfo calendarPermissionGroupInfoApp2 = new PermissionGroupInfo();
    calendarPermissionGroupInfoApp2.name = permission_group.CALENDAR;
    PermissionGroup calendarPermissionGroupApp2 =
        new PermissionGroup(pkg2, calendarPermissionGroupInfoApp2);
    pkg2.permissionGroups.add(calendarPermissionGroupApp2);

    shadowOf(packageManager).addPackageInternal(pkg2);

    // Make sure that the duplicate permission group does not show up in the list
    // Total list should be: contacts, storage, calendar, "org.robolectric.package_permission_group"
    List<PermissionGroupInfo> allPermissionGroups = packageManager.getAllPermissionGroups(0);
    assertThat(allPermissionGroups).hasSize(4);
  }

  @Test
  public void getPackageArchiveInfo() {
    ApplicationInfo appInfo = new ApplicationInfo();
    appInfo.flags = ApplicationInfo.FLAG_INSTALLED;
    appInfo.packageName = TEST_PACKAGE_NAME;
    appInfo.sourceDir = TEST_APP_PATH;
    appInfo.name = TEST_PACKAGE_LABEL;

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = appInfo;
    shadowOf(packageManager).installPackage(packageInfo);

    PackageInfo packageInfoResult = packageManager.getPackageArchiveInfo(TEST_APP_PATH, 0);
    assertThat(packageInfoResult).isNotNull();
    ApplicationInfo applicationInfo = packageInfoResult.applicationInfo;
    assertThat(applicationInfo).isInstanceOf(ApplicationInfo.class);
    assertThat(applicationInfo.packageName).isEqualTo(TEST_PACKAGE_NAME);
    assertThat(applicationInfo.sourceDir).isEqualTo(TEST_APP_PATH);
  }

  @Test
  public void getPackageArchiveInfo_ApkNotInstalled() throws IOException {
    File testApk = TestUtil.resourcesBaseDir().resolve(REAL_TEST_APP_ASSET_PATH).toFile();

    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(testApk.getAbsolutePath(), 0);

    String resourcesMode = System.getProperty("robolectric.resourcesMode");
    if (resourcesMode != null && resourcesMode.equals("legacy")) {
      assertThat(packageInfo).isNull();
    } else {
      assertThat(packageInfo).isNotNull();
      ApplicationInfo applicationInfo = packageInfo.applicationInfo;
      assertThat(applicationInfo.packageName).isEqualTo(REAL_TEST_APP_PACKAGE_NAME);

      // double-check that Robolectric doesn't consider this package to be installed
      try {
        packageManager.getPackageInfo(packageInfo.packageName, 0);
        Assert.fail("Package not expected to be installed.");
      } catch (NameNotFoundException e) {
        // expected exception
      }
    }
  }

  @Test
  public void getApplicationInfo_ThisApplication() throws Exception {
    ApplicationInfo info = packageManager.getApplicationInfo(context.getPackageName(), 0);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(context.getPackageName());
    assertThat(info.processName).isEqualTo(info.packageName);
  }

  @Test
  public void getApplicationInfo_uninstalledApplication_includeUninstalled() throws Exception {
    shadowOf(packageManager).deletePackage(context.getPackageName());

    ApplicationInfo info =
        packageManager.getApplicationInfo(context.getPackageName(), MATCH_UNINSTALLED_PACKAGES);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(context.getPackageName());
  }

  @Test
  public void getApplicationInfo_uninstalledApplication_dontIncludeUninstalled() throws Exception {
    shadowOf(packageManager).deletePackage(context.getPackageName());

    try {
      packageManager.getApplicationInfo(context.getPackageName(), 0);
      fail("PackageManager.NameNotFoundException not thrown");
    } catch (PackageManager.NameNotFoundException e) {
      // expected
    }
  }

  @Test(expected = PackageManager.NameNotFoundException.class)
  public void getApplicationInfo_whenUnknown_shouldThrowNameNotFoundException() throws Exception {
    try {
      packageManager.getApplicationInfo("unknown_package", 0);
      fail("should have thrown NameNotFoundException");
    } catch (PackageManager.NameNotFoundException e) {
      assertThat(e.getMessage()).contains("unknown_package");
      throw e;
    }
  }

  @Test
  public void getApplicationInfo_OtherApplication() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    shadowOf(packageManager).installPackage(packageInfo);

    ApplicationInfo info = packageManager.getApplicationInfo(TEST_PACKAGE_NAME, 0);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(TEST_PACKAGE_NAME);
    assertThat(packageManager.getApplicationLabel(info).toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void getApplicationInfo_readsValuesFromSetPackageArchiveInfo() {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "some.package.name";
    String archiveFilePath = "some/file/path";
    shadowOf(packageManager).setPackageArchiveInfo(archiveFilePath, packageInfo);

    assertThat(packageManager.getPackageArchiveInfo(archiveFilePath, /* flags= */ 0))
        .isEqualTo(packageInfo);
  }

  @Test
  public void removePackage_shouldHideItFromGetApplicationInfo() {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    shadowOf(packageManager).installPackage(packageInfo);
    shadowOf(packageManager).removePackage(TEST_PACKAGE_NAME);

    try {
      packageManager.getApplicationInfo(TEST_PACKAGE_NAME, 0);
      fail("NameNotFoundException not thrown");
    } catch (NameNotFoundException e) {
      // expected
    }
  }

  @Test
  public void queryIntentActivities_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_APP_ERROR, null);
    i.addCategory(Intent.CATEGORY_APP_BROWSER);

    List<ResolveInfo> activities = packageManager.queryIntentActivities(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  public void queryIntentActivities_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    info.activityInfo = new ActivityInfo();
    info.activityInfo.name = "name";
    info.activityInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager).addResolveInfoForIntent(i, info);

    List<ResolveInfo> activities = packageManager.queryIntentActivities(i, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(2);
    assertThat(activities.get(0).nonLocalizedLabel.toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void queryIntentActivities_ServiceMatch() throws Exception {
    Intent i = new Intent("SomeStrangeAction");

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    info.serviceInfo = new ServiceInfo();
    info.serviceInfo.name = "name";
    info.serviceInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager).addResolveInfoForIntent(i, info);

    List<ResolveInfo> activities = packageManager.queryIntentActivities(i, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).isEmpty();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void queryIntentActivitiesAsUser_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_APP_ERROR, null);
    i.addCategory(Intent.CATEGORY_APP_BROWSER);

    List<ResolveInfo> activities = packageManager.queryIntentActivitiesAsUser(i, 0, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void queryIntentActivitiesAsUser_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    shadowOf(packageManager).addResolveInfoForIntent(i, info);

    List<ResolveInfo> activities = packageManager.queryIntentActivitiesAsUser(i, 0, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(2);
    assertThat(activities.get(0).nonLocalizedLabel.toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void queryIntentActivities_launcher() {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> resolveInfos =
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
    assertThat(resolveInfos).hasSize(1);

    assertThat(resolveInfos.get(0).activityInfo.name)
        .isEqualTo("org.robolectric.shadows.TestActivityAlias");
    assertThat(resolveInfos.get(0).activityInfo.targetActivity)
        .isEqualTo("org.robolectric.shadows.TestActivity");
  }

  @Test
  public void queryIntentActivities_MatchSystemOnly() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    ResolveInfo info1 = ShadowResolveInfo.newResolveInfo(TEST_PACKAGE_LABEL, TEST_PACKAGE_NAME);
    ResolveInfo info2 = ShadowResolveInfo.newResolveInfo("System App", "system.launcher");
    info2.activityInfo.applicationInfo.flags |= ApplicationInfo.FLAG_SYSTEM;
    info2.nonLocalizedLabel = "System App";

    shadowOf(packageManager).addResolveInfoForIntent(i, info1);
    shadowOf(packageManager).addResolveInfoForIntent(i, info2);

    List<ResolveInfo> activities =
        packageManager.queryIntentActivities(i, PackageManager.MATCH_SYSTEM_ONLY);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).nonLocalizedLabel.toString()).isEqualTo("System App");
  }

  @Test
  public void queryIntentActivities_EmptyResultWithNoMatchingImplicitIntents() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);
    i.setDataAndType(Uri.parse("content://testhost1.com:1/testPath/test.jpeg"), "image/jpeg");

    List<ResolveInfo> activities = packageManager.queryIntentActivities(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  public void queryIntentActivities_MatchWithExplicitIntent() throws Exception {
    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.shadows.TestActivity");

    List<ResolveInfo> activities = packageManager.queryIntentActivities(i, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).resolvePackageName).isEqualTo("org.robolectric");
    assertThat(activities.get(0).activityInfo.name)
        .isEqualTo("org.robolectric.shadows.TestActivity");
  }

  @Test
  public void queryIntentActivities_MatchWithImplicitIntents() throws Exception {
    Uri uri = Uri.parse("content://testhost1.com:1/testPath/test.jpeg");
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.addCategory(Intent.CATEGORY_DEFAULT);
    i.setDataAndType(uri, "image/jpeg");

    List<ResolveInfo> activities = packageManager.queryIntentActivities(i, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).resolvePackageName).isEqualTo("org.robolectric");
    assertThat(activities.get(0).activityInfo.name)
        .isEqualTo("org.robolectric.shadows.TestActivity");
  }

  @Test
  public void queryIntentActivities_MatchWithAliasIntents() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> activities = packageManager.queryIntentActivities(i, 0);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).resolvePackageName).isEqualTo("org.robolectric");
    assertThat(activities.get(0).activityInfo.targetActivity)
        .isEqualTo("org.robolectric.shadows.TestActivity");
    assertThat(activities.get(0).activityInfo.name)
        .isEqualTo("org.robolectric.shadows.TestActivityAlias");
  }

  @Test
  public void queryIntentActivities_DisabledComponentExplicitIntent() throws Exception {
    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.shadows.DisabledActivity");

    List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(i, 0);
    assertThat(resolveInfos).isEmpty();
  }

  @Test
  public void queryIntentActivities_MatchDisabledComponents() throws Exception {
    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.shadows.DisabledActivity");

    List<ResolveInfo> resolveInfos =
        packageManager.queryIntentActivities(i, PackageManager.MATCH_DISABLED_COMPONENTS);
    assertThat(resolveInfos).isNotNull();
    assertThat(resolveInfos).hasSize(1);
    assertThat(resolveInfos.get(0).activityInfo.enabled).isFalse();
  }

  @Test
  public void queryIntentActivities_DisabledComponentViaPmExplicitIntent() throws Exception {
    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.shadows.TestActivity");

    ComponentName componentToDisable =
        new ComponentName(context, "org.robolectric.shadows.TestActivity");
    packageManager.setComponentEnabledSetting(
        componentToDisable,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP);

    List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(i, 0);
    assertThat(resolveInfos).isEmpty();
  }

  @Test
  public void queryIntentActivities_DisabledComponentEnabledViaPmExplicitIntent() throws Exception {
    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.shadows.DisabledActivity");

    ComponentName componentToDisable =
        new ComponentName(context, "org.robolectric.shadows.DisabledActivity");
    packageManager.setComponentEnabledSetting(
        componentToDisable,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP);

    List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(i, 0);
    assertThat(resolveInfos).hasSize(1);
    assertThat(resolveInfos.get(0).activityInfo.enabled).isFalse();
  }

  @Test
  public void queryIntentActivities_DisabledComponentViaPmImplicitIntent() throws Exception {
    Uri uri = Uri.parse("content://testhost1.com:1/testPath/test.jpeg");
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.addCategory(Intent.CATEGORY_DEFAULT);
    i.setDataAndType(uri, "image/jpeg");

    ComponentName componentToDisable =
        new ComponentName(context, "org.robolectric.shadows.TestActivity");
    packageManager.setComponentEnabledSetting(
        componentToDisable,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP);

    List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(i, 0);
    assertThat(resolveInfos).isEmpty();
  }

  @Test
  public void queryIntentActivities_MatchDisabledViaPmComponents() throws Exception {
    Uri uri = Uri.parse("content://testhost1.com:1/testPath/test.jpeg");
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.addCategory(Intent.CATEGORY_DEFAULT);
    i.setDataAndType(uri, "image/jpeg");

    ComponentName componentToDisable =
        new ComponentName(context, "org.robolectric.shadows.TestActivity");
    packageManager.setComponentEnabledSetting(
        componentToDisable,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP);

    List<ResolveInfo> resolveInfos =
        packageManager.queryIntentActivities(i, PackageManager.MATCH_DISABLED_COMPONENTS);
    assertThat(resolveInfos).isNotNull();
    assertThat(resolveInfos).hasSize(1);
    assertThat(resolveInfos.get(0).activityInfo.enabled).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryIntentActivities_appHidden_includeUninstalled() {
    String packageName = context.getPackageName();
    packageManager.setApplicationHiddenSettingAsUser(
        packageName, /* hidden= */ true, /* user= */ null);

    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.shadows.TestActivity");

    List<ResolveInfo> activities =
        packageManager.queryIntentActivities(i, MATCH_UNINSTALLED_PACKAGES);
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).resolvePackageName).isEqualTo(packageName);
    assertThat(activities.get(0).activityInfo.name)
        .isEqualTo("org.robolectric.shadows.TestActivity");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryIntentActivities_appHidden_dontIncludeUninstalled() {
    String packageName = context.getPackageName();
    packageManager.setApplicationHiddenSettingAsUser(
        packageName, /* hidden= */ true, /* user= */ null);

    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.shadows.TestActivity");

    assertThat(packageManager.queryIntentActivities(i, /* flags= */ 0)).isEmpty();
  }

  @Test
  public void resolveActivity_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    info.activityInfo = new ActivityInfo();
    info.activityInfo.name = "name";
    info.activityInfo.packageName = TEST_PACKAGE_NAME;
    shadowOf(packageManager).addResolveInfoForIntent(i, info);

    assertThat(packageManager.resolveActivity(i, 0)).isNotNull();
    assertThat(packageManager.resolveActivity(i, 0).activityInfo.name).isEqualTo("name");
    assertThat(packageManager.resolveActivity(i, 0).activityInfo.packageName)
        .isEqualTo(TEST_PACKAGE_NAME);
  }

  @Test
  public void addIntentFilterForComponent() throws Exception {
    ComponentName testComponent = new ComponentName("package", "name");
    IntentFilter intentFilter = new IntentFilter("ACTION");
    intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
    intentFilter.addCategory(Intent.CATEGORY_APP_CALENDAR);

    shadowOf(packageManager).addActivityIfNotPresent(testComponent);
    shadowOf(packageManager).addIntentFilterForActivity(testComponent, intentFilter);
    Intent intent = new Intent();

    intent.setAction("ACTION");
    assertThat(intent.resolveActivity(packageManager)).isEqualTo(testComponent);

    intent.setPackage("package");
    assertThat(intent.resolveActivity(packageManager)).isEqualTo(testComponent);

    intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
    assertThat(intent.resolveActivity(packageManager)).isEqualTo(testComponent);

    intent.putExtra("key", "value");
    assertThat(intent.resolveActivity(packageManager)).isEqualTo(testComponent);

    intent.setData(Uri.parse("content://boo")); // data matches only if it is in the filter
    assertThat(intent.resolveActivity(packageManager)).isNull();

    intent.setData(null).setAction("BOO"); // different action
    assertThat(intent.resolveActivity(packageManager)).isNull();
  }

  @Test
  public void resolveActivity_NoMatch() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName("foo.bar", "No Activity"));
    assertThat(packageManager.resolveActivity(i, 0)).isNull();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void resolveActivityAsUser_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    info.activityInfo = new ActivityInfo();
    info.activityInfo.name = "name";
    info.activityInfo.packageName = TEST_PACKAGE_NAME;
    shadowOf(packageManager).addResolveInfoForIntent(i, info);

    ResolveInfo resolvedActivity =
        ReflectionHelpers.callInstanceMethod(
            packageManager,
            "resolveActivityAsUser",
            ClassParameter.from(Intent.class, i),
            ClassParameter.from(int.class, 0),
            ClassParameter.from(int.class, USER_ID));

    assertThat(resolvedActivity).isNotNull();
    assertThat(resolvedActivity.activityInfo.name).isEqualTo("name");
    assertThat(resolvedActivity.activityInfo.packageName).isEqualTo(TEST_PACKAGE_NAME);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void resolveActivityAsUser_NoMatch() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName("foo.bar", "No Activity"));

    ResolveInfo resolvedActivity =
        ReflectionHelpers.callInstanceMethod(
            packageManager,
            "resolveActivityAsUser",
            ClassParameter.from(Intent.class, i),
            ClassParameter.from(int.class, 0),
            ClassParameter.from(int.class, USER_ID));

    assertThat(resolvedActivity).isNull();
  }

  @Test
  public void queryIntentServices_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> activities = packageManager.queryIntentServices(i, 0);
    assertThat(activities).isEmpty();
  }

  @Test
  public void queryIntentServices_MatchWithExplicitIntent() throws Exception {
    Intent i = new Intent();
    i.setClassName(context, "com.foo.Service");

    List<ResolveInfo> services = packageManager.queryIntentServices(i, 0);
    assertThat(services).isNotNull();
    assertThat(services).hasSize(1);
    assertThat(services.get(0).resolvePackageName).isEqualTo("org.robolectric");
    assertThat(services.get(0).serviceInfo.name).isEqualTo("com.foo.Service");
  }

  @Test
  public void queryIntentServices_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);

    ResolveInfo info = new ResolveInfo();
    info.serviceInfo = new ServiceInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    shadowOf(packageManager).addResolveInfoForIntent(i, info);

    List<ResolveInfo> services = packageManager.queryIntentServices(i, 0);
    assertThat(services).hasSize(1);
    assertThat(services.get(0).nonLocalizedLabel.toString()).isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void queryIntentServices_fromManifest() {
    Intent i = new Intent("org.robolectric.ACTION_DIFFERENT_PACKAGE");
    i.addCategory(Intent.CATEGORY_LAUNCHER);
    i.setType("image/jpeg");
    List<ResolveInfo> services = packageManager.queryIntentServices(i, 0);
    assertThat(services).isNotEmpty();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryIntentServices_appHidden_includeUninstalled() {
    String packageName = context.getPackageName();
    packageManager.setApplicationHiddenSettingAsUser(
        packageName, /* hidden= */ true, /* user= */ null);

    Intent i = new Intent();
    i.setClassName(context, "com.foo.Service");

    List<ResolveInfo> services = packageManager.queryIntentServices(i, MATCH_UNINSTALLED_PACKAGES);
    assertThat(services).hasSize(1);
    assertThat(services.get(0).resolvePackageName).isEqualTo(packageName);
    assertThat(services.get(0).serviceInfo.name).isEqualTo("com.foo.Service");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryIntentServices_appHidden_dontIncludeUninstalled() {
    String packageName = context.getPackageName();
    packageManager.setApplicationHiddenSettingAsUser(
        packageName, /* hidden= */ true, /* user= */ null);

    Intent i = new Intent();
    i.setClassName(context, "com.foo.Service");

    assertThat(packageManager.queryIntentServices(i, /* flags= */ 0)).isEmpty();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void queryIntentServicesAsUser() {
    Intent i = new Intent("org.robolectric.ACTION_DIFFERENT_PACKAGE");
    i.addCategory(Intent.CATEGORY_LAUNCHER);
    i.setType("image/jpeg");
    List<ResolveInfo> services = packageManager.queryIntentServicesAsUser(i, 0, 0);
    assertThat(services).isNotEmpty();
  }

  @Test
  public void queryBroadcastReceivers_EmptyResult() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    i.addCategory(Intent.CATEGORY_LAUNCHER);

    List<ResolveInfo> broadCastReceivers = packageManager.queryBroadcastReceivers(i, 0);
    assertThat(broadCastReceivers).isEmpty();
  }

  @Test
  public void queryBroadcastReceivers_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);

    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;

    shadowOf(packageManager).addResolveInfoForIntent(i, info);

    List<ResolveInfo> broadCastReceivers = packageManager.queryBroadcastReceivers(i, 0);
    assertThat(broadCastReceivers).hasSize(1);
    assertThat(broadCastReceivers.get(0).nonLocalizedLabel.toString())
        .isEqualTo(TEST_PACKAGE_LABEL);
  }

  @Test
  public void queryBroadcastReceivers_MatchWithExplicitIntent() throws Exception {
    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.fakes.ConfigTestReceiver");

    List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(i, 0);
    assertThat(receivers).isNotNull();
    assertThat(receivers).hasSize(1);
    assertThat(receivers.get(0).resolvePackageName).isEqualTo("org.robolectric");
    assertThat(receivers.get(0).activityInfo.name)
        .isEqualTo("org.robolectric.fakes.ConfigTestReceiver");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryBroadcastReceivers_appHidden_includeUninstalled() {
    String packageName = context.getPackageName();
    packageManager.setApplicationHiddenSettingAsUser(
        packageName, /* hidden= */ true, /* user= */ null);

    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.fakes.ConfigTestReceiver");

    List<ResolveInfo> activities =
        packageManager.queryBroadcastReceivers(i, MATCH_UNINSTALLED_PACKAGES);
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).resolvePackageName).isEqualTo(packageName);
    assertThat(activities.get(0).activityInfo.name)
        .isEqualTo("org.robolectric.fakes.ConfigTestReceiver");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryBroadcastReceivers_appHidden_dontIncludeUninstalled() {
    String packageName = context.getPackageName();
    packageManager.setApplicationHiddenSettingAsUser(
        packageName, /* hidden= */ true, /* user= */ null);

    Intent i = new Intent();
    i.setClassName(context, "org.robolectric.fakes.ConfigTestReceiver");

    assertThat(packageManager.queryBroadcastReceivers(i, /* flags= */ 0)).isEmpty();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryIntentContentProviders_EmptyResult() throws Exception {
    Intent i = new Intent(DocumentsContract.PROVIDER_INTERFACE);

    List<ResolveInfo> broadCastReceivers = packageManager.queryIntentContentProviders(i, 0);
    assertThat(broadCastReceivers).isEmpty();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryIntentContentProviders_Match() throws Exception {
    Intent i = new Intent(DocumentsContract.PROVIDER_INTERFACE);

    ResolveInfo resolveInfo = new ResolveInfo();
    ProviderInfo providerInfo = new ProviderInfo();
    providerInfo.authority = "com.robolectric";
    resolveInfo.providerInfo = providerInfo;

    shadowOf(packageManager).addResolveInfoForIntent(i, resolveInfo);

    List<ResolveInfo> contentProviders = packageManager.queryIntentContentProviders(i, 0);
    assertThat(contentProviders).hasSize(1);
    assertThat(contentProviders.get(0).providerInfo.authority).isEqualTo(providerInfo.authority);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryIntentContentProviders_MatchSystemOnly() throws Exception {
    Intent i = new Intent(DocumentsContract.PROVIDER_INTERFACE);

    ResolveInfo info1 = new ResolveInfo();
    info1.providerInfo = new ProviderInfo();
    info1.providerInfo.applicationInfo = new ApplicationInfo();

    ResolveInfo info2 = new ResolveInfo();
    info2.providerInfo = new ProviderInfo();
    info2.providerInfo.applicationInfo = new ApplicationInfo();
    info2.providerInfo.applicationInfo.flags |= ApplicationInfo.FLAG_SYSTEM;
    info2.nonLocalizedLabel = "System App";

    shadowOf(packageManager).addResolveInfoForIntent(i, info1);
    shadowOf(packageManager).addResolveInfoForIntent(i, info2);

    List<ResolveInfo> activities =
        packageManager.queryIntentContentProviders(i, PackageManager.MATCH_SYSTEM_ONLY);
    assertThat(activities).isNotNull();
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).nonLocalizedLabel.toString()).isEqualTo("System App");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryIntentContentProviders_MatchDisabledComponents() throws Exception {
    Intent i = new Intent(DocumentsContract.PROVIDER_INTERFACE);

    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.providerInfo = new ProviderInfo();
    resolveInfo.providerInfo.applicationInfo = new ApplicationInfo();
    resolveInfo.providerInfo.applicationInfo.packageName =
        "org.robolectric.shadows.TestPackageName";
    resolveInfo.providerInfo.name = "org.robolectric.shadows.TestProvider";
    resolveInfo.providerInfo.enabled = false;

    shadowOf(packageManager).addResolveInfoForIntent(i, resolveInfo);

    List<ResolveInfo> resolveInfos = packageManager.queryIntentContentProviders(i, 0);
    assertThat(resolveInfos).isEmpty();

    resolveInfos =
        packageManager.queryIntentContentProviders(i, PackageManager.MATCH_DISABLED_COMPONENTS);
    assertThat(resolveInfos).isNotNull();
    assertThat(resolveInfos).hasSize(1);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queryIntentContentProviders_appHidden_includeUninstalled() {
    String packageName = context.getPackageName();
    packageManager.setApplicationHiddenSettingAsUser(
        packageName, /* hidden= */ true, /* user= */ null);

    Intent i = new Intent(DocumentsContract.PROVIDER_INTERFACE);
    i.setClassName(context, "org.robolectric.shadows.testing.TestContentProvider1");

    List<ResolveInfo> resolveInfos = packageManager.queryIntentContentProviders(i, 0);
    assertThat(resolveInfos).isEmpty();

    resolveInfos = packageManager.queryIntentContentProviders(i, MATCH_UNINSTALLED_PACKAGES);

    assertThat(resolveInfos).hasSize(1);
    assertThat(resolveInfos.get(0).providerInfo.applicationInfo.packageName).isEqualTo(packageName);
    assertThat(resolveInfos.get(0).providerInfo.name)
        .isEqualTo("org.robolectric.shadows.testing.TestContentProvider1");
  }

  @Test
  public void resolveService_Match() throws Exception {
    Intent i = new Intent(Intent.ACTION_MAIN, null);
    ResolveInfo info = new ResolveInfo();
    info.serviceInfo = new ServiceInfo();
    info.serviceInfo.name = "name";
    shadowOf(packageManager).addResolveInfoForIntent(i, info);
    assertThat(packageManager.resolveService(i, 0)).isNotNull();
    assertThat(packageManager.resolveService(i, 0).serviceInfo.name).isEqualTo("name");
  }

  @Test
  public void removeResolveInfosForIntent_shouldCauseResolveActivityToReturnNull()
      throws Exception {
    Intent intent =
        new Intent(Intent.ACTION_APP_ERROR, null).addCategory(Intent.CATEGORY_APP_BROWSER);
    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    info.activityInfo = new ActivityInfo();
    info.activityInfo.packageName = "com.org";
    shadowOf(packageManager).addResolveInfoForIntent(intent, info);

    shadowOf(packageManager).removeResolveInfosForIntent(intent, "com.org");

    assertThat(packageManager.resolveActivity(intent, 0)).isNull();
  }

  @Test
  public void removeResolveInfosForIntent_forService() throws Exception {
    Intent intent =
        new Intent(Intent.ACTION_APP_ERROR, null).addCategory(Intent.CATEGORY_APP_BROWSER);
    ResolveInfo info = new ResolveInfo();
    info.nonLocalizedLabel = TEST_PACKAGE_LABEL;
    info.serviceInfo = new ServiceInfo();
    info.serviceInfo.packageName = "com.org";
    shadowOf(packageManager).addResolveInfoForIntent(intent, info);

    shadowOf(packageManager).removeResolveInfosForIntent(intent, "com.org");

    assertThat(packageManager.resolveService(intent, 0)).isNull();
  }

  @Test
  public void resolveService_NoMatch() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName("foo.bar", "No Activity"));
    assertThat(packageManager.resolveService(i, 0)).isNull();
  }

  @Test
  public void queryActivityIcons_Match() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName(TEST_PACKAGE_NAME, ""));
    Drawable d = new BitmapDrawable();

    shadowOf(packageManager).addActivityIcon(i, d);

    assertThat(packageManager.getActivityIcon(i)).isSameInstanceAs(d);
    assertThat(packageManager.getActivityIcon(i.getComponent())).isSameInstanceAs(d);
  }

  @Test
  public void getApplicationIcon_componentName_matches() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName(TEST_PACKAGE_NAME, ""));
    Drawable d = new BitmapDrawable();

    shadowOf(packageManager).setApplicationIcon(TEST_PACKAGE_NAME, d);

    assertThat(packageManager.getApplicationIcon(TEST_PACKAGE_NAME)).isSameInstanceAs(d);
  }

  @Test
  public void getApplicationIcon_applicationInfo_matches() throws Exception {
    Intent i = new Intent();
    i.setComponent(new ComponentName(TEST_PACKAGE_NAME, ""));
    Drawable d = new BitmapDrawable();

    shadowOf(packageManager).setApplicationIcon(TEST_PACKAGE_NAME, d);

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = TEST_PACKAGE_NAME;

    assertThat(packageManager.getApplicationIcon(applicationInfo)).isSameInstanceAs(d);
  }

  @Test
  public void hasSystemFeature() throws Exception {
    // uninitialized
    assertThat(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)).isFalse();

    // positive
    shadowOf(packageManager).setSystemFeature(PackageManager.FEATURE_CAMERA, true);
    assertThat(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)).isTrue();

    // negative
    shadowOf(packageManager).setSystemFeature(PackageManager.FEATURE_CAMERA, false);
    assertThat(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)).isFalse();
  }

  @Test
  public void addSystemSharedLibraryName() {
    shadowOf(packageManager).addSystemSharedLibraryName("com.foo.system_library_1");
    shadowOf(packageManager).addSystemSharedLibraryName("com.foo.system_library_2");

    assertThat(packageManager.getSystemSharedLibraryNames())
        .asList()
        .containsExactly("com.foo.system_library_1", "com.foo.system_library_2");
  }

  @Test
  public void clearSystemSharedLibraryName() {
    shadowOf(packageManager).addSystemSharedLibraryName("com.foo.system_library_1");
    shadowOf(packageManager).clearSystemSharedLibraryNames();

    assertThat(packageManager.getSystemSharedLibraryNames()).isEmpty();
  }

  @Test
  public void getPackageInfo_shouldReturnActivityInfos() throws Exception {
    PackageInfo packageInfo =
        packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
    ActivityInfo activityInfoWithFilters =
        findActivity(packageInfo.activities, ActivityWithFilters.class.getName());
    assertThat(activityInfoWithFilters.packageName).isEqualTo("org.robolectric");
    assertThat(activityInfoWithFilters.exported).isEqualTo(true);
    assertThat(activityInfoWithFilters.permission).isEqualTo("com.foo.MY_PERMISSION");
  }

  private static ActivityInfo findActivity(ActivityInfo[] activities, String name) {
    for (ActivityInfo activityInfo : activities) {
      if (activityInfo.name.equals(name)) {
        return activityInfo;
      }
    }
    return null;
  }

  @Test
  public void getPackageInfo_getProvidersShouldReturnProviderInfos() throws Exception {
    PackageInfo packageInfo =
        packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
    ProviderInfo[] providers = packageInfo.providers;
    assertThat(providers).isNotEmpty();
    assertThat(providers.length).isEqualTo(3);
    assertThat(providers[0].packageName).isEqualTo("org.robolectric");
    assertThat(providers[1].packageName).isEqualTo("org.robolectric");
    assertThat(providers[2].packageName).isEqualTo("org.robolectric");
  }

  @Test
  public void getProviderInfo_shouldReturnProviderInfos() throws Exception {
    ProviderInfo providerInfo1 =
        packageManager.getProviderInfo(
            new ComponentName(context, "org.robolectric.shadows.testing.TestContentProvider1"), 0);
    assertThat(providerInfo1.packageName).isEqualTo("org.robolectric");
    assertThat(providerInfo1.authority).isEqualTo("org.robolectric.authority1");

    ProviderInfo providerInfo2 =
        packageManager.getProviderInfo(
            new ComponentName(context, "org.robolectric.shadows.testing.TestContentProvider2"), 0);
    assertThat(providerInfo2.packageName).isEqualTo("org.robolectric");
    assertThat(providerInfo2.authority).isEqualTo("org.robolectric.authority2");
  }

  @Test
  public void getProviderInfo_packageNotFoundShouldThrowException() {
    try {
      packageManager.getProviderInfo(
          new ComponentName("non.existent.package", ".tester.DoesntExist"), 0);
      fail("should have thrown NameNotFoundException");
    } catch (NameNotFoundException e) {
      // expected
    }
  }

  @Test
  public void getProviderInfo_shouldPopulatePermissionsInProviderInfos() throws Exception {
    ProviderInfo providerInfo =
        packageManager.getProviderInfo(
            new ComponentName(context, "org.robolectric.shadows.testing.TestContentProvider1"), 0);
    assertThat(providerInfo.authority).isEqualTo("org.robolectric.authority1");

    assertThat(providerInfo.readPermission).isEqualTo("READ_PERMISSION");
    assertThat(providerInfo.writePermission).isEqualTo("WRITE_PERMISSION");

    assertThat(providerInfo.pathPermissions).asList().hasSize(1);
    assertThat(providerInfo.pathPermissions[0].getType())
        .isEqualTo(PathPermission.PATTERN_SIMPLE_GLOB);
    assertThat(providerInfo.pathPermissions[0].getPath()).isEqualTo("/path/*");
    assertThat(providerInfo.pathPermissions[0].getReadPermission())
        .isEqualTo("PATH_READ_PERMISSION");
    assertThat(providerInfo.pathPermissions[0].getWritePermission())
        .isEqualTo("PATH_WRITE_PERMISSION");
  }

  @Test
  public void getProviderInfo_shouldMetaDataInProviderInfos() throws Exception {
    ProviderInfo providerInfo =
        packageManager.getProviderInfo(
            new ComponentName(context, "org.robolectric.shadows.testing.TestContentProvider1"),
            PackageManager.GET_META_DATA);
    assertThat(providerInfo.authority).isEqualTo("org.robolectric.authority1");

    assertThat(providerInfo.metaData.getString("greeting")).isEqualTo("Hello");
  }

  @Test
  public void resolveContentProvider_shouldResolveByPackageName() throws Exception {
    ProviderInfo providerInfo =
        packageManager.resolveContentProvider("org.robolectric.authority1", 0);
    assertThat(providerInfo.packageName).isEqualTo("org.robolectric");
    assertThat(providerInfo.authority).isEqualTo("org.robolectric.authority1");
  }

  @Test
  public void resolveContentProvider_multiAuthorities() throws Exception {
    ProviderInfo providerInfo =
        packageManager.resolveContentProvider("org.robolectric.authority3", 0);
    assertThat(providerInfo.packageName).isEqualTo("org.robolectric");
    assertThat(providerInfo.authority)
        .isEqualTo("org.robolectric.authority3;org.robolectric.authority4");
  }

  @Test
  public void testReceiverInfo() throws Exception {
    ActivityInfo info =
        packageManager.getReceiverInfo(
            new ComponentName(context, "org.robolectric.test.ConfigTestReceiver"),
            PackageManager.GET_META_DATA);
    assertThat(info.metaData.getInt("numberOfSheep")).isEqualTo(42);
  }

  @Test
  public void testGetPackageInfo_ForReceiversIncorrectPackage() {
    try {
      packageManager.getPackageInfo("unknown_package", PackageManager.GET_RECEIVERS);
      fail("should have thrown NameNotFoundException");
    } catch (PackageManager.NameNotFoundException e) {
      assertThat(e.getMessage()).contains("unknown_package");
    }
  }

  @Test
  public void getPackageInfo_shouldReturnRequestedPermissions() throws Exception {
    PackageInfo packageInfo =
        packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
    String[] permissions = packageInfo.requestedPermissions;
    assertThat(permissions).isNotNull();
    assertThat(permissions.length).isEqualTo(4);
  }

  @Test
  public void getPackageInfo_uninstalledPackage_includeUninstalled() throws Exception {
    String packageName = context.getPackageName();
    shadowOf(packageManager).deletePackage(packageName);

    PackageInfo info = packageManager.getPackageInfo(packageName, MATCH_UNINSTALLED_PACKAGES);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(packageName);
  }

  @Test
  public void getPackageInfo_uninstalledPackage_dontIncludeUninstalled() throws Exception {
    String packageName = context.getPackageName();
    shadowOf(packageManager).deletePackage(packageName);

    try {
      PackageInfo info = packageManager.getPackageInfo(packageName, 0);
      fail("should have thrown NameNotFoundException:" + info.applicationInfo.flags);
    } catch (NameNotFoundException e) {
      // expected
    }
  }

  @Test
  public void getPackageInfo_disabledPackage_includeDisabled() throws Exception {
    packageManager.setApplicationEnabledSetting(
        context.getPackageName(), COMPONENT_ENABLED_STATE_DISABLED, 0);
    PackageInfo info =
        packageManager.getPackageInfo(context.getPackageName(), MATCH_DISABLED_COMPONENTS);
    assertThat(info).isNotNull();
    assertThat(info.packageName).isEqualTo(context.getPackageName());
  }

  @Test
  public void getInstalledPackages_uninstalledPackage_dontIncludeUninstalled() throws Exception {
    shadowOf(packageManager).deletePackage(context.getPackageName());

    assertThat(packageManager.getInstalledPackages(0)).isEmpty();
  }

  @Test
  public void getInstalledPackages_disabledPackage_includeDisabled() throws Exception {
    packageManager.setApplicationEnabledSetting(
        context.getPackageName(), COMPONENT_ENABLED_STATE_DISABLED, 0);

    assertThat(packageManager.getInstalledPackages(MATCH_DISABLED_COMPONENTS)).isNotEmpty();
    assertThat(packageManager.getInstalledPackages(MATCH_DISABLED_COMPONENTS).get(0).packageName)
        .isEqualTo(context.getPackageName());
  }

  @Test
  public void testGetPreferredActivities() throws Exception {
    final String packageName = "com.example.dummy";
    ComponentName name = new ComponentName(packageName, "LauncherActivity");

    // Setup an intentfilter and add to packagemanager
    IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
    filter.addCategory(Intent.CATEGORY_HOME);
    packageManager.addPreferredActivity(filter, 0, null, name);

    // Test match
    List<IntentFilter> filters = new ArrayList<>();
    List<ComponentName> activities = new ArrayList<>();
    int filterCount = packageManager.getPreferredActivities(filters, activities, null);

    assertThat(filterCount).isEqualTo(1);
    assertThat(activities.size()).isEqualTo(1);
    assertThat(activities.get(0).getPackageName()).isEqualTo(packageName);
    assertThat(filters.size()).isEqualTo(1);

    filterCount = packageManager.getPreferredActivities(filters, activities, "other");

    assertThat(filterCount).isEqualTo(0);
  }

  @Test
  public void resolveActivity_preferred() {
    ComponentName preferredName = new ComponentName("preferred", "LauncherActivity");
    ComponentName otherName = new ComponentName("other", "LauncherActivity");
    Intent homeIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
    shadowOf(packageManager)
        .setResolveInfosForIntent(
            homeIntent,
            ImmutableList.of(
                ShadowResolveInfo.newResolveInfo(
                    "label1", otherName.getPackageName(), otherName.getClassName()),
                ShadowResolveInfo.newResolveInfo(
                    "label2", preferredName.getPackageName(), preferredName.getClassName())));

    ResolveInfo resolveInfo = packageManager.resolveActivity(homeIntent, 0);
    assertThat(resolveInfo.activityInfo.packageName).isEqualTo(otherName.getPackageName());

    // Setup an intentfilter and add to packagemanager
    IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
    filter.addCategory(Intent.CATEGORY_HOME);
    packageManager.addPreferredActivity(filter, 0, null, preferredName);

    resolveInfo = packageManager.resolveActivity(homeIntent, 0);
    assertThat(resolveInfo.activityInfo.packageName).isEqualTo(preferredName.getPackageName());
  }

  @Test
  public void canResolveDrawableGivenPackageAndResourceId() throws Exception {
    Drawable drawable =
        Drawable.createFromStream(new ByteArrayInputStream(new byte[0]), "my_source");
    shadowOf(packageManager).addDrawableResolution("com.example.foo", 4334, drawable);
    Drawable actual = packageManager.getDrawable("com.example.foo", 4334, null);
    assertThat(actual).isSameInstanceAs(drawable);
  }

  @Test
  public void shouldAssignTheApplicationClassNameFromTheManifest() throws Exception {
    ApplicationInfo applicationInfo = packageManager.getApplicationInfo("org.robolectric", 0);
    assertThat(applicationInfo.className)
        .isEqualTo("org.robolectric.shadows.testing.TestApplication");
  }

  @Test
  @Config(minSdk = N_MR1)
  public void shouldAssignTheApplicationNameFromTheManifest() throws Exception {
    ApplicationInfo applicationInfo = packageManager.getApplicationInfo("org.robolectric", 0);
    assertThat(applicationInfo.name).isEqualTo("org.robolectric.shadows.testing.TestApplication");
  }

  @Test
  public void testLaunchIntentForPackage() {
    Intent intent = packageManager.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent).isNull();

    Intent launchIntent = new Intent(Intent.ACTION_MAIN);
    launchIntent.setPackage(TEST_PACKAGE_LABEL);
    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "LauncherActivity";
    shadowOf(packageManager).addResolveInfoForIntent(launchIntent, resolveInfo);

    intent = packageManager.getLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent.getComponent().getClassName()).isEqualTo("LauncherActivity");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void testLeanbackLaunchIntentForPackage() {
    Intent intent = packageManager.getLeanbackLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent).isNull();

    Intent launchIntent = new Intent(Intent.ACTION_MAIN);
    launchIntent.setPackage(TEST_PACKAGE_LABEL);
    launchIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "LauncherActivity";
    shadowOf(packageManager).addResolveInfoForIntent(launchIntent, resolveInfo);

    intent = packageManager.getLeanbackLaunchIntentForPackage(TEST_PACKAGE_LABEL);
    assertThat(intent.getComponent().getClassName()).isEqualTo("LauncherActivity");
  }

  @Test
  public void shouldAssignTheAppMetaDataFromTheManifest() throws Exception {
    ApplicationInfo info = packageManager.getApplicationInfo(context.getPackageName(), 0);
    Bundle meta = info.metaData;

    assertThat(meta.getString("org.robolectric.metaName1")).isEqualTo("metaValue1");
    assertThat(meta.getString("org.robolectric.metaName2")).isEqualTo("metaValue2");

    assertThat(meta.getBoolean("org.robolectric.metaFalseLiteral")).isEqualTo(false);
    assertThat(meta.getBoolean("org.robolectric.metaTrueLiteral")).isEqualTo(true);

    assertThat(meta.getInt("org.robolectric.metaInt")).isEqualTo(123);
    assertThat(meta.getFloat("org.robolectric.metaFloat")).isEqualTo(1.23f);

    assertThat(meta.getInt("org.robolectric.metaColor")).isEqualTo(Color.WHITE);

    assertThat(meta.getBoolean("org.robolectric.metaBooleanFromRes"))
        .isEqualTo(context.getResources().getBoolean(R.bool.false_bool_value));

    assertThat(meta.getInt("org.robolectric.metaIntFromRes"))
        .isEqualTo(context.getResources().getInteger(R.integer.test_integer1));

    assertThat(meta.getInt("org.robolectric.metaColorFromRes"))
        .isEqualTo(context.getResources().getColor(R.color.clear));

    assertThat(meta.getString("org.robolectric.metaStringFromRes"))
        .isEqualTo(context.getString(R.string.app_name));

    assertThat(meta.getString("org.robolectric.metaStringOfIntFromRes"))
        .isEqualTo(context.getString(R.string.str_int));

    assertThat(meta.getInt("org.robolectric.metaStringRes")).isEqualTo(R.string.app_name);
  }

  @Test
  public void testResolveDifferentIntentObjects() {
    Intent intent1 = new Intent(Intent.ACTION_MAIN);
    intent1.setPackage(TEST_PACKAGE_LABEL);
    intent1.addCategory(Intent.CATEGORY_APP_BROWSER);

    assertThat(packageManager.resolveActivity(intent1, 0)).isNull();
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "BrowserActivity";
    shadowOf(packageManager).addResolveInfoForIntent(intent1, resolveInfo);

    // the original intent object should yield a result
    ResolveInfo result = packageManager.resolveActivity(intent1, 0);
    assertThat(result.activityInfo.name).isEqualTo("BrowserActivity");

    // AND a new, functionally equivalent intent should also yield a result
    Intent intent2 = new Intent(Intent.ACTION_MAIN);
    intent2.setPackage(TEST_PACKAGE_LABEL);
    intent2.addCategory(Intent.CATEGORY_APP_BROWSER);
    result = packageManager.resolveActivity(intent2, 0);
    assertThat(result.activityInfo.name).isEqualTo("BrowserActivity");
  }

  @Test
  public void testResolvePartiallySimilarIntents() {
    Intent intent1 = new Intent(Intent.ACTION_APP_ERROR);
    intent1.setPackage(TEST_PACKAGE_LABEL);
    intent1.addCategory(Intent.CATEGORY_APP_BROWSER);

    assertThat(packageManager.resolveActivity(intent1, 0)).isNull();

    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.packageName = TEST_PACKAGE_LABEL;
    resolveInfo.activityInfo.name = "BrowserActivity";
    shadowOf(packageManager).addResolveInfoForIntent(intent1, resolveInfo);

    // the original intent object should yield a result
    ResolveInfo result = packageManager.resolveActivity(intent1, 0);
    assertThat(result.activityInfo.name).isEqualTo("BrowserActivity");

    // an intent with just the same action should not be considered the same
    Intent intent2 = new Intent(Intent.ACTION_APP_ERROR);
    result = packageManager.resolveActivity(intent2, 0);
    assertThat(result).isNull();

    // an intent with just the same category should not be considered the same
    Intent intent3 = new Intent();
    intent3.addCategory(Intent.CATEGORY_APP_BROWSER);
    result = packageManager.resolveActivity(intent3, 0);
    assertThat(result).isNull();

    // an intent without the correct package restriction should not be the same
    Intent intent4 = new Intent(Intent.ACTION_APP_ERROR);
    intent4.addCategory(Intent.CATEGORY_APP_BROWSER);
    result = packageManager.resolveActivity(intent4, 0);
    assertThat(result).isNull();
  }

  @Test
  public void testSetApplicationEnabledSetting() {
    assertThat(packageManager.getApplicationEnabledSetting("org.robolectric"))
        .isEqualTo(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

    packageManager.setApplicationEnabledSetting(
        "org.robolectric", PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

    assertThat(packageManager.getApplicationEnabledSetting("org.robolectric"))
        .isEqualTo(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
  }

  private static class ActivityWithMetadata extends Activity {}

  @Test
  public void getActivityMetaData() throws Exception {
    Activity activity = setupActivity(ActivityWithMetadata.class);

    ActivityInfo activityInfo =
        packageManager.getActivityInfo(
            activity.getComponentName(),
            PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA);
    assertThat(activityInfo.metaData.get("someName")).isEqualTo("someValue");
  }

  @Test
  public void shouldAssignLabelResFromTheManifest() throws Exception {
    ApplicationInfo applicationInfo = packageManager.getApplicationInfo("org.robolectric", 0);
    assertThat(applicationInfo.labelRes).isEqualTo(R.string.app_name);
    assertThat(applicationInfo.nonLocalizedLabel).isNull();
  }

  @Test
  public void getServiceInfo_shouldReturnServiceInfoIfExists() throws Exception {
    ServiceInfo serviceInfo =
        packageManager.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), 0);
    assertThat(serviceInfo.packageName).isEqualTo("org.robolectric");
    assertThat(serviceInfo.name).isEqualTo("com.foo.Service");
    assertThat(serviceInfo.permission).isEqualTo("com.foo.MY_PERMISSION");
    assertThat(serviceInfo.applicationInfo).isNotNull();
  }

  @Test
  public void getServiceInfo_shouldReturnServiceInfoWithMetaDataWhenFlagsSet() throws Exception {
    ServiceInfo serviceInfo =
        packageManager.getServiceInfo(
            new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_META_DATA);
    assertThat(serviceInfo.metaData).isNotNull();
  }

  @Test
  public void getServiceInfo_shouldReturnServiceInfoWithoutMetaDataWhenFlagsNotSet()
      throws Exception {
    ComponentName component = new ComponentName("org.robolectric", "com.foo.Service");
    ServiceInfo serviceInfo = packageManager.getServiceInfo(component, 0);
    assertThat(serviceInfo.metaData).isNull();
  }

  @Test
  public void getServiceInfo_shouldThrowNameNotFoundExceptionIfNotExist() {
    ComponentName nonExistComponent =
        new ComponentName("org.robolectric", "com.foo.NonExistService");
    try {
      packageManager.getServiceInfo(nonExistComponent, PackageManager.GET_SERVICES);
      fail("should have thrown NameNotFoundException");
    } catch (PackageManager.NameNotFoundException e) {
      assertThat(e.getMessage()).contains("com.foo.NonExistService");
    }
  }

  @Test
  public void getServiceInfo_shouldFindServiceIfAddedInResolveInfo() throws Exception {
    ComponentName componentName = new ComponentName("com.test", "com.test.ServiceName");
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.serviceInfo = new ServiceInfo();
    resolveInfo.serviceInfo.name = componentName.getClassName();
    resolveInfo.serviceInfo.applicationInfo = new ApplicationInfo();
    resolveInfo.serviceInfo.applicationInfo.packageName = componentName.getPackageName();
    shadowOf(packageManager).addResolveInfoForIntent(new Intent("RANDOM_ACTION"), resolveInfo);

    ServiceInfo serviceInfo = packageManager.getServiceInfo(componentName, 0);
    assertThat(serviceInfo).isNotNull();
  }

  @Test
  public void getNameForUid() {
    assertThat(packageManager.getNameForUid(10)).isNull();

    shadowOf(packageManager).setNameForUid(10, "a_name");

    assertThat(packageManager.getNameForUid(10)).isEqualTo("a_name");
  }

  @Test
  public void getPackagesForUid() {
    assertThat(packageManager.getPackagesForUid(10)).isNull();

    shadowOf(packageManager).setPackagesForUid(10, new String[] {"a_name"});

    assertThat(packageManager.getPackagesForUid(10)).asList().containsExactly("a_name");
  }

  @Test
  @Config(minSdk = N)
  public void getPackageUid() throws NameNotFoundException {
    shadowOf(packageManager).setPackagesForUid(10, new String[] {"a_name"});
    assertThat(packageManager.getPackageUid("a_name", 0)).isEqualTo(10);
  }

  @Test
  @Config(minSdk = N)
  public void getPackageUid_shouldThrowNameNotFoundExceptionIfNotExist() {
    try {
      packageManager.getPackageUid("a_name", 0);
      fail("should have thrown NameNotFoundException");
    } catch (PackageManager.NameNotFoundException e) {
      assertThat(e.getMessage()).contains("a_name");
    }
  }

  @Test
  public void getPackagesForUid_shouldReturnSetPackageName() {
    shadowOf(packageManager).setPackagesForUid(10, new String[] {"a_name"});
    assertThat(packageManager.getPackagesForUid(10)).asList().containsExactly("a_name");
  }

  @Test
  public void getResourcesForApplication_currentApplication() throws Exception {
    assertThat(
            packageManager
                .getResourcesForApplication("org.robolectric")
                .getString(R.string.app_name))
        .isEqualTo(context.getString(R.string.app_name));
  }

  @Test
  public void getResourcesForApplication_unknownPackage() {
    try {
      packageManager.getResourcesForApplication("non.existent.package");
      fail("should have thrown NameNotFoundException");
    } catch (NameNotFoundException e) {
      // expected
    }
  }

  @Test
  public void getResourcesForApplication_anotherPackage() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "another.package";

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = "another.package";
    packageInfo.applicationInfo = applicationInfo;
    shadowOf(packageManager).installPackage(packageInfo);

    assertThat(packageManager.getResourcesForApplication("another.package")).isNotNull();
    assertThat(packageManager.getResourcesForApplication("another.package"))
        .isNotEqualTo(context.getResources());
  }

  private void verifyApkNotInstalled(String packageName) {
    try {
      packageManager.getPackageInfo(packageName, 0);
      Assert.fail("Package not expected to be installed.");
    } catch (NameNotFoundException e) {
      // expected exception
    }
  }

  @Test
  public void getResourcesForApplication_ApkNotInstalled() throws NameNotFoundException {
    assume().that(RuntimeEnvironment.useLegacyResources()).isFalse();

    File testApk = TestUtil.resourcesBaseDir().resolve(REAL_TEST_APP_ASSET_PATH).toFile();

    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(testApk.getAbsolutePath(), 0);

    assertThat(packageInfo).isNotNull();
    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
    assertThat(applicationInfo.packageName).isEqualTo(REAL_TEST_APP_PACKAGE_NAME);

    // double-check that Robolectric doesn't consider this package to be installed
    verifyApkNotInstalled(packageInfo.packageName);

    applicationInfo.sourceDir = applicationInfo.publicSourceDir = testApk.getAbsolutePath();
    assertThat(packageManager.getResourcesForApplication(applicationInfo)).isNotNull();
  }

  @Test
  public void getResourcesForApplication_ApkNotPresent() {
    ApplicationInfo applicationInfo =
        ApplicationInfoBuilder.newBuilder().setPackageName("com.not.present").build();
    applicationInfo.sourceDir = applicationInfo.publicSourceDir = "/some/nonexistant/path";

    try {
      packageManager.getResourcesForApplication(applicationInfo);
      Assert.fail("Expected NameNotFoundException not thrown");
    } catch (NameNotFoundException ex) {
      // Expected exception
    }
  }

  @Test
  @Config(minSdk = M)
  public void shouldShowRequestPermissionRationale() {
    assertThat(packageManager.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
        .isFalse();

    shadowOf(packageManager)
        .setShouldShowRequestPermissionRationale(Manifest.permission.CAMERA, true);

    assertThat(packageManager.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
        .isTrue();
  }

  @Test
  public void getSystemAvailableFeatures() {
    assertThat(packageManager.getSystemAvailableFeatures()).isNull();

    FeatureInfo feature = new FeatureInfo();
    feature.reqGlEsVersion = 0x20000;
    feature.flags = FeatureInfo.FLAG_REQUIRED;
    shadowOf(packageManager).addSystemAvailableFeature(feature);

    assertThat(packageManager.getSystemAvailableFeatures()).asList().contains(feature);

    shadowOf(packageManager).clearSystemAvailableFeatures();

    assertThat(packageManager.getSystemAvailableFeatures()).isNull();
  }

  @Test
  public void verifyPendingInstall() {
    packageManager.verifyPendingInstall(1234, VERIFICATION_ALLOW);

    assertThat(shadowOf(packageManager).getVerificationResult(1234)).isEqualTo(VERIFICATION_ALLOW);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void extendPendingInstallTimeout() {
    packageManager.extendVerificationTimeout(1234, 0, 1000);

    assertThat(shadowOf(packageManager).getVerificationExtendedTimeout(1234)).isEqualTo(1000);
  }

  @Test
  @Config(minSdk = N, maxSdk = N_MR1) // Functionality removed in O
  public void whenPackageNotPresent_getPackageSizeInfo_callsBackWithFailure() throws Exception {
    IPackageStatsObserver packageStatsObserver = mock(IPackageStatsObserver.class);
    packageManager.getPackageSizeInfo("nonexistant.package", packageStatsObserver);
    shadowMainLooper().idle();

    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(false));
    assertThat(packageStatsCaptor.getValue()).isNull();
  }

  @Test
  @Config(minSdk = N, maxSdk = N_MR1) // Functionality removed in O
  public void whenPackageNotPresentAndPaused_getPackageSizeInfo_callsBackWithFailure()
      throws Exception {
    shadowMainLooper().pause();
    IPackageStatsObserver packageStatsObserver = mock(IPackageStatsObserver.class);
    packageManager.getPackageSizeInfo("nonexistant.package", packageStatsObserver);

    verifyNoMoreInteractions(packageStatsObserver);

    shadowMainLooper().idle();
    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(false));
    assertThat(packageStatsCaptor.getValue()).isNull();
  }

  @Test
  @Config(minSdk = N, maxSdk = N_MR1) // Functionality removed in O
  public void whenNotPreconfigured_getPackageSizeInfo_callsBackWithDefaults() throws Exception {
    IPackageStatsObserver packageStatsObserver = mock(IPackageStatsObserver.class);
    packageManager.getPackageSizeInfo("org.robolectric", packageStatsObserver);
    shadowMainLooper().idle();

    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(true));
    assertThat(packageStatsCaptor.getValue().packageName).isEqualTo("org.robolectric");
  }

  @Test
  @Config(minSdk = N, maxSdk = N_MR1) // Functionality removed in O
  public void whenPreconfigured_getPackageSizeInfo_callsBackWithConfiguredValues()
      throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "org.robolectric";
    PackageStats packageStats = new PackageStats("org.robolectric");
    shadowOf(packageManager).addPackage(packageInfo, packageStats);

    IPackageStatsObserver packageStatsObserver = mock(IPackageStatsObserver.class);
    packageManager.getPackageSizeInfo("org.robolectric", packageStatsObserver);
    shadowMainLooper().idle();

    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(true));
    assertThat(packageStatsCaptor.getValue().packageName).isEqualTo("org.robolectric");
    assertThat(packageStatsCaptor.getValue().toString()).isEqualTo(packageStats.toString());
  }

  @Test
  @Config(minSdk = N, maxSdk = N_MR1) // Functionality removed in O
  public void whenPreconfiguredForAnotherPackage_getPackageSizeInfo_callsBackWithConfiguredValues()
      throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "org.other";
    PackageStats packageStats = new PackageStats("org.other");
    shadowOf(packageManager).addPackage(packageInfo, packageStats);

    IPackageStatsObserver packageStatsObserver = mock(IPackageStatsObserver.class);
    packageManager.getPackageSizeInfo("org.other", packageStatsObserver);
    shadowMainLooper().idle();

    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(true));
    assertThat(packageStatsCaptor.getValue().packageName).isEqualTo("org.other");
    assertThat(packageStatsCaptor.getValue().toString()).isEqualTo(packageStats.toString());
  }

  @Test
  @Config(minSdk = N, maxSdk = N_MR1) // Functionality removed in O
  public void whenPaused_getPackageSizeInfo_callsBackWithConfiguredValuesAfterIdle()
      throws Exception {
    shadowMainLooper().pause();

    IPackageStatsObserver packageStatsObserver = mock(IPackageStatsObserver.class);
    packageManager.getPackageSizeInfo("org.robolectric", packageStatsObserver);

    verifyNoMoreInteractions(packageStatsObserver);

    shadowMainLooper().idle();
    verify(packageStatsObserver).onGetStatsCompleted(packageStatsCaptor.capture(), eq(true));
    assertThat(packageStatsCaptor.getValue().packageName).isEqualTo("org.robolectric");
  }

  @Test
  public void addCurrentToCannonicalName() {
    shadowOf(packageManager).addCurrentToCannonicalName("current_name_1", "canonical_name_1");
    shadowOf(packageManager).addCurrentToCannonicalName("current_name_2", "canonical_name_2");

    assertThat(
            packageManager.currentToCanonicalPackageNames(
                new String[] {"current_name_1", "current_name_2", "some_other_name"}))
        .asList()
        .containsExactly("canonical_name_1", "canonical_name_2", "some_other_name")
        .inOrder();
  }

  @Test
  public void addCanonicalName() {
    shadowOf(packageManager).addCanonicalName("current_name_1", "canonical_name_1");
    shadowOf(packageManager).addCanonicalName("current_name_2", "canonical_name_2");

    assertThat(
            packageManager.canonicalToCurrentPackageNames(
                new String[] {"canonical_name_1", "canonical_name_2", "some_other_name"}))
        .asList()
        .containsExactly("current_name_1", "current_name_2", "some_other_name")
        .inOrder();
    assertThat(
            packageManager.currentToCanonicalPackageNames(
                new String[] {"current_name_1", "current_name_2", "some_other_name"}))
        .asList()
        .containsExactly("canonical_name_1", "canonical_name_2", "some_other_name")
        .inOrder();
  }

  @Test
  public void getInstalledApplications() {
    List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);

    // Default should include the application under test
    assertThat(installedApplications).hasSize(1);
    assertThat(installedApplications.get(0).packageName).isEqualTo("org.robolectric");

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "org.other";
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = "org.other";
    shadowOf(packageManager).installPackage(packageInfo);

    installedApplications = packageManager.getInstalledApplications(0);
    assertThat(installedApplications).hasSize(2);
    assertThat(installedApplications.get(1).packageName).isEqualTo("org.other");
  }

  @Test
  public void getPermissionInfo() throws Exception {
    PermissionInfo permission =
        context.getPackageManager().getPermissionInfo("org.robolectric.some_permission", 0);
    assertThat(permission.labelRes).isEqualTo(R.string.test_permission_label);
    assertThat(permission.descriptionRes).isEqualTo(R.string.test_permission_description);
    assertThat(permission.name).isEqualTo("org.robolectric.some_permission");
  }

  @Test
  public void checkSignatures_same() throws Exception {
    shadowOf(packageManager)
        .installPackage(newPackageInfo("first.package", new Signature("00000000")));
    shadowOf(packageManager)
        .installPackage(newPackageInfo("second.package", new Signature("00000000")));
    assertThat(packageManager.checkSignatures("first.package", "second.package"))
        .isEqualTo(SIGNATURE_MATCH);
  }

  @Test
  public void checkSignatures_firstNotSigned() throws Exception {
    shadowOf(packageManager).installPackage(newPackageInfo("first.package", (Signature[]) null));
    shadowOf(packageManager)
        .installPackage(newPackageInfo("second.package", new Signature("00000000")));
    assertThat(packageManager.checkSignatures("first.package", "second.package"))
        .isEqualTo(SIGNATURE_FIRST_NOT_SIGNED);
  }

  @Test
  public void checkSignatures_secondNotSigned() throws Exception {
    shadowOf(packageManager)
        .installPackage(newPackageInfo("first.package", new Signature("00000000")));
    shadowOf(packageManager).installPackage(newPackageInfo("second.package", (Signature[]) null));
    assertThat(packageManager.checkSignatures("first.package", "second.package"))
        .isEqualTo(SIGNATURE_SECOND_NOT_SIGNED);
  }

  @Test
  public void checkSignatures_neitherSigned() throws Exception {
    shadowOf(packageManager).installPackage(newPackageInfo("first.package", (Signature[]) null));
    shadowOf(packageManager).installPackage(newPackageInfo("second.package", (Signature[]) null));
    assertThat(packageManager.checkSignatures("first.package", "second.package"))
        .isEqualTo(SIGNATURE_NEITHER_SIGNED);
  }

  @Test
  public void checkSignatures_noMatch() throws Exception {
    shadowOf(packageManager)
        .installPackage(newPackageInfo("first.package", new Signature("00000000")));
    shadowOf(packageManager)
        .installPackage(newPackageInfo("second.package", new Signature("FFFFFFFF")));
    assertThat(packageManager.checkSignatures("first.package", "second.package"))
        .isEqualTo(SIGNATURE_NO_MATCH);
  }

  @Test
  public void checkSignatures_noMatch_mustBeExact() throws Exception {
    shadowOf(packageManager)
        .installPackage(newPackageInfo("first.package", new Signature("00000000")));
    shadowOf(packageManager)
        .installPackage(
            newPackageInfo("second.package", new Signature("00000000"), new Signature("FFFFFFFF")));
    assertThat(packageManager.checkSignatures("first.package", "second.package"))
        .isEqualTo(SIGNATURE_NO_MATCH);
  }

  @Test
  public void checkSignatures_unknownPackage() throws Exception {
    assertThat(packageManager.checkSignatures("first.package", "second.package"))
        .isEqualTo(SIGNATURE_UNKNOWN_PACKAGE);
  }

  private static PackageInfo newPackageInfo(String packageName, Signature... signatures) {
    PackageInfo firstPackageInfo = new PackageInfo();
    firstPackageInfo.packageName = packageName;
    firstPackageInfo.signatures = signatures;
    return firstPackageInfo;
  }

  @Test
  public void getPermissionInfo_notFound() {
    try {
      packageManager.getPermissionInfo("non_existant_permission", 0);
      fail("should have thrown NameNotFoundException");
    } catch (NameNotFoundException e) {
      // expected
    }
  }

  @Test
  public void getPermissionInfo_noMetaData() throws Exception {
    PermissionInfo permission =
        packageManager.getPermissionInfo("org.robolectric.some_permission", 0);
    assertThat(permission.metaData).isNull();
    assertThat(permission.name).isEqualTo("org.robolectric.some_permission");
    assertThat(permission.descriptionRes).isEqualTo(R.string.test_permission_description);
    assertThat(permission.labelRes).isEqualTo(R.string.test_permission_label);
    assertThat(permission.nonLocalizedLabel).isNull();
    assertThat(permission.group).isEqualTo("my_permission_group");
    assertThat(permission.protectionLevel).isEqualTo(PermissionInfo.PROTECTION_DANGEROUS);
  }

  @Test
  public void getPermissionInfo_withMetaData() throws Exception {
    PermissionInfo permission =
        packageManager.getPermissionInfo(
            "org.robolectric.some_permission", PackageManager.GET_META_DATA);
    assertThat(permission.metaData).isNotNull();
    assertThat(permission.metaData.getString("meta_data_name")).isEqualTo("meta_data_value");
  }

  @Test
  public void getPermissionInfo_withLiteralLabel() throws Exception {
    PermissionInfo permission =
        packageManager.getPermissionInfo("org.robolectric.permission_with_literal_label", 0);
    assertThat(permission.labelRes).isEqualTo(0);
    assertThat(permission.nonLocalizedLabel.toString()).isEqualTo("Literal label");
    assertThat(permission.protectionLevel).isEqualTo(PermissionInfo.PROTECTION_NORMAL);
  }

  @Test
  public void queryPermissionsByGroup_groupNotFound() throws Exception {
    try {
      packageManager.queryPermissionsByGroup("nonexistent_permission_group", 0);
      fail("Exception expected");
    } catch (NameNotFoundException expected) {
    }
  }

  @Test
  public void queryPermissionsByGroup_noMetaData() throws Exception {
    List<PermissionInfo> permissions =
        packageManager.queryPermissionsByGroup("my_permission_group", 0);
    assertThat(permissions).hasSize(1);

    PermissionInfo permission = permissions.get(0);

    assertThat(permission.group).isEqualTo("my_permission_group");
    assertThat(permission.name).isEqualTo("org.robolectric.some_permission");
    assertThat(permission.metaData).isNull();
  }

  @Test
  public void queryPermissionsByGroup_withMetaData() throws Exception {
    List<PermissionInfo> permissions =
        packageManager.queryPermissionsByGroup("my_permission_group", PackageManager.GET_META_DATA);
    assertThat(permissions).hasSize(1);

    PermissionInfo permission = permissions.get(0);

    assertThat(permission.group).isEqualTo("my_permission_group");
    assertThat(permission.name).isEqualTo("org.robolectric.some_permission");
    assertThat(permission.metaData).isNotNull();
    assertThat(permission.metaData.getString("meta_data_name")).isEqualTo("meta_data_value");
  }

  @Test
  public void queryPermissionsByGroup_nullMatchesPermissionsNotAssociatedWithGroup()
      throws Exception {
    List<PermissionInfo> permissions = packageManager.queryPermissionsByGroup(null, 0);

    assertThat(Iterables.transform(permissions, getPermissionNames()))
        .containsExactly(
            "org.robolectric.permission_with_minimal_fields",
            "org.robolectric.permission_with_literal_label");
  }

  @Test
  public void
      queryPermissionsByGroup_nullMatchesPermissionsNotAssociatedWithGroup_with_addPermissionInfo()
          throws Exception {
    PermissionInfo permissionInfo = new PermissionInfo();
    permissionInfo.name = "some_name";
    shadowOf(packageManager).addPermissionInfo(permissionInfo);

    List<PermissionInfo> permissions = packageManager.queryPermissionsByGroup(null, 0);
    assertThat(permissions).isNotEmpty();

    assertThat(permissions.get(0).name).isEqualTo(permissionInfo.name);
  }

  @Test
  public void queryPermissionsByGroup_with_addPermissionInfo() throws Exception {
    PermissionInfo permissionInfo = new PermissionInfo();
    permissionInfo.name = "some_name";
    permissionInfo.group = "some_group";
    shadowOf(packageManager).addPermissionInfo(permissionInfo);

    List<PermissionInfo> permissions =
        packageManager.queryPermissionsByGroup(permissionInfo.group, 0);
    assertThat(permissions).hasSize(1);

    assertThat(permissions.get(0).name).isEqualTo(permissionInfo.name);
    assertThat(permissions.get(0).group).isEqualTo(permissionInfo.group);
  }

  @Test
  public void getDefaultActivityIcon() {
    assertThat(packageManager.getDefaultActivityIcon()).isNotNull();
  }

  @Test
  public void addPackageShouldUseUidToProvidePackageName() throws Exception {
    PackageInfo packageInfoOne = new PackageInfo();
    packageInfoOne.packageName = "package.one";
    packageInfoOne.applicationInfo = new ApplicationInfo();
    packageInfoOne.applicationInfo.uid = 1234;
    packageInfoOne.applicationInfo.packageName = packageInfoOne.packageName;
    shadowOf(packageManager).installPackage(packageInfoOne);

    PackageInfo packageInfoTwo = new PackageInfo();
    packageInfoTwo.packageName = "package.two";
    packageInfoTwo.applicationInfo = new ApplicationInfo();
    packageInfoTwo.applicationInfo.uid = 1234;
    packageInfoTwo.applicationInfo.packageName = packageInfoTwo.packageName;
    shadowOf(packageManager).installPackage(packageInfoTwo);

    assertThat(packageManager.getPackagesForUid(1234))
        .asList()
        .containsExactly("package.one", "package.two");
  }

  @Test
  public void installerPackageName() throws Exception {
    packageManager.setInstallerPackageName("target.package", "installer.package");

    assertThat(packageManager.getInstallerPackageName("target.package"))
        .isEqualTo("installer.package");
  }

  @Test
  @GetInstallerPackageNameMode(Mode.LEGACY)
  public void installerPackageName_notInstalledAndLegacySettings() throws Exception {
    String packageName = packageManager.getInstallerPackageName("target.package");
    assertThat(packageName).isNull();
  }

  @Test
  @GetInstallerPackageNameMode(Mode.REALISTIC)
  public void installerPackageName_notInstalledAndRealisticSettings() throws Exception {
    try {
      packageManager.getInstallerPackageName("target.package");
      fail("Exception expected");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().contains("target.package");
    }
  }

  @Test
  public void getXml() throws Exception {
    XmlResourceParser in =
        packageManager.getXml(
            context.getPackageName(), R.xml.dialog_preferences, context.getApplicationInfo());
    assertThat(in).isNotNull();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void addPackageShouldNotCreateSessions() {

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "test.package";
    shadowOf(packageManager).installPackage(packageInfo);

    assertThat(packageManager.getPackageInstaller().getAllSessions()).isEmpty();
  }

  @Test
  public void installPackage_defaults() {
    PackageInfo info = new PackageInfo();
    info.packageName = "name";
    info.activities = new ActivityInfo[] {new ActivityInfo()};

    shadowOf(packageManager).installPackage(info);

    PackageInfo installed = shadowOf(packageManager).getInternalMutablePackageInfo("name");
    ActivityInfo activity = installed.activities[0];
    assertThat(installed.applicationInfo).isNotNull();
    assertThat(installed.applicationInfo.packageName).isEqualTo("name");
    assertWithMessage("%s is installed", installed.applicationInfo)
        .that((installed.applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED) != 0)
        .isTrue();
    assertThat(activity.packageName).isEqualTo("name");
    // this should be really equal in parcel sense as ApplicationInfo doesn't implement equals().
    assertThat(activity.applicationInfo).isEqualTo(installed.applicationInfo);
    assertThat(installed.applicationInfo.processName).isEqualTo("name");
    assertThat(activity.name).isNotEmpty();
  }

  @Test
  public void addPackageMultipleTimesShouldWork() throws Exception {
    shadowOf(packageManager).addPackage("test.package");

    // Shouldn't throw exception
    shadowOf(packageManager).addPackage("test.package");
  }

  @Test
  public void addPackageSetsStorage() throws Exception {
    shadowOf(packageManager).addPackage("test.package");

    PackageInfo packageInfo = packageManager.getPackageInfo("test.package", 0);
    assertThat(packageInfo.applicationInfo.sourceDir).isNotNull();
    assertThat(new File(packageInfo.applicationInfo.sourceDir).exists()).isTrue();
    assertThat(packageInfo.applicationInfo.publicSourceDir)
        .isEqualTo(packageInfo.applicationInfo.sourceDir);
  }

  @Test
  public void addComponent_noData() {
    try {
      shadowOf(packageManager).addOrUpdateActivity(new ActivityInfo());
      fail();
    } catch (IllegalArgumentException e) {
      // should throw
    }
  }

  @Test
  public void addActivity() throws Exception {
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = "name";
    activityInfo.packageName = "package";

    shadowOf(packageManager).addOrUpdateActivity(activityInfo);

    assertThat(packageManager.getActivityInfo(new ComponentName("package", "name"), 0)).isNotNull();
  }

  @Test
  public void addService() throws Exception {
    ServiceInfo serviceInfo = new ServiceInfo();
    serviceInfo.name = "name";
    serviceInfo.packageName = "package";

    shadowOf(packageManager).addOrUpdateService(serviceInfo);

    assertThat(packageManager.getServiceInfo(new ComponentName("package", "name"), 0)).isNotNull();
  }

  @Test
  public void addProvider() throws Exception {
    ProviderInfo providerInfo = new ProviderInfo();
    providerInfo.name = "name";
    providerInfo.packageName = "package";

    shadowOf(packageManager).addOrUpdateProvider(providerInfo);

    assertThat(packageManager.getProviderInfo(new ComponentName("package", "name"), 0)).isNotNull();
  }

  @Test
  public void addReceiver() throws Exception {
    ActivityInfo receiverInfo = new ActivityInfo();
    receiverInfo.name = "name";
    receiverInfo.packageName = "package";

    shadowOf(packageManager).addOrUpdateReceiver(receiverInfo);

    assertThat(packageManager.getReceiverInfo(new ComponentName("package", "name"), 0)).isNotNull();
  }

  @Test
  public void addActivity_addsNewPackage() throws Exception {
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = "name";
    activityInfo.packageName = "package";

    shadowOf(packageManager).addOrUpdateActivity(activityInfo);
    PackageInfo packageInfo =
        packageManager.getPackageInfo("package", PackageManager.GET_ACTIVITIES);

    assertThat(packageInfo.packageName).isEqualTo("package");
    assertThat(packageInfo.applicationInfo.packageName).isEqualTo("package");
    assertWithMessage("applicationInfo is installed")
        .that((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED) != 0)
        .isTrue();
    assertThat(packageInfo.activities).hasLength(1);
    ActivityInfo addedInfo = packageInfo.activities[0];
    assertThat(addedInfo.name).isEqualTo("name");
    assertThat(addedInfo.applicationInfo).isNotNull();
    assertThat(addedInfo.applicationInfo.packageName).isEqualTo("package");
  }

  @Test
  public void addActivity_usesExistingPackage() throws Exception {
    String packageName = context.getPackageName();
    int originalActivitiesCount =
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities.length;
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = "name";
    activityInfo.packageName = packageName;

    shadowOf(packageManager).addOrUpdateActivity(activityInfo);
    PackageInfo packageInfo =
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

    assertThat(packageInfo.activities).hasLength(originalActivitiesCount + 1);
    ActivityInfo addedInfo = packageInfo.activities[originalActivitiesCount];
    assertThat(addedInfo.name).isEqualTo("name");
    assertThat(addedInfo.applicationInfo).isNotNull();
    assertThat(addedInfo.applicationInfo.packageName).isEqualTo(packageName);
  }

  @Test
  public void removeActivity() throws Exception {
    ComponentName componentName =
        new ComponentName(context, "org.robolectric.shadows.TestActivity");

    ActivityInfo removed = shadowOf(packageManager).removeActivity(componentName);

    assertThat(removed).isNotNull();
    try {
      packageManager.getActivityInfo(componentName, 0);
      // for now it goes here because package manager autocreates activities...
      // fail();
    } catch (NameNotFoundException e) {
      // expected
    }
  }

  @Test
  public void removeService() throws Exception {
    ComponentName componentName = new ComponentName(context, "com.foo.Service");

    ServiceInfo removed = shadowOf(packageManager).removeService(componentName);

    assertThat(removed).isNotNull();
    try {
      packageManager.getServiceInfo(componentName, 0);
      fail();
    } catch (NameNotFoundException e) {
      // expected
    }
  }

  @Test
  public void removeProvider() throws Exception {
    ComponentName componentName =
        new ComponentName(context, "org.robolectric.shadows.testing.TestContentProvider1");

    ProviderInfo removed = shadowOf(packageManager).removeProvider(componentName);

    assertThat(removed).isNotNull();
    try {
      packageManager.getProviderInfo(componentName, 0);
      fail();
    } catch (NameNotFoundException e) {
      // expected
    }
  }

  @Test
  public void removeReceiver() throws Exception {
    ComponentName componentName =
        new ComponentName(context, "org.robolectric.fakes.ConfigTestReceiver");

    ActivityInfo removed = shadowOf(packageManager).removeReceiver(componentName);

    assertThat(removed).isNotNull();
    try {
      packageManager.getReceiverInfo(componentName, 0);
      fail();
    } catch (NameNotFoundException e) {
      // expected
    }
  }

  @Test
  public void removeNonExistingComponent() throws Exception {
    ComponentName componentName = new ComponentName(context, "org.robolectric.DoesnExist");

    ActivityInfo removed = shadowOf(packageManager).removeReceiver(componentName);

    assertThat(removed).isNull();
  }

  @Test
  public void deletePackage() throws Exception {
    // Apps must have the android.permission.DELETE_PACKAGES set to delete packages.
    PackageManager packageManager = context.getPackageManager();
    shadowOf(packageManager)
            .getInternalMutablePackageInfo(context.getPackageName())
            .requestedPermissions =
        new String[] {android.Manifest.permission.DELETE_PACKAGES};

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "test.package";
    shadowOf(packageManager).installPackage(packageInfo);

    IPackageDeleteObserver mockObserver = mock(IPackageDeleteObserver.class);
    packageManager.deletePackage(packageInfo.packageName, mockObserver, 0);

    shadowOf(packageManager).doPendingUninstallCallbacks();

    assertThat(shadowOf(packageManager).getDeletedPackages()).contains(packageInfo.packageName);
    verify(mockObserver).packageDeleted(packageInfo.packageName, PackageManager.DELETE_SUCCEEDED);
  }

  @Test
  public void deletePackage_missingRequiredPermission() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "test.package";
    shadowOf(packageManager).installPackage(packageInfo);

    IPackageDeleteObserver mockObserver = mock(IPackageDeleteObserver.class);
    packageManager.deletePackage(packageInfo.packageName, mockObserver, 0);

    shadowOf(packageManager).doPendingUninstallCallbacks();

    assertThat(shadowOf(packageManager).getDeletedPackages()).hasSize(0);
    verify(mockObserver)
        .packageDeleted(packageInfo.packageName, PackageManager.DELETE_FAILED_INTERNAL_ERROR);
  }

  private static class ActivityWithFilters extends Activity {}

  @Test
  public void getIntentFiltersForComponent() throws Exception {
    List<IntentFilter> intentFilters =
        shadowOf(packageManager)
            .getIntentFiltersForActivity(new ComponentName(context, ActivityWithFilters.class));
    assertThat(intentFilters).hasSize(1);
    IntentFilter intentFilter = intentFilters.get(0);
    assertThat(intentFilter.getCategory(0)).isEqualTo(Intent.CATEGORY_DEFAULT);
    assertThat(intentFilter.getAction(0)).isEqualTo(Intent.ACTION_VIEW);
    assertThat(intentFilter.getDataPath(0).getPath()).isEqualTo("/testPath/test.jpeg");
  }

  @Test
  public void getPackageInfo_shouldHaveWritableDataDirs() throws Exception {
    PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

    File dataDir = new File(packageInfo.applicationInfo.dataDir);
    assertThat(dataDir.isDirectory()).isTrue();
    assertThat(dataDir.exists()).isTrue();
  }

  private static Function<PermissionInfo, String> getPermissionNames() {
    return new Function<PermissionInfo, String>() {
      @Nullable
      @Override
      public String apply(@Nullable PermissionInfo permissionInfo) {
        return permissionInfo.name;
      }
    };
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getApplicationHiddenSettingAsUser_hidden() throws Exception {
    String packageName = context.getPackageName();

    packageManager.setApplicationHiddenSettingAsUser(
        packageName, /* hidden= */ true, /* user= */ null);

    assertThat(packageManager.getApplicationHiddenSettingAsUser(packageName, /* user= */ null))
        .isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getApplicationHiddenSettingAsUser_notHidden() throws Exception {
    String packageName = context.getPackageName();

    assertThat(packageManager.getApplicationHiddenSettingAsUser(packageName, /* user= */ null))
        .isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getApplicationHiddenSettingAsUser_unknownPackage() throws Exception {
    assertThat(packageManager.getApplicationHiddenSettingAsUser("not.a.package", /* user= */ null))
        .isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setApplicationHiddenSettingAsUser_includeUninstalled() throws Exception {
    String packageName = context.getPackageName();

    packageManager.setApplicationHiddenSettingAsUser(
        packageName, /* hidden= */ true, /* user= */ null);

    assertThat(packageManager.getPackageInfo(packageName, MATCH_UNINSTALLED_PACKAGES)).isNotNull();
    assertThat(packageManager.getApplicationInfo(packageName, MATCH_UNINSTALLED_PACKAGES))
        .isNotNull();
    List<PackageInfo> installedPackages =
        packageManager.getInstalledPackages(MATCH_UNINSTALLED_PACKAGES);
    assertThat(installedPackages).hasSize(1);
    assertThat(installedPackages.get(0).packageName).isEqualTo(packageName);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setApplicationHiddenSettingAsUser_dontIncludeUninstalled() throws Exception {
    String packageName = context.getPackageName();

    boolean success =
        packageManager.setApplicationHiddenSettingAsUser(
            packageName, /* hidden= */ true, /* user= */ null);

    assertThat(success).isTrue();

    try {
      PackageInfo info = packageManager.getPackageInfo(packageName, /* flags= */ 0);
      fail(
          "PackageManager.NameNotFoundException not thrown. Returned app with flags: "
              + info.applicationInfo.flags);
    } catch (NameNotFoundException e) {
      // Expected
    }

    try {
      packageManager.getApplicationInfo(packageName, /* flags= */ 0);
      fail("PackageManager.NameNotFoundException not thrown");
    } catch (NameNotFoundException e) {
      // Expected
    }

    assertThat(packageManager.getInstalledPackages(/* flags= */ 0)).isEmpty();
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void setUnbadgedApplicationIcon() throws Exception {
    String packageName = context.getPackageName();
    Drawable d = new BitmapDrawable();

    shadowOf(packageManager).setUnbadgedApplicationIcon(packageName, d);

    assertThat(
            packageManager
                .getApplicationInfo(packageName, PackageManager.GET_SHARED_LIBRARY_FILES)
                .loadUnbadgedIcon(packageManager))
        .isSameInstanceAs(d);
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void isPackageSuspended_nonExistentPackage_shouldThrow() {
    try {
      packageManager.isPackageSuspended(TEST_PACKAGE_NAME);
      fail("Should have thrown NameNotFoundException");
    } catch (Exception expected) {
      // The compiler thinks that isPackageSuspended doesn't throw NameNotFoundException because the
      // test is compiled against the publicly released SDK.
      assertThat(expected).isInstanceOf(NameNotFoundException.class);
    }
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void isPackageSuspended_callersPackage_shouldReturnFalse() throws NameNotFoundException {
    assertThat(packageManager.isPackageSuspended(context.getPackageName())).isFalse();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void isPackageSuspended_installedNeverSuspendedPackage_shouldReturnFalse()
      throws NameNotFoundException {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    assertThat(packageManager.isPackageSuspended(TEST_PACKAGE_NAME)).isFalse();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void isPackageSuspended_installedSuspendedPackage_shouldReturnTrue()
      throws NameNotFoundException {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME},
        /* suspended= */ true,
        /* appExtras= */ null,
        /* launcherExtras= */ null,
        /* dialogMessage= */ (String) null);
    assertThat(packageManager.isPackageSuspended(TEST_PACKAGE_NAME)).isTrue();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void isPackageSuspended_installedSuspendedPackage_suspendDialogInfo_shouldReturnTrue()
      throws NameNotFoundException {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    packageManager.setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME},
        /* suspended= */ true,
        /* appExtras= */ null,
        /* launcherExtras= */ null,
        /* suspendDialogInfo= */ (SuspendDialogInfo) null);
    assertThat(packageManager.isPackageSuspended(TEST_PACKAGE_NAME)).isTrue();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void isPackageSuspended_installedUnsuspendedPackage_shouldReturnFalse()
      throws NameNotFoundException {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME},
        /* suspended= */ true,
        /* appExtras= */ null,
        /* launcherExtras= */ null,
        /* dialogMessage= */ (String) null);
    setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME},
        /* suspended= */ false,
        /* appExtras= */ null,
        /* launcherExtras= */ null,
        /* dialogMessage= */ (String) null);
    assertThat(packageManager.isPackageSuspended(TEST_PACKAGE_NAME)).isFalse();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void isPackageSuspended_installedUnsuspendedPackage_suspendDialogInfo_shouldReturnFalse()
      throws NameNotFoundException {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    packageManager.setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME},
        /* suspended= */ true,
        /* appExtras= */ null,
        /* launcherExtras= */ null,
        /* suspendDialogInfo= */ (SuspendDialogInfo) null);
    packageManager.setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME},
        /* suspended= */ false,
        /* appExtras= */ null,
        /* launcherExtras= */ null,
        /* suspendDialogInfo= */ (SuspendDialogInfo) null);
    assertThat(packageManager.isPackageSuspended(TEST_PACKAGE_NAME)).isFalse();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void setPackagesSuspended_withProfileOwner_shouldThrow() {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    shadowOf(devicePolicyManager)
        .setProfileOwner(new ComponentName("com.profile.owner", "ProfileOwnerClass"));
    try {
      setPackagesSuspended(
          new String[] {TEST_PACKAGE_NAME},
          /* suspended= */ true,
          /* appExtras= */ null,
          /* launcherExtras= */ null,
          /* dialogMessage= */ (String) null);
      fail("Should have thrown UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void setPackagesSuspended_withProfileOwner_suspendDialogInfo_shouldThrow() {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    shadowOf(devicePolicyManager)
        .setProfileOwner(new ComponentName("com.profile.owner", "ProfileOwnerClass"));
    try {
      packageManager.setPackagesSuspended(
          new String[] {TEST_PACKAGE_NAME},
          /* suspended= */ true,
          /* appExtras= */ null,
          /* launcherExtras= */ null,
          /* suspendDialogInfo= */ (SuspendDialogInfo) null);
      fail("Should have thrown UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void setPackagesSuspended_isProfileOwner_suspendDialogInfo() throws NameNotFoundException {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    String packageName = context.getPackageName();
    ComponentName componentName =
        new ComponentName(packageName, ActivityWithFilters.class.getName());
    shadowOf(devicePolicyManager).setProfileOwner(componentName);

    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    packageManager.setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME},
        /* suspended= */ true,
        /* appExtras= */ null,
        /* launcherExtras= */ null,
        /* suspendDialogInfo= */ (SuspendDialogInfo) null);
    assertThat(packageManager.isPackageSuspended(TEST_PACKAGE_NAME)).isTrue();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void setPackagesSuspended_withDeviceOwner_shouldThrow() {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    shadowOf(devicePolicyManager)
        .setDeviceOwner(new ComponentName("com.device.owner", "DeviceOwnerClass"));
    // Robolectric uses a random UID (see ShadowProcess#getRandomApplicationUid) that falls within
    // the range of the system user, so the device owner is on the current user and hence apps
    // cannot be suspended.
    try {
      setPackagesSuspended(
          new String[] {TEST_PACKAGE_NAME},
          /* suspended= */ true,
          /* appExtras= */ null,
          /* launcherExtras= */ null,
          /* dialogMessage= */ (String) null);
      fail("Should have thrown UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void setPackagesSuspended_withDeviceOwner_suspendDialogInfo_shouldThrow() {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    shadowOf(devicePolicyManager)
        .setDeviceOwner(new ComponentName("com.device.owner", "DeviceOwnerClass"));
    // Robolectric uses a random UID (see ShadowProcess#getRandomApplicationUid) that falls within
    // the range of the system user, so the device owner is on the current user and hence apps
    // cannot be suspended.
    try {
      packageManager.setPackagesSuspended(
          new String[] {TEST_PACKAGE_NAME},
          /* suspended= */ true,
          /* appExtras= */ null,
          /* launcherExtras= */ null,
          /* suspendDialogInfo= */ (SuspendDialogInfo) null);
      fail("Should have thrown UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
    }
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void setPackagesSuspended_isDeviceOwner_suspendDialogInfo() throws NameNotFoundException {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    String packageName = context.getPackageName();
    ComponentName componentName =
        new ComponentName(packageName, ActivityWithFilters.class.getName());
    shadowOf(devicePolicyManager).setDeviceOwner(componentName);

    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    packageManager.setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME},
        /* suspended= */ true,
        /* appExtras= */ null,
        /* launcherExtras= */ null,
        /* suspendDialogInfo= */ (SuspendDialogInfo) null);
    assertThat(packageManager.isPackageSuspended(TEST_PACKAGE_NAME)).isTrue();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void setPackagesSuspended_shouldSuspendSuspendablePackagesAndReturnTheRest()
      throws NameNotFoundException {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName("android"));
    shadowOf(packageManager)
        .installPackage(createPackageInfoWithPackageName("com.suspendable.package1"));
    shadowOf(packageManager)
        .installPackage(createPackageInfoWithPackageName("com.suspendable.package2"));

    assertThat(
            setPackagesSuspended(
                new String[] {
                  "com.nonexistent.package", // Unsuspenable (app doesn't exist).
                  "com.suspendable.package1",
                  "android", // Unsuspendable (platform package).
                  "com.suspendable.package2",
                  context.getPackageName() // Unsuspendable (caller's package).
                },
                /* suspended= */ true,
                /* appExtras= */ null,
                /* launcherExtras= */ null,
                /* dialogMessage= */ (String) null))
        .asList()
        .containsExactly("com.nonexistent.package", "android", context.getPackageName());

    assertThat(packageManager.isPackageSuspended("com.suspendable.package1")).isTrue();
    assertThat(packageManager.isPackageSuspended("android")).isFalse();
    assertThat(packageManager.isPackageSuspended("com.suspendable.package2")).isTrue();
    assertThat(packageManager.isPackageSuspended(context.getPackageName())).isFalse();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void
      setPackagesSuspended_suspendDialogInfo_shouldSuspendSuspendablePackagesAndReturnTheRest()
          throws NameNotFoundException {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName("android"));
    shadowOf(packageManager)
        .installPackage(createPackageInfoWithPackageName("com.suspendable.package1"));
    shadowOf(packageManager)
        .installPackage(createPackageInfoWithPackageName("com.suspendable.package2"));

    assertThat(
            packageManager.setPackagesSuspended(
                new String[] {
                  "com.nonexistent.package", // Unsuspenable (app doesn't exist).
                  "com.suspendable.package1",
                  "android", // Unsuspendable (platform package).
                  "com.suspendable.package2",
                  context.getPackageName() // Unsuspendable (caller's package).
                },
                /* suspended= */ true,
                /* appExtras= */ null,
                /* launcherExtras= */ null,
                /* suspendDialogInfo= */ (SuspendDialogInfo) null))
        .asList()
        .containsExactly("com.nonexistent.package", "android", context.getPackageName());

    assertThat(packageManager.isPackageSuspended("com.suspendable.package1")).isTrue();
    assertThat(packageManager.isPackageSuspended("android")).isFalse();
    assertThat(packageManager.isPackageSuspended("com.suspendable.package2")).isTrue();
    assertThat(packageManager.isPackageSuspended(context.getPackageName())).isFalse();
  }

  @Test(expected = SecurityException.class)
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void getUnsuspendablePackages_withoutSuspendAppsPermission_shouldThrow() {
    shadowOf(ApplicationProvider.<Application>getApplicationContext())
        .denyPermissions(SUSPEND_APPS);

    packageManager.getUnsuspendablePackages(new String[] {TEST_PACKAGE_NAME});
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void getUnsuspendablePackages_allPackagesSuspendable_shouldReturnEmpty() {
    shadowOf(ApplicationProvider.<Application>getApplicationContext())
        .grantPermissions(SUSPEND_APPS);

    assertThat(packageManager.getUnsuspendablePackages(new String[] {TEST_PACKAGE_NAME})).isEmpty();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void
      getUnsuspendablePackages_somePackagesSuspendableAndSomeNot_shouldReturnUnsuspendablePackages() {
    String dialerPackage = "dialer";
    String platformPackage = "android";
    shadowOf((Application) context).grantPermissions(SUSPEND_APPS);
    shadowOf(context.getSystemService(TelecomManager.class)).setDefaultDialerPackage(dialerPackage);

    assertThat(
            packageManager.getUnsuspendablePackages(
                new String[] {
                  "some.suspendable.app",
                  dialerPackage,
                  "some.other.suspendable.app",
                  platformPackage
                }))
        .asList()
        .containsExactly(dialerPackage, platformPackage);
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.O)
  public void getChangedPackages_negativeSequenceNumber_returnsNull() {
    shadowOf(packageManager).addChangedPackage(-5, TEST_PACKAGE_NAME);

    assertThat(packageManager.getChangedPackages(-5)).isNull();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.O)
  public void getChangedPackages_validSequenceNumber_withChangedPackages() {
    shadowOf(packageManager).addChangedPackage(0, TEST_PACKAGE_NAME);
    shadowOf(packageManager).addChangedPackage(0, TEST_PACKAGE2_NAME);
    shadowOf(packageManager).addChangedPackage(1, "appPackageName");

    ChangedPackages changedPackages = packageManager.getChangedPackages(0);
    assertThat(changedPackages.getSequenceNumber()).isEqualTo(1);
    assertThat(changedPackages.getPackageNames())
        .containsExactly(TEST_PACKAGE_NAME, TEST_PACKAGE2_NAME);
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.O)
  public void getChangedPackages_validSequenceNumber_noChangedPackages() {
    assertThat(packageManager.getChangedPackages(0)).isNull();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void setPackagesSuspended_shouldUnsuspendSuspendablePackagesAndReturnTheRest()
      throws NameNotFoundException {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName("android"));
    shadowOf(packageManager)
        .installPackage(createPackageInfoWithPackageName("com.suspendable.package1"));
    shadowOf(packageManager)
        .installPackage(createPackageInfoWithPackageName("com.suspendable.package2"));
    setPackagesSuspended(
        new String[] {
          "com.suspendable.package1", "com.suspendable.package2",
        },
        /* suspended= */ false,
        /* appExtras= */ null,
        /* launcherExtras= */ null,
        /* dialogMessage= */ (String) null);

    assertThat(
            setPackagesSuspended(
                new String[] {
                  "com.nonexistent.package", // Unsuspenable (app doesn't exist).
                  "com.suspendable.package1",
                  "android", // Unsuspendable (platform package).
                  "com.suspendable.package2",
                  context.getPackageName() // Unsuspendable (caller's package).
                },
                /* suspended= */ false,
                /* appExtras= */ null,
                /* launcherExtras= */ null,
                /* dialogMessage= */ (String) null))
        .asList()
        .containsExactly("com.nonexistent.package", "android", context.getPackageName());

    assertThat(packageManager.isPackageSuspended("com.suspendable.package1")).isFalse();
    assertThat(packageManager.isPackageSuspended("android")).isFalse();
    assertThat(packageManager.isPackageSuspended("com.suspendable.package2")).isFalse();
    assertThat(packageManager.isPackageSuspended(context.getPackageName())).isFalse();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void getPackageSetting_nonExistentPackage_shouldReturnNull() {
    assertThat(shadowOf(packageManager).getPackageSetting(TEST_PACKAGE_NAME)).isNull();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void getPackageSetting_removedPackage_shouldReturnNull() {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    shadowOf(packageManager).removePackage(TEST_PACKAGE_NAME);

    assertThat(shadowOf(packageManager).getPackageSetting(TEST_PACKAGE_NAME)).isNull();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void getPackageSetting_installedNeverSuspendedPackage_shouldReturnUnsuspendedSetting() {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));

    PackageSetting setting = shadowOf(packageManager).getPackageSetting(TEST_PACKAGE_NAME);

    assertThat(setting.isSuspended()).isFalse();
    assertThat(setting.getDialogMessage()).isNull();
    assertThat(setting.getSuspendedAppExtras()).isNull();
    assertThat(setting.getSuspendedLauncherExtras()).isNull();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void getPackageSetting_installedSuspendedPackage_shouldReturnSuspendedSetting() {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    PersistableBundle appExtras = new PersistableBundle();
    appExtras.putString("key", "value");
    PersistableBundle launcherExtras = new PersistableBundle();
    launcherExtras.putInt("number", 7);
    setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME}, true, appExtras, launcherExtras, "Dialog message");

    PackageSetting setting = shadowOf(packageManager).getPackageSetting(TEST_PACKAGE_NAME);

    assertThat(setting.isSuspended()).isTrue();
    assertThat(setting.getDialogMessage()).isEqualTo("Dialog message");
    assertThat(setting.getSuspendedAppExtras().getString("key")).isEqualTo("value");
    assertThat(setting.getSuspendedLauncherExtras().getInt("number")).isEqualTo(7);
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void
      getPackageSetting_installedSuspendedPackage_dialogInfo_shouldReturnSuspendedSetting() {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    PersistableBundle appExtras = new PersistableBundle();
    appExtras.putString("key", "value");
    PersistableBundle launcherExtras = new PersistableBundle();
    launcherExtras.putInt("number", 7);
    SuspendDialogInfo suspendDialogInfo =
        new SuspendDialogInfo.Builder()
            .setIcon(R.drawable.an_image)
            .setMessage("Dialog message")
            .setTitle(R.string.greeting)
            .setNeutralButtonText(R.string.copy)
            .build();

    packageManager.setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME}, true, appExtras, launcherExtras, suspendDialogInfo);

    PackageSetting setting = shadowOf(packageManager).getPackageSetting(TEST_PACKAGE_NAME);

    assertThat(setting.isSuspended()).isTrue();
    assertThat(setting.getDialogMessage()).isNull();
    assertThat(setting.getDialogInfo()).isEqualTo(suspendDialogInfo);
    assertThat(setting.getSuspendedAppExtras().getString("key")).isEqualTo("value");
    assertThat(setting.getSuspendedLauncherExtras().getInt("number")).isEqualTo(7);

    ShadowSuspendDialogInfo shadowDialogInfo = Shadow.extract(setting.getDialogInfo());
    assertThat(shadowDialogInfo.getDialogMessage()).isEqualTo("Dialog message");
    assertThat(shadowDialogInfo.getIconResId()).isEqualTo(R.drawable.an_image);
    assertThat(shadowDialogInfo.getTitleResId()).isEqualTo(R.string.greeting);
    assertThat(shadowDialogInfo.getNeutralButtonTextResId()).isEqualTo(R.string.copy);
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.P)
  public void getPackageSetting_installedUnsuspendedPackage_shouldReturnUnsuspendedSetting() {
    shadowOf(packageManager).installPackage(createPackageInfoWithPackageName(TEST_PACKAGE_NAME));
    PersistableBundle appExtras = new PersistableBundle();
    appExtras.putString("key", "value");
    PersistableBundle launcherExtras = new PersistableBundle();
    launcherExtras.putInt("number", 7);
    setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME}, true, appExtras, launcherExtras, "Dialog message");
    setPackagesSuspended(
        new String[] {TEST_PACKAGE_NAME}, false, appExtras, launcherExtras, "Dialog message");

    PackageSetting setting = shadowOf(packageManager).getPackageSetting(TEST_PACKAGE_NAME);

    assertThat(setting.isSuspended()).isFalse();
    assertThat(setting.getDialogMessage()).isNull();
    assertThat(setting.getSuspendedAppExtras()).isNull();
    assertThat(setting.getSuspendedLauncherExtras()).isNull();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.O)
  public void canRequestPackageInstalls_shouldReturnFalseByDefault() throws Exception {
    assertThat(packageManager.canRequestPackageInstalls()).isFalse();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.O)
  public void canRequestPackageInstalls_shouldReturnTrue_whenSetToTrue() throws Exception {
    shadowOf(packageManager).setCanRequestPackageInstalls(true);
    assertThat(packageManager.canRequestPackageInstalls()).isTrue();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.O)
  public void canRequestPackageInstalls_shouldReturnFalse_whenSetToFalse() throws Exception {
    shadowOf(packageManager).setCanRequestPackageInstalls(false);
    assertThat(packageManager.canRequestPackageInstalls()).isFalse();
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void getModule() throws Exception {
    ModuleInfo sentModuleInfo =
        ModuleInfoBuilder.newBuilder()
            .setName("test.module.name")
            .setPackageName("test.module.package.name")
            .setHidden(false)
            .build();
    shadowOf(packageManager).installModule(sentModuleInfo);

    ModuleInfo receivedModuleInfo =
        packageManager.getModuleInfo(sentModuleInfo.getPackageName(), 0);
    assertThat(receivedModuleInfo.getName().toString().contentEquals(sentModuleInfo.getName()))
        .isTrue();
    assertThat(receivedModuleInfo.getPackageName().equals(sentModuleInfo.getPackageName()))
        .isTrue();
    assertThat(receivedModuleInfo.isHidden()).isSameInstanceAs(sentModuleInfo.isHidden());
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void getInstalledModules() throws Exception {
    List<ModuleInfo> sentModuleInfos = new ArrayList<>();
    sentModuleInfos.add(
        ModuleInfoBuilder.newBuilder()
            .setName("test.module.name.one")
            .setPackageName("test.module.package.name.one")
            .setHidden(false)
            .build());
    sentModuleInfos.add(
        ModuleInfoBuilder.newBuilder()
            .setName("test.module.name.two")
            .setPackageName("test.module.package.name.two")
            .setHidden(false)
            .build());

    for (ModuleInfo sentModuleInfo : sentModuleInfos) {
      shadowOf(packageManager).installModule(sentModuleInfo);
    }

    List<ModuleInfo> receivedModuleInfos = packageManager.getInstalledModules(0);

    for (int i = 0; i < receivedModuleInfos.size(); i++) {
      assertThat(
              receivedModuleInfos
                  .get(i)
                  .getName()
                  .toString()
                  .contentEquals(sentModuleInfos.get(i).getName()))
          .isTrue();
      assertThat(
              receivedModuleInfos
                  .get(i)
                  .getPackageName()
                  .equals(sentModuleInfos.get(i).getPackageName()))
          .isTrue();
      assertThat(receivedModuleInfos.get(i).isHidden())
          .isSameInstanceAs(sentModuleInfos.get(i).isHidden());
    }
  }

  @Test
  @Config(minSdk = android.os.Build.VERSION_CODES.Q)
  public void deleteModule() throws Exception {
    ModuleInfo sentModuleInfo =
        ModuleInfoBuilder.newBuilder()
            .setName("test.module.name")
            .setPackageName("test.module.package.name")
            .setHidden(false)
            .build();
    shadowOf(packageManager).installModule(sentModuleInfo);

    ModuleInfo receivedModuleInfo =
        packageManager.getModuleInfo(sentModuleInfo.getPackageName(), 0);

    assertThat(receivedModuleInfo.getPackageName().equals(sentModuleInfo.getPackageName()))
        .isTrue();

    ModuleInfo deletedModuleInfo =
        (ModuleInfo) shadowOf(packageManager).deleteModule(sentModuleInfo.getPackageName());

    assertThat(deletedModuleInfo.getName().toString().contentEquals(sentModuleInfo.getName()))
        .isTrue();
    assertThat(deletedModuleInfo.getPackageName().equals(sentModuleInfo.getPackageName())).isTrue();
    assertThat(deletedModuleInfo.isHidden()).isSameInstanceAs(sentModuleInfo.isHidden());
  }

  @Test
  public void loadIcon_default() {
    ActivityInfo info = new ActivityInfo();
    info.applicationInfo = new ApplicationInfo();
    info.packageName = "testPackage";
    info.name = "testName";

    Drawable icon = info.loadIcon(packageManager);

    assertThat(icon).isNotNull();
  }

  @Test
  public void loadIcon_specified() {
    ActivityInfo info = new ActivityInfo();
    info.applicationInfo = new ApplicationInfo();
    info.packageName = "testPackage";
    info.name = "testName";
    info.icon = R.drawable.an_image;

    Drawable icon = info.loadIcon(packageManager);

    assertThat(icon).isNotNull();
  }

  @Test
  public void resolveInfoComparator() {
    ResolveInfo priority = new ResolveInfo();
    priority.priority = 100;
    ResolveInfo preferredOrder = new ResolveInfo();
    preferredOrder.preferredOrder = 100;
    ResolveInfo defaultResolveInfo = new ResolveInfo();

    ResolveInfo[] array = new ResolveInfo[] {priority, preferredOrder, defaultResolveInfo};
    Arrays.sort(array, new ResolveInfoComparator());

    assertThat(array)
        .asList()
        .containsExactly(preferredOrder, priority, defaultResolveInfo)
        .inOrder();
  }

  private static PackageInfo createPackageInfoWithPackageName(String packageName) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = packageName;
    packageInfo.applicationInfo.name = TEST_PACKAGE_LABEL;
    return packageInfo;
  }

  @Test
  public void addActivityIfNotPresent_newPackage() throws Exception {
    ComponentName componentName = new ComponentName("test.package", "Activity");
    shadowOf(packageManager).addActivityIfNotPresent(componentName);

    ActivityInfo activityInfo = packageManager.getActivityInfo(componentName, 0);

    assertThat(activityInfo).isNotNull();
    assertThat(activityInfo.packageName).isEqualTo("test.package");
    assertThat(activityInfo.name).isEqualTo("Activity");
  }

  @Test
  public void addActivityIfNotPresent_existing() throws Exception {
    String packageName = context.getPackageName();
    ComponentName componentName =
        new ComponentName(packageName, ActivityWithFilters.class.getName());
    shadowOf(packageManager).addActivityIfNotPresent(componentName);

    ActivityInfo activityInfo = packageManager.getActivityInfo(componentName, 0);

    assertThat(activityInfo).isNotNull();
    assertThat(activityInfo.packageName).isEqualTo(packageName);
    assertThat(activityInfo.name).isEqualTo(ActivityWithFilters.class.getName());
  }

  @Test
  public void addActivityIfNotPresent_newActivity() throws Exception {
    String packageName = context.getPackageName();
    ComponentName componentName = new ComponentName(packageName, "NewActivity");
    shadowOf(packageManager).addActivityIfNotPresent(componentName);

    ActivityInfo activityInfo = packageManager.getActivityInfo(componentName, 0);

    assertThat(activityInfo).isNotNull();
    assertThat(activityInfo.packageName).isEqualTo(packageName);
    assertThat(activityInfo.name).isEqualTo("NewActivity");
  }

  @Test
  public void setSafeMode() {
    assertThat(packageManager.isSafeMode()).isFalse();

    shadowOf(packageManager).setSafeMode(true);
    assertThat(packageManager.isSafeMode()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void setDistractingPackageRestrictions() {
    assertThat(
            packageManager.setDistractingPackageRestrictions(
                new String[] {TEST_PACKAGE_NAME, TEST_PACKAGE2_NAME},
                PackageManager.RESTRICTION_HIDE_FROM_SUGGESTIONS))
        .isEmpty();

    assertThat(shadowOf(packageManager).getDistractingPackageRestrictions(TEST_PACKAGE_NAME))
        .isEqualTo(PackageManager.RESTRICTION_HIDE_FROM_SUGGESTIONS);
    assertThat(shadowOf(packageManager).getDistractingPackageRestrictions(TEST_PACKAGE2_NAME))
        .isEqualTo(PackageManager.RESTRICTION_HIDE_FROM_SUGGESTIONS);
    assertThat(shadowOf(packageManager).getDistractingPackageRestrictions(TEST_PACKAGE3_NAME))
        .isEqualTo(PackageManager.RESTRICTION_NONE);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getPackagesHoldingPermissions_returnPackages() throws Exception {
    String permissionA = "com.android.providers.permission.test.a";
    String permissionB = "com.android.providers.permission.test.b";

    PackageInfo packageInfoA = new PackageInfo();
    packageInfoA.packageName = TEST_PACKAGE_NAME;
    packageInfoA.applicationInfo = new ApplicationInfo();
    packageInfoA.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfoA.requestedPermissions = new String[] {permissionA};

    PackageInfo packageInfoB = new PackageInfo();
    packageInfoB.packageName = TEST_PACKAGE2_NAME;
    packageInfoB.applicationInfo = new ApplicationInfo();
    packageInfoB.applicationInfo.packageName = TEST_PACKAGE2_NAME;
    packageInfoB.requestedPermissions = new String[] {permissionB};

    shadowOf(packageManager).installPackage(packageInfoA);
    shadowOf(packageManager).installPackage(packageInfoB);

    List<PackageInfo> result =
        packageManager.getPackagesHoldingPermissions(new String[] {permissionA, permissionB}, 0);

    assertThat(result).containsExactly(packageInfoA, packageInfoB);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getPackagesHoldingPermissions_returnsEmpty() throws Exception {
    String permissionA = "com.android.providers.permission.test.a";

    PackageInfo packageInfoA = new PackageInfo();
    packageInfoA.packageName = TEST_PACKAGE_NAME;
    packageInfoA.applicationInfo = new ApplicationInfo();
    packageInfoA.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfoA.requestedPermissions = new String[] {permissionA};

    shadowOf(packageManager).installPackage(packageInfoA);

    List<PackageInfo> result =
        packageManager.getPackagesHoldingPermissions(
            new String[] {"com.android.providers.permission.test.b"}, 0);

    assertThat(result).isEmpty();
  }

  @Test
  @Config(minSdk = O)
  public void isInstantApp() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo
        .applicationInfo
        .getClass()
        .getDeclaredField("privateFlags")
        .setInt(packageInfo.applicationInfo, /*ApplicationInfo.PRIVATE_FLAG_INSTANT*/ 1 << 7);

    shadowOf(packageManager).installPackage(packageInfo);

    assertThat(packageManager.isInstantApp(TEST_PACKAGE_NAME)).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void isInstantApp_falseDefault() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = TEST_PACKAGE_NAME;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = TEST_PACKAGE_NAME;

    shadowOf(packageManager).installPackage(packageInfo);

    assertThat(packageManager.isInstantApp(TEST_PACKAGE_NAME)).isFalse();
  }

  @Test
  public void getText_stringAdded_originalStringExists_returnsUserAddedString() {
    shadowOf(packageManager)
        .addStringResource(context.getPackageName(), R.string.hello, "fake hello");

    assertThat(
            packageManager
                .getText(context.getPackageName(), R.string.hello, context.getApplicationInfo())
                .toString())
        .isEqualTo("fake hello");
  }

  @Test
  public void getText_stringAdded_originalStringDoesNotExists_returnsUserAddedString() {
    shadowOf(packageManager).addStringResource(TEST_PACKAGE_NAME, 1, "package1 resId1");
    shadowOf(packageManager).addStringResource(TEST_PACKAGE_NAME, 2, "package1 resId2");
    shadowOf(packageManager).addStringResource(TEST_PACKAGE2_NAME, 1, "package2 resId1");
    shadowOf(packageManager).addStringResource(TEST_PACKAGE2_NAME, 3, "package2 resId3");

    assertThat(packageManager.getText(TEST_PACKAGE_NAME, 1, new ApplicationInfo()).toString())
        .isEqualTo("package1 resId1");
    assertThat(packageManager.getText(TEST_PACKAGE_NAME, 2, new ApplicationInfo()).toString())
        .isEqualTo("package1 resId2");
    assertThat(packageManager.getText(TEST_PACKAGE2_NAME, 1, new ApplicationInfo()).toString())
        .isEqualTo("package2 resId1");
    assertThat(packageManager.getText(TEST_PACKAGE2_NAME, 3, new ApplicationInfo()).toString())
        .isEqualTo("package2 resId3");
  }

  @Test
  public void getText_stringAddedTwice_originalStringDoesNotExists_returnsNewlyUserAddedString() {
    shadowOf(packageManager).addStringResource(TEST_PACKAGE_NAME, 1, "package1 resId1");
    shadowOf(packageManager).addStringResource(TEST_PACKAGE_NAME, 1, "package1 resId2 new");

    assertThat(packageManager.getText(TEST_PACKAGE_NAME, 1, new ApplicationInfo()).toString())
        .isEqualTo("package1 resId2 new");
  }

  @Test
  public void getText_stringNotAdded_originalStringExists_returnsOriginalText() {
    shadowOf(packageManager).addStringResource(context.getPackageName(), 1, "fake");
    shadowOf(packageManager).addStringResource(TEST_PACKAGE_NAME, R.string.hello, "fake hello");

    assertThat(
            packageManager
                .getText(context.getPackageName(), R.string.hello, context.getApplicationInfo())
                .toString())
        .isEqualTo(context.getString(R.string.hello));
  }

  @Test
  public void getText_stringNotAdded_originalStringDoesNotExists_returnsNull() {
    assertThat(packageManager.getText(context.getPackageName(), 1, context.getApplicationInfo()))
        .isNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void setAutoRevokeWhitelisted() {
    assertThat(packageManager.isAutoRevokeWhitelisted()).isFalse();

    shadowOf(packageManager).setAutoRevokeWhitelisted(true);
    assertThat(packageManager.isAutoRevokeWhitelisted()).isTrue();
  }

  public String[] setPackagesSuspended(
      String[] packageNames,
      boolean suspended,
      PersistableBundle appExtras,
      PersistableBundle launcherExtras,
      String dialogMessage) {
    return packageManager.setPackagesSuspended(
        packageNames, suspended, appExtras, launcherExtras, dialogMessage);
  }
}
