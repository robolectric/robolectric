package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.RuntimeEnvironment.getApplication;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.Parcel;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadows.ShadowStorageManager.StorageManagerReflector;
import org.robolectric.util.ReflectionHelpers;

/** Unit tests for {@link ShadowStorageManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowStorageManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private final String internalStorage = "/storage/internal";
  private final String sdcardStorage = "/storage/sdcard";

  private StorageManager storageManager;

  @Before
  public void setUp() {
    storageManager = (StorageManager) getApplication().getSystemService(Context.STORAGE_SERVICE);
  }

  @Test
  public void getVolumeList() {
    assertThat(shadowOf(storageManager).getVolumeList()).isNotNull();
  }

  @Test
  @Config(minSdk = N)
  public void getStorageVolumes() {
    File file1 = new File(sdcardStorage);
    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file1, "sd card"));
    assertThat(shadowOf(storageManager).getStorageVolumes()).isNotNull();
  }

  @Test
  @Config(minSdk = N)
  public void getStorageVolumesHaveDifferentUUID() {
    File file1 = new File(sdcardStorage);
    File file2 = new File(internalStorage);

    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file1, "sd card"));
    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file2, "internal"));

    List<StorageVolume> volumeList = shadowOf(storageManager).getStorageVolumes();
    assertThat(volumeList).hasSize(2);
    StorageVolume storage1 = volumeList.get(0);
    StorageVolume storage2 = volumeList.get(1);
    assertThat(storage1.getUuid()).isNotEqualTo(storage2.getUuid());
  }

  @Test
  @Config(minSdk = N)
  public void getStorageVolume() {
    File file1 = new File(internalStorage);
    File file2 = new File(sdcardStorage);
    File file3 = new File(internalStorage, "test_folder");
    File file4 = new File(sdcardStorage, "test_folder");
    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file1, "internal"));
    assertThat(shadowOf(storageManager).getStorageVolume(file1)).isNotNull();
    assertThat(shadowOf(storageManager).getStorageVolume(file2)).isNull();
    assertThat(shadowOf(storageManager).getStorageVolume(file3)).isNotNull();
    assertThat(shadowOf(storageManager).getStorageVolume(file4)).isNull();
  }

  @Test
  @Config(minSdk = N, maxSdk = TIRAMISU)
  public void isFileEncryptedNativeOrEmulated() {
    shadowOf(storageManager).setFileEncryptedNativeOrEmulated(true);
    // Use reflection, as this method is planned to be removed from StorageManager in V.
    assertThat(
            (boolean)
                ReflectionHelpers.callStaticMethod(
                    StorageManager.class, "isFileEncryptedNativeOrEmulated"))
        .isTrue();
  }

  @Test
  @Config(minSdk = N, maxSdk = UPSIDE_DOWN_CAKE)
  public void isUserKeyUnlocked() {
    shadowOf(getApplication().getSystemService(UserManager.class)).setUserUnlocked(true);
    // Use reflection, as this method is planned to be removed from StorageManager in V.
    assertThat(
            (boolean)
                ReflectionHelpers.callStaticMethod(
                    StorageManager.class,
                    "isUserKeyUnlocked",
                    ReflectionHelpers.ClassParameter.from(int.class, 0)))
        .isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void getStorageVolumeFromAnUserContext() {
    File file1 = new File(internalStorage);
    shadowOf(storageManager).addStorageVolume(buildAndGetStorageVolume(file1, "internal"));
    Context userContext;

    try {
      userContext =
          getApplication().createPackageContextAsUser("system", 0, UserHandle.of(0 /* userId */));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    StorageManager anotherStorageManager = userContext.getSystemService(StorageManager.class);
    assertThat(shadowOf(anotherStorageManager).getStorageVolume(file1)).isNotNull();
  }

  private StorageVolume buildAndGetStorageVolume(File file, String description) {
    Parcel parcel = Parcel.obtain();
    parcel.writeInt(0);
    parcel.setDataPosition(0);
    UserHandle userHandle = new UserHandle(parcel);
    StorageVolumeBuilder storageVolumeBuilder =
        new StorageVolumeBuilder("volume " + description, file, description, userHandle, "mounted");
    return storageVolumeBuilder.build();
  }

  @Test
  @Config(minSdk = O)
  public void storageManager_activityContextEnabled_differentInstancesRetrieveVolumes() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      StorageManager applicationStorageManager =
          RuntimeEnvironment.getApplication().getSystemService(StorageManager.class);

      Activity activity = controller.get();
      StorageManager activityStorageManager = activity.getSystemService(StorageManager.class);

      assertThat(applicationStorageManager).isNotSameInstanceAs(activityStorageManager);

      List<StorageVolume> applicationVolumes = applicationStorageManager.getStorageVolumes();
      List<StorageVolume> activityVolumes = activityStorageManager.getStorageVolumes();

      assertThat(activityVolumes).isEqualTo(applicationVolumes);
    }
  }

  @Test
  @Config(minSdk = O)
  public void getUuidForPath_defaultStoragePaths_returnsDefaultUuid() throws Exception {
    Context application = getApplication();
    assertThat(storageManager.getUuidForPath(Environment.getDataDirectory()))
        .isEqualTo(StorageManager.UUID_DEFAULT);
    assertThat(storageManager.getUuidForPath(application.getCacheDir()))
        .isEqualTo(StorageManager.UUID_DEFAULT);
    assertThat(storageManager.getUuidForPath(application.getFilesDir()))
        .isEqualTo(StorageManager.UUID_DEFAULT);
    assertThat(
            storageManager.getUuidForPath(
                application.createDeviceProtectedStorageContext().getCacheDir()))
        .isEqualTo(StorageManager.UUID_DEFAULT);

    File otherDataDir =
        RuntimeEnvironment.getTempDirectory().create("com.example.other-dataDir").toFile();
    PackageInfo otherPackageInfo = new PackageInfo();
    otherPackageInfo.packageName = "com.example.other";
    otherPackageInfo.applicationInfo = new ApplicationInfo();
    otherPackageInfo.applicationInfo.packageName = "com.example.other";
    otherPackageInfo.applicationInfo.dataDir = otherDataDir.getAbsolutePath();
    otherPackageInfo.applicationInfo.credentialProtectedDataDir = otherDataDir.getAbsolutePath();
    shadowOf(application.getPackageManager()).installPackage(otherPackageInfo);
    Context otherContext = application.createPackageContext("com.example.other", 0);
    assertThat(storageManager.getUuidForPath(otherContext.getDataDir()))
        .isEqualTo(StorageManager.UUID_DEFAULT);

    ShadowEnvironment.setIsExternalStorageEmulated(true);
    assertThat(storageManager.getUuidForPath(application.getExternalCacheDir()))
        .isEqualTo(StorageManager.UUID_DEFAULT);
  }

  @Test
  @Config(minSdk = O)
  public void getUuidForPath_addedStorageVolumes_returnsLongestMatchingVolumeUuid()
      throws Exception {
    UUID outerUuid = UUID.randomUUID();
    UUID nestedUuid = UUID.randomUUID();
    File outerDir = new File(sdcardStorage);
    File nestedDir = new File(sdcardStorage, "nested_mount");

    shadowOf(storageManager)
        .addStorageVolume(buildStorageVolumeWithFsUuid(nestedDir, "nested", nestedUuid.toString()));
    shadowOf(storageManager)
        .addStorageVolume(buildStorageVolumeWithFsUuid(outerDir, "outer", outerUuid.toString()));

    assertThat(storageManager.getUuidForPath(new File(outerDir, "photo.jpg"))).isEqualTo(outerUuid);
    assertThat(storageManager.getUuidForPath(new File(nestedDir, "photo.jpg")))
        .isEqualTo(nestedUuid);
  }

  @Test
  @Config(minSdk = O)
  public void getUuidForPath_unknownOrInvalidUuidVolume_throwsFileNotFoundException() {
    File invalidUuidVolumeDir = new File(sdcardStorage);
    shadowOf(storageManager)
        .addStorageVolume(
            buildStorageVolumeWithFsUuid(invalidUuidVolumeDir, "invalid", "invalid-fs-uuid"));

    assertThrows(
        FileNotFoundException.class,
        () -> storageManager.getUuidForPath(new File(invalidUuidVolumeDir, "file.txt")));
    assertThrows(
        FileNotFoundException.class,
        () -> storageManager.getUuidForPath(new File("/unmounted/unknown/path")));
  }

  @Test
  @Config(minSdk = O)
  public void getCacheQuotaBytes_defaultAndConfiguredQuotas() throws Exception {
    long expectedAndroidDefaultQuota = 64L * 1024 * 1024;
    assertThat(storageManager.getCacheQuotaBytes(StorageManager.UUID_DEFAULT))
        .isEqualTo(expectedAndroidDefaultQuota);

    shadowOf(storageManager).setCacheQuotaBytes(StorageManager.UUID_DEFAULT, 0L);
    assertThat(storageManager.getCacheQuotaBytes(StorageManager.UUID_DEFAULT)).isEqualTo(0L);

    long configuredDefaultQuota = 96L * 1024 * 1024;
    shadowOf(storageManager)
        .setCacheQuotaBytes(StorageManager.UUID_DEFAULT, configuredDefaultQuota);
    assertThat(storageManager.getCacheQuotaBytes(StorageManager.UUID_DEFAULT))
        .isEqualTo(configuredDefaultQuota);

    UUID volumeUuid = UUID.randomUUID();
    shadowOf(storageManager)
        .addStorageVolume(
            buildStorageVolumeWithFsUuid(new File(sdcardStorage), "sdcard", volumeUuid.toString()));
    assertThat(storageManager.getCacheQuotaBytes(volumeUuid))
        .isEqualTo(expectedAndroidDefaultQuota);

    long configuredVolumeQuota = 128L * 1024 * 1024;
    shadowOf(storageManager).setCacheQuotaBytes(volumeUuid, configuredVolumeQuota);
    assertThat(storageManager.getCacheQuotaBytes(volumeUuid)).isEqualTo(configuredVolumeQuota);

    assertThrows(
        IllegalArgumentException.class,
        () -> shadowOf(storageManager).setCacheQuotaBytes(StorageManager.UUID_DEFAULT, -1L));
  }

  @Test
  @Config(minSdk = S)
  public void storageVolume_getStorageUuid_supportedByGetUuidForPathAndGetCacheQuotaBytes()
      throws Exception {
    File builderVolumeDir = new File(sdcardStorage);
    StorageVolume builderVolume = buildAndGetStorageVolume(builderVolumeDir, "sdcard");
    shadowOf(storageManager).addStorageVolume(builderVolume);

    UUID storageVolumeUuid = builderVolume.getStorageUuid();
    assertThat(storageVolumeUuid).isNotNull();
    assertThat(storageManager.getCacheQuotaBytes(storageVolumeUuid)).isEqualTo(64L * 1024 * 1024);

    long configuredQuota = 160L * 1024 * 1024;
    long configuredAllocatableBytes = 512L * 1024 * 1024;
    shadowOf(storageManager).setCacheQuotaBytes(storageVolumeUuid, configuredQuota);
    shadowOf(storageManager).setAllocatableBytes(storageVolumeUuid, configuredAllocatableBytes);
    UUID pathUuid = storageManager.getUuidForPath(new File(builderVolumeDir, "file.bin"));
    assertThat(storageManager.getCacheQuotaBytes(pathUuid)).isEqualTo(configuredQuota);
    assertThat(storageManager.getAllocatableBytes(pathUuid)).isEqualTo(configuredAllocatableBytes);

    File nullFsUuidDir = new File(internalStorage);
    StorageVolume nullFsUuidVolume = buildStorageVolumeWithFsUuid(nullFsUuidDir, "null-fs", null);
    shadowOf(storageManager).addStorageVolume(nullFsUuidVolume);
    assertThat(storageManager.getUuidForPath(new File(nullFsUuidDir, "file.bin")))
        .isEqualTo(nullFsUuidVolume.getStorageUuid());
  }

  @Test
  @Config(minSdk = O)
  public void getCacheQuotaBytes_activityContextEnabled_sharesConfiguredQuota() throws Exception {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");
    long expectedQuota = 96L * 1024 * 1024;

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      StorageManager applicationStorageManager =
          RuntimeEnvironment.getApplication().getSystemService(StorageManager.class);
      StorageManager activityStorageManager =
          controller.get().getSystemService(StorageManager.class);

      shadowOf(applicationStorageManager)
          .setCacheQuotaBytes(StorageManager.UUID_DEFAULT, expectedQuota);

      assertThat(activityStorageManager.getCacheQuotaBytes(StorageManager.UUID_DEFAULT))
          .isEqualTo(expectedQuota);
    }
  }

  @Test
  @Config(minSdk = O)
  public void getCacheQuotaBytes_unknownUuid_throwsIoException() {
    assertThrows(IOException.class, () -> storageManager.getCacheQuotaBytes(UUID.randomUUID()));
  }

  @Test
  @Config(minSdk = O)
  public void getAllocatableBytes_defaultAndConfiguredValues() throws Exception {
    assertThat(storageManager.getAllocatableBytes(StorageManager.UUID_DEFAULT)).isGreaterThan(0L);

    shadowOf(storageManager).setAllocatableBytes(StorageManager.UUID_DEFAULT, 0L);
    assertThat(storageManager.getAllocatableBytes(StorageManager.UUID_DEFAULT)).isEqualTo(0L);

    long configuredDefaultBytes = 512L * 1024 * 1024;
    shadowOf(storageManager)
        .setAllocatableBytes(StorageManager.UUID_DEFAULT, configuredDefaultBytes);
    assertThat(storageManager.getAllocatableBytes(StorageManager.UUID_DEFAULT))
        .isEqualTo(configuredDefaultBytes);
    long flaggedAllocatableBytes =
        reflector(StorageManagerReflector.class, storageManager)
            .getAllocatableBytes(StorageManager.UUID_DEFAULT, 1);
    assertThat(flaggedAllocatableBytes).isEqualTo(configuredDefaultBytes);

    UUID volumeUuid = UUID.randomUUID();
    shadowOf(storageManager)
        .addStorageVolume(
            buildStorageVolumeWithFsUuid(new File(sdcardStorage), "sdcard", volumeUuid.toString()));
    assertThat(storageManager.getAllocatableBytes(volumeUuid)).isGreaterThan(0L);

    long configuredVolumeBytes = 256L * 1024 * 1024;
    shadowOf(storageManager).setAllocatableBytes(volumeUuid, configuredVolumeBytes);
    assertThat(storageManager.getAllocatableBytes(volumeUuid)).isEqualTo(configuredVolumeBytes);

    assertThrows(
        IllegalArgumentException.class,
        () -> shadowOf(storageManager).setAllocatableBytes(StorageManager.UUID_DEFAULT, -1L));
  }

  @Test
  @Config(minSdk = O)
  public void getAllocatableBytes_usesRegisteredShadowStatFsWhenUnconfigured() throws Exception {
    ShadowStatFs.registerStats(Environment.getDataDirectory(), 1000, 250, 200);
    assertThat(storageManager.getAllocatableBytes(StorageManager.UUID_DEFAULT))
        .isEqualTo(200L * ShadowStatFs.BLOCK_SIZE);

    ShadowStatFs.registerStats(Environment.getDataDirectory(), 1000, 0, 0);
    assertThat(storageManager.getAllocatableBytes(StorageManager.UUID_DEFAULT)).isEqualTo(0L);

    long explicitOverrideBytes = 64L * 1024 * 1024;
    shadowOf(storageManager)
        .setAllocatableBytes(StorageManager.UUID_DEFAULT, explicitOverrideBytes);
    assertThat(storageManager.getAllocatableBytes(StorageManager.UUID_DEFAULT))
        .isEqualTo(explicitOverrideBytes);

    File volumeDir = new File(sdcardStorage);
    UUID volumeUuid = UUID.randomUUID();
    shadowOf(storageManager)
        .addStorageVolume(buildStorageVolumeWithFsUuid(volumeDir, "sdcard", volumeUuid.toString()));
    ShadowStatFs.registerStats(volumeDir, 500, 100, 80);
    assertThat(storageManager.getAllocatableBytes(volumeUuid))
        .isEqualTo(80L * ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  @Config(minSdk = O)
  public void getAllocatableBytes_activityContextEnabled_sharesConfiguredBytes() throws Exception {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");
    long expectedBytes = 256L * 1024 * 1024;

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      StorageManager applicationStorageManager =
          RuntimeEnvironment.getApplication().getSystemService(StorageManager.class);
      StorageManager activityStorageManager =
          controller.get().getSystemService(StorageManager.class);

      shadowOf(applicationStorageManager)
          .setAllocatableBytes(StorageManager.UUID_DEFAULT, expectedBytes);

      assertThat(activityStorageManager.getAllocatableBytes(StorageManager.UUID_DEFAULT))
          .isEqualTo(expectedBytes);
    }
  }

  @Test
  @Config(minSdk = O)
  public void getAllocatableBytes_unknownUuid_throwsIoException() {
    assertThrows(IOException.class, () -> storageManager.getAllocatableBytes(UUID.randomUUID()));
  }

  private StorageVolume buildStorageVolumeWithFsUuid(File file, String description, String fsUuid) {
    return new StorageVolumeBuilder(
            "volume " + description, file, description, UserHandle.of(0), "mounted")
        .setFsUuid(fsUuid)
        .build();
  }
}
