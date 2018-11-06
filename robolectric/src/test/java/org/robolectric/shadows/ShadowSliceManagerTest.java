package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.slice.SliceManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowSliceManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.P)
public final class ShadowSliceManagerTest {

  private static final String PACKAGE_NAME_1 = "com.google.testing.slicemanager.foo";
  private static final int PACKAGE_1_UID = 10;
  private Uri sliceUri1;
  private static final String PACKAGE_NAME_2 = "com.google.testing.slicemanager.bar";
  private static final int PACKAGE_2_UID = 20;
  private Uri sliceUri2;
  private SliceManager sliceManager;

  @Before
  public void setUp() {
    PackageManager packageManager = RuntimeEnvironment.application.getPackageManager();
    ShadowApplicationPackageManager shadowPackageManager =
        (ShadowApplicationPackageManager) shadowOf(packageManager);
    shadowPackageManager.setPackagesForUid(PACKAGE_1_UID, new String[] {PACKAGE_NAME_1});
    shadowPackageManager.setPackagesForUid(PACKAGE_2_UID, new String[] {PACKAGE_NAME_2});
    sliceUri1 = Uri.parse("content://a/b");
    sliceUri2 = Uri.parse("content://c/d");
    sliceManager = ApplicationProvider.getApplicationContext().getSystemService(SliceManager.class);
  }

  @Test
  public void testGrantSlicePermission_grantsPermissionToPackage() {
    sliceManager.grantSlicePermission(PACKAGE_NAME_1, sliceUri1);
    assertThat(sliceManager.checkSlicePermission(sliceUri1, /* pid= */ 1, PACKAGE_1_UID))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
  }

  @Test
  public void testGrantSlicePermission_doesNotGrantPermissionToOtherPackage() {
    sliceManager.grantSlicePermission(PACKAGE_NAME_1, sliceUri1);
    assertThat(sliceManager.checkSlicePermission(sliceUri1, /* pid= */ 1, PACKAGE_2_UID))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
  }

  @Test
  public void testGrantSlicePermission_doesNotGrantPermissionToOtherSliceUri() {
    sliceManager.grantSlicePermission(PACKAGE_NAME_1, sliceUri1);
    assertThat(sliceManager.checkSlicePermission(sliceUri2, /* pid= */ 1, PACKAGE_1_UID))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
  }

  @Test
  public void testRevokeSlicePermission_revokesPermissionToPackage() {
    sliceManager.grantSlicePermission(PACKAGE_NAME_1, sliceUri1);
    sliceManager.revokeSlicePermission(PACKAGE_NAME_1, sliceUri1);
    assertThat(sliceManager.checkSlicePermission(sliceUri1, /* pid= */ 1, PACKAGE_1_UID))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
  }
}
