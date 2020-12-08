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
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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
  public void queryWithoutSetup_shouldFail() throws Exception {
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
  public void queryWithWrongArguments_shouldFail() throws Exception {
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
  public void queryAfterClearSetup_shouldFail() throws Exception {
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
