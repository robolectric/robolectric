package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowWallpaperManager.WallpaperCommandRecord;

@RunWith(AndroidJUnit4.class)
public class ShadowWallpaperManagerTest {

  private static final Bitmap TEST_IMAGE_1 = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

  private static final Bitmap TEST_IMAGE_2 = Bitmap.createBitmap(3, 2, Bitmap.Config.ARGB_8888);

  private static final Bitmap TEST_IMAGE_3 = Bitmap.createBitmap(1, 5, Bitmap.Config.ARGB_8888);

  private static final int UNSUPPORTED_FLAG = WallpaperManager.FLAG_LOCK + 123;

  private static final String SET_WALLPAPER_COMPONENT =
      "android.permission.SET_WALLPAPER_COMPONENT";

  private static final ComponentName TEST_WALLPAPER_SERVICE =
      new ComponentName("org.robolectric", "org.robolectric.TestWallpaperService");

  private Application application;
  private WallpaperManager manager;

  @Before
  public void setUp() {
    application = ApplicationProvider.getApplicationContext();
    manager = WallpaperManager.getInstance(application);

    shadowOf(application).grantPermissions(SET_WALLPAPER_COMPONENT);
  }

  @Test
  public void getInstance_shouldCreateInstance() {
    assertThat(manager).isNotNull();
  }

  @Test
  public void sendWallpaperCommand_shouldTrackRecord() {
    manager.sendWallpaperCommand(null, null, 0, 0, 0, null);

    IBinder binder = new Binder();
    Bundle bundle = new Bundle();
    bundle.putString("key", "value");
    manager.sendWallpaperCommand(binder, "action", 1, 2, 3, bundle);

    List<WallpaperCommandRecord> records = shadowOf(manager).getWallpaperCommandRecords();

    assertThat(records).hasSize(2);

    WallpaperCommandRecord record0 = records.get(0);
    assertThat(record0.windowToken).isNull();
    assertThat(record0.action).isNull();
    assertThat(record0.x).isEqualTo(0);
    assertThat(record0.y).isEqualTo(0);
    assertThat(record0.z).isEqualTo(0);
    assertThat(record0.extras).isNull();

    WallpaperCommandRecord record1 = records.get(1);
    assertThat(record1.windowToken).isEqualTo(binder);
    assertThat(record1.action).isEqualTo("action");
    assertThat(record1.x).isEqualTo(1);
    assertThat(record1.y).isEqualTo(2);
    assertThat(record1.z).isEqualTo(3);
    assertThat(record1.extras.getString("key")).isEqualTo("value");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void hasResourceWallpaper_wallpaperResourceNotSet_returnsFalse() {
    assertThat(manager.hasResourceWallpaper(1)).isFalse();
    assertThat(manager.hasResourceWallpaper(5)).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void hasResourceWallpaper_wallpaperResourceSet_returnsTrue() throws IOException {
    int resid = 5;
    manager.setResource(resid);

    assertThat(manager.hasResourceWallpaper(1)).isFalse();
    assertThat(manager.hasResourceWallpaper(resid)).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void setResource_multipleTimes_hasResourceWallpaperReturnsTrueForLastValue()
      throws IOException {
    manager.setResource(1);
    manager.setResource(2);
    manager.setResource(3);

    assertThat(manager.hasResourceWallpaper(1)).isFalse();
    assertThat(manager.hasResourceWallpaper(2)).isFalse();
    assertThat(manager.hasResourceWallpaper(3)).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void setResource_invalidFlag_returnsZero() throws IOException {
    assertThat(manager.setResource(1, 0)).isEqualTo(0);
    assertThat(manager.hasResourceWallpaper(1)).isFalse();
    assertThat(manager.hasResourceWallpaper(2)).isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void setResource_lockScreenOnly_returnsNewId() throws IOException {
    assertThat(manager.setResource(1, WallpaperManager.FLAG_LOCK)).isEqualTo(1);
    assertThat(manager.hasResourceWallpaper(1)).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void setResource_homeScreenOnly_returnsNewId() throws IOException {
    assertThat(manager.setResource(1, WallpaperManager.FLAG_SYSTEM)).isEqualTo(1);
    assertThat(manager.hasResourceWallpaper(1)).isTrue();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_flagSystem_shouldCacheInMemory() throws Exception {
    int returnCode =
        manager.setBitmap(
            TEST_IMAGE_1,
            /* visibleCropHint= */ null,
            /* allowBackup= */ false,
            WallpaperManager.FLAG_SYSTEM);

    assertThat(returnCode).isEqualTo(1);
    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)).isEqualTo(TEST_IMAGE_1);
    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_liveWallpaperWasDefault_flagSystem_shouldRemoveLiveWallpaper()
      throws Exception {
    manager.setWallpaperComponent(TEST_WALLPAPER_SERVICE);

    manager.setBitmap(
        TEST_IMAGE_1,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_SYSTEM);

    assertThat(manager.getWallpaperInfo()).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_multipleCallsWithFlagSystem_shouldCacheLastBitmapInMemory()
      throws Exception {
    manager.setBitmap(
        TEST_IMAGE_1,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_SYSTEM);
    manager.setBitmap(
        TEST_IMAGE_2,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_SYSTEM);
    manager.setBitmap(
        TEST_IMAGE_3,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_SYSTEM);

    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)).isEqualTo(TEST_IMAGE_3);
    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_flagLock_shouldCacheInMemory() throws Exception {
    int returnCode =
        manager.setBitmap(
            TEST_IMAGE_2,
            /* visibleCropHint= */ null,
            /* allowBackup= */ false,
            WallpaperManager.FLAG_LOCK);

    assertThat(returnCode).isEqualTo(1);
    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)).isEqualTo(TEST_IMAGE_2);
    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_liveWallpaperWasDefault_flagLock_shouldRemoveLiveWallpaper()
      throws Exception {
    manager.setWallpaperComponent(TEST_WALLPAPER_SERVICE);

    manager.setBitmap(
        TEST_IMAGE_1,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_LOCK);

    assertThat(manager.getWallpaperInfo()).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_multipleCallsWithFlagLock_shouldCacheLastBitmapInMemory() throws Exception {
    manager.setBitmap(
        TEST_IMAGE_1,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_LOCK);
    manager.setBitmap(
        TEST_IMAGE_2,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_LOCK);
    manager.setBitmap(
        TEST_IMAGE_3,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_LOCK);

    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)).isEqualTo(TEST_IMAGE_3);
    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_unsupportedFlag_shouldNotCacheInMemory() throws Exception {
    int code =
        manager.setBitmap(
            TEST_IMAGE_1, /* visibleCropHint= */ null, /* allowBackup= */ false, UNSUPPORTED_FLAG);

    assertThat(code).isEqualTo(0);
    assertThat(shadowOf(manager).getBitmap(UNSUPPORTED_FLAG)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_liveWallpaperWasDefault_unsupportedFlag_shouldNotRemoveLiveWallpaper()
      throws Exception {
    manager.setWallpaperComponent(TEST_WALLPAPER_SERVICE);

    manager.setBitmap(
        TEST_IMAGE_1, /* visibleCropHint= */ null, /* allowBackup= */ false, UNSUPPORTED_FLAG);

    assertThat(manager.getWallpaperInfo().getComponent()).isEqualTo(TEST_WALLPAPER_SERVICE);
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_flagSystem_nothingCached_shouldReturnNull() throws Exception {
    assertThat(manager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_flagSystem_previouslyCached_shouldReturnParcelFileDescriptor()
      throws Exception {
    manager.setBitmap(
        TEST_IMAGE_1,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_SYSTEM);

    try (ParcelFileDescriptor parcelFileDescriptor =
        manager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)) {
    assertThat(getBytesFromFileDescriptor(parcelFileDescriptor.getFileDescriptor()))
        .isEqualTo(getBytesFromBitmap(TEST_IMAGE_1));
    }
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_flagLock_nothingCached_shouldReturnNull() throws Exception {
    assertThat(manager.getWallpaperFile(WallpaperManager.FLAG_LOCK)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_flagLock_previouslyCached_shouldReturnParcelFileDescriptor()
      throws Exception {
    manager.setBitmap(
        TEST_IMAGE_3,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_LOCK);

    try (ParcelFileDescriptor parcelFileDescriptor =
        manager.getWallpaperFile(WallpaperManager.FLAG_LOCK)) {
    assertThat(getBytesFromFileDescriptor(parcelFileDescriptor.getFileDescriptor()))
        .isEqualTo(getBytesFromBitmap(TEST_IMAGE_3));
    }
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_unsupportedFlag_shouldReturnNull() throws Exception {
    assertThat(manager.getWallpaperFile(UNSUPPORTED_FLAG)).isNull();
  }

  @Test
  @Config(minSdk = N)
  public void isSetWallpaperAllowed_allowed_shouldReturnTrue() {
    shadowOf(manager).setIsSetWallpaperAllowed(true);

    assertThat(manager.isSetWallpaperAllowed()).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void isSetWallpaperAllowed_disallowed_shouldReturnFalse() {
    shadowOf(manager).setIsSetWallpaperAllowed(false);

    assertThat(manager.isSetWallpaperAllowed()).isFalse();
  }

  @Test
  @Config(minSdk = M)
  public void isWallpaperSupported_supported_shouldReturnTrue() {
    shadowOf(manager).setIsWallpaperSupported(true);

    assertThat(manager.isWallpaperSupported()).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void isWallpaperSupported_unsupported_shouldReturnFalse() {
    shadowOf(manager).setIsWallpaperSupported(false);

    assertThat(manager.isWallpaperSupported()).isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void setStream_flagSystem_shouldCacheInMemory() throws Exception {
    InputStream inputStream = null;
    byte[] testImageBytes = getBytesFromBitmap(TEST_IMAGE_1);
    try {
      inputStream = new ByteArrayInputStream(testImageBytes);
      manager.setStream(
          inputStream,
          /* visibleCropHint= */ null,
          /* allowBackup= */ true,
          WallpaperManager.FLAG_SYSTEM);

      assertThat(getBytesFromBitmap(shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)))
          .isEqualTo(testImageBytes);
      assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)).isNull();
    } finally {
      close(inputStream);
    }
  }

  @Test
  @Config(minSdk = N)
  public void setStream_flagLock_shouldCacheInMemory() throws Exception {
    InputStream inputStream = null;
    byte[] testImageBytes = getBytesFromBitmap(TEST_IMAGE_2);
    try {
      inputStream = new ByteArrayInputStream(testImageBytes);
      manager.setStream(
          inputStream,
          /* visibleCropHint= */ null,
          /* allowBackup= */ true,
          WallpaperManager.FLAG_LOCK);

      assertThat(getBytesFromBitmap(shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)))
          .isEqualTo(testImageBytes);
      assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)).isNull();
    } finally {
      close(inputStream);
    }
  }

  @Test
  @Config(minSdk = N)
  public void setStream_unsupportedFlag_shouldNotCacheInMemory() throws Exception {
    InputStream inputStream = null;
    byte[] testImageBytes = getBytesFromBitmap(TEST_IMAGE_2);
    try {
      inputStream = new ByteArrayInputStream(testImageBytes);
      manager.setStream(
          inputStream, /* visibleCropHint= */ null, /* allowBackup= */ true, UNSUPPORTED_FLAG);

      assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)).isNull();
      assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)).isNull();
      assertThat(shadowOf(manager).getBitmap(UNSUPPORTED_FLAG)).isNull();
    } finally {
      close(inputStream);
    }
  }

  @Test
  @Config(minSdk = M)
  public void setWallpaperComponent_setWallpaperComponentPermissionNotGranted_shouldThrow() {
    shadowOf(application).denyPermissions(SET_WALLPAPER_COMPONENT);

    try {
      manager.setWallpaperComponent(TEST_WALLPAPER_SERVICE);
      fail();
    } catch (SecurityException e) {
      // Expected.
    }
  }

  @Test
  @Config(minSdk = M)
  public void setWallpaperComponent_wallpaperServiceNotExist_shouldThrow() {
    try {
      manager.setWallpaperComponent(new ComponentName("Foo", "Bar"));
      fail();
    } catch (IllegalArgumentException e) {
      // Expected.
    }
  }

  @Test
  @Config(minSdk = M)
  public void
      setWallpaperComponent_liveWallpaperSet_shouldReturnLiveWallpaperComponentAndUnsetStaticWallpapers()
          throws Exception {
    manager.setWallpaperComponent(TEST_WALLPAPER_SERVICE);

    assertThat(manager.getWallpaperInfo().getComponent()).isEqualTo(TEST_WALLPAPER_SERVICE);
    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)).isNull();
    assertThat(shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)).isNull();
  }

  @Test
  @Config(minSdk = M)
  public void getWallpaperInfo_noLiveWallpaperSet_shouldReturnNull() throws Exception {
    assertThat(manager.getWallpaperInfo()).isNull();
  }

  @Config(minSdk = P)
  public void
      getWallpaperInfo_staticWallpaperWasDefault_liveWallpaperSet_shouldRemoveCachedStaticWallpaper()
          throws Exception {
    manager.setBitmap(
        TEST_IMAGE_1,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_SYSTEM);
    manager.setBitmap(
        TEST_IMAGE_2,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_LOCK);

    manager.setWallpaperComponent(TEST_WALLPAPER_SERVICE);

    assertThat(manager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)).isNull();
    assertThat(manager.getWallpaperFile(WallpaperManager.FLAG_LOCK)).isNull();
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void getDefaultWallpaperDimAmount_shouldBeZero() {
    assertThat(
            ApplicationProvider.getApplicationContext()
                .getSystemService(WallpaperManager.class)
                .getWallpaperDimAmount())
        .isEqualTo(0.0f);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setWallpaperDimAmount_shouldGetSameDimAmount() {
    float testDimAmount = 0.1f;
    ApplicationProvider.getApplicationContext()
        .getSystemService(WallpaperManager.class)
        .setWallpaperDimAmount(testDimAmount);

    assertThat(
            ApplicationProvider.getApplicationContext()
                .getSystemService(WallpaperManager.class)
                .getWallpaperDimAmount())
        .isEqualTo(testDimAmount);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setWallpaperDimAmount_belowRange_shouldBeBounded() {
    float testDimAmount = -0.5f;
    ApplicationProvider.getApplicationContext()
        .getSystemService(WallpaperManager.class)
        .setWallpaperDimAmount(testDimAmount);

    assertThat(
            ApplicationProvider.getApplicationContext()
                .getSystemService(WallpaperManager.class)
                .getWallpaperDimAmount())
        .isEqualTo(0f);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setWallpaperDimAmount_aboveRange_shouldBeBounded() {
    float testDimAmount = 2.5f;
    ApplicationProvider.getApplicationContext()
        .getSystemService(WallpaperManager.class)
        .setWallpaperDimAmount(testDimAmount);

    assertThat(
            ApplicationProvider.getApplicationContext()
                .getSystemService(WallpaperManager.class)
                .getWallpaperDimAmount())
        .isEqualTo(1f);
  }

  private static byte[] getBytesFromFileDescriptor(FileDescriptor fileDescriptor)
      throws IOException {
    FileInputStream inputStream = null;
    ByteArrayOutputStream outputStream = null;
    try {
      inputStream = new FileInputStream(fileDescriptor);
      outputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int numOfBytes = 0;
      while ((numOfBytes = inputStream.read(buffer, 0, buffer.length)) != -1) {
        outputStream.write(buffer, 0, numOfBytes);
      }
      return outputStream.toByteArray();
    } finally {
      close(inputStream);
      close(outputStream);
    }
  }

  private static byte[] getBytesFromBitmap(Bitmap bitmap) throws IOException {
    ByteArrayOutputStream stream = null;
    try {
      stream = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.PNG, /* quality= */ 0, stream);
      return stream.toByteArray();
    } finally {
      close(stream);
    }
  }

  private static void close(@Nullable Closeable closeable) throws IOException {
    if (closeable != null) {
      closeable.close();
    }
  }
}
