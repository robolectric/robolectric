package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.content.pm.VersionedPackage;
import android.content.rollback.PackageRollbackInfo;
import android.os.Build;
import android.util.IntArray;
import android.util.SparseLongArray;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Unit tests for {@link PackageRollbackInfoBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.Q)
public final class PackageRollbackInfoBuilderTest {
  private static final int BACKUP_ID = 1;
  private static final int INSTALLED_USER_ID = 10;
  private static final int SNAPSHOTTED_USER_ID = 11;
  private static final int RESTORE_INFO_USER_ID = 2;
  private static final int RESTORE_INFO_APP_ID = 3;
  private static final String RESTORE_INFO_SEINFO = "fake_seinfo";
  private static final VersionedPackage packageRolledBackFrom =
      new VersionedPackage("test_package", 123);
  private static final VersionedPackage packageRolledBackTo =
      new VersionedPackage("test_package", 345);
  private static final SparseLongArray ceSnapshotInodes = new SparseLongArray();

  @Before
  public void setUp() {
    ceSnapshotInodes.append(1, 1L);
  }

  @Test
  public void build_throwsError_whenMissingPackageRolledBackFrom() {
    PackageRollbackInfoBuilder packageRollbackInfoBuilder = PackageRollbackInfoBuilder.newBuilder();

    NullPointerException expectedException =
        assertThrows(NullPointerException.class, packageRollbackInfoBuilder::build);
    assertThat(expectedException)
        .hasMessageThat()
        .isEqualTo("Mandatory field 'packageRolledBackFrom' missing.");
  }

  @Test
  public void build_throwsError_whenMissingPackageRolledBackTo() {
    PackageRollbackInfoBuilder packageRollbackInfoBuilder =
        PackageRollbackInfoBuilder.newBuilder().setPackageRolledBackFrom(packageRolledBackFrom);

    NullPointerException expectedException =
        assertThrows(NullPointerException.class, packageRollbackInfoBuilder::build);
    assertThat(expectedException)
        .hasMessageThat()
        .isEqualTo("Mandatory field 'packageRolledBackTo' missing.");
  }

  @Test
  public void build_withBasicFields() {
    PackageRollbackInfo packageRollbackInfo =
        PackageRollbackInfoBuilder.newBuilder()
            .setPackageRolledBackFrom(packageRolledBackFrom)
            .setPackageRolledBackTo(packageRolledBackTo)
            .build();

    assertThat(packageRollbackInfo).isNotNull();
    assertThat(packageRollbackInfo.getVersionRolledBackFrom()).isEqualTo(packageRolledBackFrom);
    assertThat(packageRollbackInfo.getVersionRolledBackTo()).isEqualTo(packageRolledBackTo);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.Q)
  public void build_onQ() {
    PackageRollbackInfo packageRollbackInfo =
        PackageRollbackInfoBuilder.newBuilder()
            .setPackageRolledBackFrom(packageRolledBackFrom)
            .setPackageRolledBackTo(packageRolledBackTo)
            .addPendingBackup(BACKUP_ID)
            .addPendingRestore(RESTORE_INFO_USER_ID, RESTORE_INFO_APP_ID, RESTORE_INFO_SEINFO)
            .setIsApex(true)
            .addInstalledUser(INSTALLED_USER_ID)
            .setCeSnapshotInodes(ceSnapshotInodes)
            .build();

    assertThat(packageRollbackInfo).isNotNull();
    assertThat(packageRollbackInfo.getVersionRolledBackFrom()).isEqualTo(packageRolledBackFrom);
    assertThat(packageRollbackInfo.getVersionRolledBackTo()).isEqualTo(packageRolledBackTo);
    int[] pendingBackups =
        ((IntArray) ReflectionHelpers.callInstanceMethod(packageRollbackInfo, "getPendingBackups"))
            .toArray();
    assertThat(pendingBackups).asList().containsExactly(BACKUP_ID);
    assertThat(packageRollbackInfo.getPendingRestores()).hasSize(1);
    assertThat(packageRollbackInfo.getPendingRestores().get(0).userId)
        .isEqualTo(RESTORE_INFO_USER_ID);
    assertThat(packageRollbackInfo.getPendingRestores().get(0).appId)
        .isEqualTo(RESTORE_INFO_APP_ID);
    assertThat(packageRollbackInfo.getPendingRestores().get(0).seInfo)
        .isEqualTo(RESTORE_INFO_SEINFO);
    assertThat(packageRollbackInfo.isApex()).isTrue();
    IntArray installedUsers =
        ReflectionHelpers.callInstanceMethod(packageRollbackInfo, "getInstalledUsers");
    assertThat(installedUsers.toArray()).hasLength(1);
    assertThat(installedUsers.get(0)).isEqualTo(INSTALLED_USER_ID);
    if (RuntimeEnvironment.getApiLevel() <= Build.VERSION_CODES.R) {
      assertThat(
              (SparseLongArray)
                  ReflectionHelpers.callInstanceMethod(packageRollbackInfo, "getCeSnapshotInodes"))
          .isEqualTo(ceSnapshotInodes);
    }
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.R)
  public void build_onR() {
    PackageRollbackInfo packageRollbackInfo =
        PackageRollbackInfoBuilder.newBuilder()
            .setPackageRolledBackFrom(packageRolledBackFrom)
            .setPackageRolledBackTo(packageRolledBackTo)
            .addPendingBackup(BACKUP_ID)
            .addPendingRestore(RESTORE_INFO_USER_ID, RESTORE_INFO_APP_ID, RESTORE_INFO_SEINFO)
            .setIsApex(true)
            .setIsApkInApex(true)
            .addSnapshottedUser(SNAPSHOTTED_USER_ID)
            .setCeSnapshotInodes(ceSnapshotInodes)
            .build();

    assertThat(packageRollbackInfo).isNotNull();
    assertThat(packageRollbackInfo.getVersionRolledBackFrom()).isEqualTo(packageRolledBackFrom);
    assertThat(packageRollbackInfo.getVersionRolledBackTo()).isEqualTo(packageRolledBackTo);
    int[] pendingBackups =
        ((IntArray) ReflectionHelpers.callInstanceMethod(packageRollbackInfo, "getPendingBackups"))
            .toArray();
    assertThat(pendingBackups).asList().containsExactly(BACKUP_ID);
    assertThat(packageRollbackInfo.getPendingRestores()).hasSize(1);
    assertThat(packageRollbackInfo.getPendingRestores().get(0).userId)
        .isEqualTo(RESTORE_INFO_USER_ID);
    assertThat(packageRollbackInfo.getPendingRestores().get(0).appId)
        .isEqualTo(RESTORE_INFO_APP_ID);
    assertThat(packageRollbackInfo.getPendingRestores().get(0).seInfo)
        .isEqualTo(RESTORE_INFO_SEINFO);
    assertThat(packageRollbackInfo.isApex()).isTrue();
    assertThat(packageRollbackInfo.isApkInApex()).isTrue();
    int[] snapshottedUsers =
        ((IntArray)
                ReflectionHelpers.callInstanceMethod(packageRollbackInfo, "getSnapshottedUsers"))
            .toArray();
    assertThat(snapshottedUsers).asList().containsExactly(SNAPSHOTTED_USER_ID);
    assertThat(
            (SparseLongArray)
                ReflectionHelpers.callInstanceMethod(packageRollbackInfo, "getCeSnapshotInodes"))
        .isEqualTo(ceSnapshotInodes);
  }
}
