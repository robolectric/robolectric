package org.robolectric.shadows;

import static android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.permission.PermissionControllerManager;
import android.permission.PermissionControllerManager.OnRevokeRuntimePermissionsCallback;
import androidx.test.core.app.ApplicationProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(minSdk = Q)
@RunWith(RobolectricTestRunner.class)
public final class ShadowPermissionControllerManagerTest {
  private Context context;
  private PackageManager packageManager;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    packageManager = context.getPackageManager();
  }

  @Test
  public void testRevokeRuntimePermission() {
    // Do not define a PermissionControllerManager class field.
    // System APIs are not available until the Robolectric classloader is initialized.
    PermissionControllerManager permissionControllerManager =
        context.getSystemService(PermissionControllerManager.class);
    int sharedUid = 12345;
    String permission1 = "com.test.permission1";
    String permission2 = "com.test.permission2";
    String packageName1 = "com.test.package1";
    PackageInfo packageInfo1 = new PackageInfo();
    packageInfo1.packageName = packageName1;
    packageInfo1.requestedPermissions = new String[] {permission1, permission2};
    packageInfo1.requestedPermissionsFlags =
        new int[] {REQUESTED_PERMISSION_GRANTED, REQUESTED_PERMISSION_GRANTED};
    packageInfo1.applicationInfo = new ApplicationInfo();
    packageInfo1.applicationInfo.uid = sharedUid;
    shadowOf(packageManager).installPackage(packageInfo1);
    String packageName2 = "com.test.package2";
    PackageInfo packageInfo2 = new PackageInfo();
    packageInfo2.packageName = packageName2;
    packageInfo2.requestedPermissions = new String[] {permission1, permission2};
    packageInfo2.requestedPermissionsFlags =
        new int[] {REQUESTED_PERMISSION_GRANTED, REQUESTED_PERMISSION_GRANTED};
    packageInfo2.applicationInfo = new ApplicationInfo();
    packageInfo2.applicationInfo.uid = sharedUid;
    shadowOf(packageManager).installPackage(packageInfo2);
    OnRevokeRuntimePermissionsCallback callback = mock(OnRevokeRuntimePermissionsCallback.class);

    ImmutableMap<String, List<String>> request1 =
        ImmutableMap.of(
            packageName1, ImmutableList.of(permission1),
            packageName2, ImmutableList.of(permission2));
    permissionControllerManager.revokeRuntimePermissions(
        request1, /* doDryRun= */ false, -1, context.getMainExecutor(), callback);
    shadowMainLooper().idle();

    verify(callback).onRevokeRuntimePermissions(eq(request1));
    assertThat(packageInfo1.requestedPermissionsFlags[0]).isEqualTo(0);
    assertThat(packageInfo1.requestedPermissionsFlags[1]).isEqualTo(REQUESTED_PERMISSION_GRANTED);
    assertThat(packageInfo2.requestedPermissionsFlags[0]).isEqualTo(REQUESTED_PERMISSION_GRANTED);
    assertThat(packageInfo2.requestedPermissionsFlags[1]).isEqualTo(0);

    ImmutableMap<String, List<String>> request2 =
        ImmutableMap.of(
            packageName1, ImmutableList.of(permission2),
            packageName2, ImmutableList.of(permission1));
    permissionControllerManager.revokeRuntimePermissions(
        request2, /* doDryRun= */ false, -1, context.getMainExecutor(), callback);
    shadowMainLooper().idle();

    verify(callback).onRevokeRuntimePermissions(eq(request2));
    assertThat(packageInfo1.requestedPermissionsFlags[0]).isEqualTo(0);
    assertThat(packageInfo1.requestedPermissionsFlags[1]).isEqualTo(0);
    assertThat(packageInfo2.requestedPermissionsFlags[0]).isEqualTo(0);
    assertThat(packageInfo2.requestedPermissionsFlags[1]).isEqualTo(0);
  }
}
