package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Process;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowStorageStatsManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.O)
public final class ShadowStorageStatsManagerTest {

  private StorageStatsManager storageStatsManager;

  @Before
  public void setUp() {
    storageStatsManager =
        (StorageStatsManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.STORAGE_STATS_SERVICE);
  }

  @Test
  public void getFreeBytes_defaultUuid_shouldReturnDefaultValue() throws Exception {
    // Act
    long defaultFreeBytes = shadowOf(storageStatsManager).getFreeBytes(StorageManager.UUID_DEFAULT);

    // Assert
    assertThat(defaultFreeBytes).isEqualTo(ShadowStorageStatsManager.DEFAULT_STORAGE_FREE_BYTES);
  }

  @Test
  public void getFreeBytes_unregisteredUuid_throwsException() {
    // Arrange
    UUID newUuid = UUID.randomUUID();

    // Act & Assert
    assertThrows(IOException.class, () -> shadowOf(storageStatsManager).getFreeBytes(newUuid));
  }

  @Test
  public void getFreeBytes_registeredNewUuid_returnSetupValue() throws Exception {
    // Arrange
    UUID newUuid = UUID.randomUUID();
    long expectedFreeBytes = 16 * 1024L;
    long expectedTotalBytes = 32 * 1024L;
    shadowOf(storageStatsManager)
        .setStorageDeviceFreeAndTotalBytes(newUuid, expectedFreeBytes, expectedTotalBytes);

    // Act
    long defaultFreeBytes = shadowOf(storageStatsManager).getFreeBytes(StorageManager.UUID_DEFAULT);
    long newUuidFreeBytes = shadowOf(storageStatsManager).getFreeBytes(newUuid);

    // Assert
    assertThat(defaultFreeBytes).isEqualTo(ShadowStorageStatsManager.DEFAULT_STORAGE_FREE_BYTES);
    assertThat(newUuidFreeBytes).isEqualTo(expectedFreeBytes);
  }

  @Test
  public void getFreeBytes_afterRemoveStorageDevice_throwsException() {
    // Arange
    shadowOf(storageStatsManager).removeStorageDevice(StorageManager.UUID_DEFAULT);

    // Act & Assert
    assertThrows(
        IOException.class,
        () -> shadowOf(storageStatsManager).getFreeBytes(StorageManager.UUID_DEFAULT));
  }

  @Test
  public void getTotalBytes_defaultUuid_shouldReturnDefaultValue() throws Exception {
    // Act
    long defaultTotalBytes =
        shadowOf(storageStatsManager).getTotalBytes(StorageManager.UUID_DEFAULT);

    // Assert
    assertThat(defaultTotalBytes).isEqualTo(ShadowStorageStatsManager.DEFAULT_STORAGE_TOTAL_BYTES);
  }

  @Test
  public void getTotalBytes_unregisteredUuid_throwsException() {
    // Arrange
    UUID newUuid = UUID.randomUUID();

    // Act & Assert
    assertThrows(IOException.class, () -> shadowOf(storageStatsManager).getTotalBytes(newUuid));
  }

  @Test
  public void getTotalBytes_registeredNewUuid_returnSetupValue() throws Exception {
    // Arrange
    UUID newUuid = UUID.randomUUID();
    long expectedFreeBytes = 16 * 1024L;
    long expectedTotalBytes = 32 * 1024L;
    shadowOf(storageStatsManager)
        .setStorageDeviceFreeAndTotalBytes(newUuid, expectedFreeBytes, expectedTotalBytes);

    // Act
    long defaultTotalBytes =
        shadowOf(storageStatsManager).getTotalBytes(StorageManager.UUID_DEFAULT);
    long newUuidTotalBytes = shadowOf(storageStatsManager).getTotalBytes(newUuid);

    // Assert
    assertThat(defaultTotalBytes).isEqualTo(ShadowStorageStatsManager.DEFAULT_STORAGE_TOTAL_BYTES);
    assertThat(newUuidTotalBytes).isEqualTo(expectedTotalBytes);
  }

  @Test
  public void getTotalBytes_afterRemoveStorageDevice_throwsException() {
    // Arange
    shadowOf(storageStatsManager).removeStorageDevice(StorageManager.UUID_DEFAULT);

    // Act & Assert
    assertThrows(
        IOException.class,
        () -> shadowOf(storageStatsManager).getTotalBytes(StorageManager.UUID_DEFAULT));
  }

  @Test
  public void queryWithoutSetup_shouldFail() {
    assertThrows(
        PackageManager.NameNotFoundException.class,
        () ->
            shadowOf(storageStatsManager)
                .queryStatsForPackage(
                    UUID.randomUUID(), "somePackageName", Process.myUserHandle()));
  }

  @Test
  public void queryWithCorrectArguments_shouldReturnSetupValue() throws Exception {
    // Arrange
    StorageStats expected = buildStorageStats();
    UUID uuid = UUID.randomUUID();
    String packageName = "somePackageName";
    UserHandle userHandle = Process.myUserHandle();
    shadowOf(storageStatsManager).addStorageStats(uuid, packageName, userHandle, expected);

    // Act
    StorageStats actual =
        shadowOf(storageStatsManager).queryStatsForPackage(uuid, packageName, userHandle);

    // Assert
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void queryWithWrongArguments_shouldFail() {
    // Arrange
    StorageStats expected = buildStorageStats();
    UUID uuid = UUID.randomUUID();
    UUID differentUUID = UUID.randomUUID();
    String packageName = "somePackageName";
    UserHandle userHandle = UserHandle.getUserHandleForUid(0);
    // getUserHandleForUid will divide uid by 100000. Pass in some arbitrary number > 100000 to be
    // different from system uid 0.
    UserHandle differentUserHandle = UserHandle.getUserHandleForUid(1200000);

    assertThat(uuid).isNotEqualTo(differentUUID);
    assertThat(userHandle).isNotEqualTo(differentUserHandle);

    // Act
    shadowOf(storageStatsManager).addStorageStats(uuid, packageName, userHandle, expected);

    // Assert
    assertThrows(
        PackageManager.NameNotFoundException.class,
        () ->
            shadowOf(storageStatsManager)
                .queryStatsForPackage(uuid, "differentPackageName", userHandle));

    assertThrows(
        PackageManager.NameNotFoundException.class,
        () ->
            shadowOf(storageStatsManager)
                .queryStatsForPackage(differentUUID, packageName, userHandle));

    assertThrows(
        PackageManager.NameNotFoundException.class,
        () ->
            shadowOf(storageStatsManager)
                .queryStatsForPackage(uuid, packageName, differentUserHandle));
  }

  @Test
  public void queryAfterClearSetup_shouldFail() {
    // Arrange
    StorageStats expected = buildStorageStats();
    UUID uuid = UUID.randomUUID();
    String packageName = "somePackageName";
    UserHandle userHandle = Process.myUserHandle();
    shadowOf(storageStatsManager).addStorageStats(uuid, packageName, userHandle, expected);

    // Act
    shadowOf(storageStatsManager).clearStorageStats();

    // Assert
    assertThrows(
        PackageManager.NameNotFoundException.class,
        () -> shadowOf(storageStatsManager).queryStatsForPackage(uuid, packageName, userHandle));
  }

  private static StorageStats buildStorageStats() {
    long codeSize = 3000L;
    long dataSize = 2000L;
    long cacheSize = 1000L;
    Parcel parcel = Parcel.obtain();
    parcel.writeLong(codeSize);
    parcel.writeLong(dataSize);
    parcel.writeLong(cacheSize);
    parcel.setDataPosition(0);
    return StorageStats.CREATOR.createFromParcel(parcel);
  }
}
