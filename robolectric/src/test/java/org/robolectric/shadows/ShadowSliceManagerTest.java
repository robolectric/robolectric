package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.slice.SliceManager;
import android.app.slice.SliceSpec;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.util.ArraySet;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
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
    PackageManager packageManager = RuntimeEnvironment.getApplication().getPackageManager();
    ShadowApplicationPackageManager shadowPackageManager =
        (ShadowApplicationPackageManager) shadowOf(packageManager);
    shadowPackageManager.setPackagesForUid(PACKAGE_1_UID, PACKAGE_NAME_1);
    shadowPackageManager.setPackagesForUid(PACKAGE_2_UID, PACKAGE_NAME_2);
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

  @Test
  public void testPinSlice_getPinnedSlicesReturnCorrectList() {
    SliceSpec sliceSpec = new SliceSpec("androidx.slice.BASIC", 1);
    sliceManager.pinSlice(sliceUri1, new HashSet<>(ImmutableList.of(sliceSpec)));

    assertThat(sliceManager.getPinnedSlices()).contains(sliceUri1);
    Set<SliceSpec> sliceSpecSet = sliceManager.getPinnedSpecs(sliceUri1);
    assertThat(sliceSpecSet).hasSize(1);
    assertThat(sliceSpecSet).contains(sliceSpec);
  }

  @Test
  public void testUnpinSlice_getPinnedSlicesReturnCorrectList() {
    SliceSpec sliceSpec = new SliceSpec("androidx.slice.BASIC", 1);
    sliceManager.pinSlice(sliceUri1, new HashSet<>(ImmutableList.of(sliceSpec)));
    sliceManager.unpinSlice(sliceUri1);

    assertThat(sliceManager.getPinnedSlices()).isEmpty();
    assertThat(sliceManager.getPinnedSpecs(sliceUri1)).isEmpty();
  }

  @Test
  public void sliceManager_activityContextEnabled_differentInstancesRetrieveSlices() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      SliceManager applicationSliceManager =
          ApplicationProvider.getApplicationContext().getSystemService(SliceManager.class);
      Activity activity = controller.get();
      SliceManager activitySliceManager = activity.getSystemService(SliceManager.class);

      assertThat(applicationSliceManager).isNotSameInstanceAs(activitySliceManager);

      Uri testUri = Uri.parse("content://com.example.slice/uri");

      Set<SliceSpec> specs = new ArraySet<>();
      specs.add(new SliceSpec("v1", 1));

      applicationSliceManager.pinSlice(testUri, specs);
      activitySliceManager.unpinSlice(testUri);

      Set<SliceSpec> applicationSpecs = applicationSliceManager.getPinnedSpecs(testUri);
      Set<SliceSpec> activitySpecs = activitySliceManager.getPinnedSpecs(testUri);
      assertThat(applicationSpecs).isEqualTo(activitySpecs);

      List<Uri> applicationPinnedSlices = applicationSliceManager.getPinnedSlices();
      List<Uri> activityPinnedSlices = activitySliceManager.getPinnedSlices();
      assertThat(applicationPinnedSlices).isEqualTo(activityPinnedSlices);

    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
