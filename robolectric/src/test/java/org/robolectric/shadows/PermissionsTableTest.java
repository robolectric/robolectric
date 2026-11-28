package org.robolectric.shadows;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests that Context and PackageManager share their permission grants through PermissionsTable. */
@RunWith(AndroidJUnit4.class)
public class PermissionsTableTest {
  // The test manifest requests INTERNET, a normal permission, but not CAMERA.
  private final Application context = ApplicationProvider.getApplicationContext();
  private final PackageManager packageManager = context.getPackageManager();
  private final String packageName = context.getPackageName();

  @Test
  public void defaults_agree() {
    assertThat(context.checkSelfPermission(INTERNET)).isEqualTo(PERMISSION_GRANTED);
    assertThat(packageManager.checkPermission(INTERNET, packageName)).isEqualTo(PERMISSION_GRANTED);
    assertThat(context.checkSelfPermission(CAMERA)).isEqualTo(PERMISSION_DENIED);
    assertThat(packageManager.checkPermission(CAMERA, packageName)).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void requestedPermissions_areGrantedLikeAtInstall() {
    installPackage("other.app", 10125, /* targetSdk= */ 0, /* flags= */ null);

    // INTERNET is a normal permission, which is granted at install time.
    assertThat(packageManager.checkPermission(INTERNET, "other.app")).isEqualTo(PERMISSION_GRANTED);
    assertThat(context.checkPermission(INTERNET, 4321, 10125)).isEqualTo(PERMISSION_GRANTED);
    // ACCESS_FINE_LOCATION is a runtime permission, which the app has to be granted.
    assertThat(packageManager.checkPermission(ACCESS_FINE_LOCATION, "other.app"))
        .isEqualTo(PERMISSION_DENIED);
    assertThat(context.checkPermission(ACCESS_FINE_LOCATION, 4321, 10125))
        .isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void runtimePermissions_areGrantedAtInstall_beforeTargetSdkM() {
    installPackage("other.app", 10125, /* targetSdk= */ 22, /* flags= */ null);

    assertThat(packageManager.checkPermission(ACCESS_FINE_LOCATION, "other.app"))
        .isEqualTo(PERMISSION_GRANTED);
    assertThat(context.checkPermission(ACCESS_FINE_LOCATION, 4321, 10125))
        .isEqualTo(PERMISSION_GRANTED);
  }

  @Test
  public void requestedPermissionsFlags_areSeenByContext() {
    installPackage(
        "other.app", 10125, /* targetSdk= */ 0, new int[] {0, REQUESTED_PERMISSION_GRANTED});

    assertThat(context.checkPermission(INTERNET, 4321, 10125)).isEqualTo(PERMISSION_DENIED);
    assertThat(context.checkPermission(ACCESS_FINE_LOCATION, 4321, 10125))
        .isEqualTo(PERMISSION_GRANTED);
  }

  @Test
  public void contextGrant_isSeenByPackageManager() {
    shadowOf(context).grantPermissions(CAMERA);

    assertThat(packageManager.checkPermission(CAMERA, packageName)).isEqualTo(PERMISSION_GRANTED);
  }

  @Test
  public void contextDeny_isSeenByPackageManager() {
    shadowOf(context).denyPermissions(INTERNET);

    assertThat(packageManager.checkPermission(INTERNET, packageName)).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void packageManagerRevoke_isSeenByContextAndPackageManager() {
    shadowOf(context).grantPermissions(INTERNET);

    packageManager.revokeRuntimePermission(packageName, INTERNET, Process.myUserHandle());

    assertThat(context.checkSelfPermission(INTERNET)).isEqualTo(PERMISSION_DENIED);
    assertThat(packageManager.checkPermission(INTERNET, packageName)).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void lastDecisionWins() {
    shadowOf(context).denyPermissions(INTERNET);
    packageManager.grantRuntimePermission(packageName, INTERNET, Process.myUserHandle());

    assertThat(context.checkSelfPermission(INTERNET)).isEqualTo(PERMISSION_GRANTED);

    shadowOf(context).denyPermissions(INTERNET);

    assertThat(packageManager.checkPermission(INTERNET, packageName)).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void packageManagerGrant_toOtherPackage_isSeenByContextForItsUid() {
    shadowOf(packageManager).installPackage(packageRequestingCamera("other.app", 10123));

    packageManager.grantRuntimePermission("other.app", CAMERA, Process.myUserHandle());

    assertThat(context.checkPermission(CAMERA, /* pid= */ 4321, 10123))
        .isEqualTo(PERMISSION_GRANTED);
    assertThat(context.checkPermission(CAMERA, /* pid= */ 4321, 10124))
        .isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void packagesWithoutUid_doNotShareGrants() {
    shadowOf(packageManager).installPackage(packageRequestingCamera("first.app", 0));
    shadowOf(packageManager).installPackage(packageRequestingCamera("second.app", 0));

    packageManager.grantRuntimePermission("first.app", CAMERA, Process.myUserHandle());

    assertThat(packageManager.checkPermission(CAMERA, "first.app")).isEqualTo(PERMISSION_GRANTED);
    assertThat(packageManager.checkPermission(CAMERA, "second.app")).isEqualTo(PERMISSION_DENIED);
  }

  private void installPackage(String packageName, int uid, int targetSdk, int[] flags) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = packageName;
    packageInfo.applicationInfo.uid = uid;
    packageInfo.applicationInfo.targetSdkVersion = targetSdk;
    packageInfo.requestedPermissions = new String[] {INTERNET, ACCESS_FINE_LOCATION};
    packageInfo.requestedPermissionsFlags = flags;
    shadowOf(packageManager).installPackage(packageInfo);
  }

  private static PackageInfo packageRequestingCamera(String packageName, int uid) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = packageName;
    packageInfo.applicationInfo.uid = uid;
    packageInfo.requestedPermissions = new String[] {CAMERA};
    packageInfo.requestedPermissionsFlags = new int[] {0};
    return packageInfo;
  }
}
