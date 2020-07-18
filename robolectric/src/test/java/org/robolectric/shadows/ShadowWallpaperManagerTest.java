package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowWallpaperManagerTest {

  private static final Bitmap TEST_IMAGE_1 = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

  private static final Bitmap TEST_IMAGE_2 = Bitmap.createBitmap(3, 2, Bitmap.Config.ARGB_8888);

  private static final Bitmap TEST_IMAGE_3 = Bitmap.createBitmap(1, 5, Bitmap.Config.ARGB_8888);

  @Test
  public void getInstance_shouldCreateInstance() {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());
    assertThat(manager).isNotNull();
  }

  @Test
  public void sendWallpaperCommand_shouldNotThrowException() {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());
    manager.sendWallpaperCommand(null, null, 0, 0, 0, null);
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_flagSystem_shouldCacheInMemory() throws Exception {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());
    int returnCode =
        manager.setBitmap(
            TEST_IMAGE_1,
            /* visibleCropHint= */ null,
            /* allowBackup= */ false,
            WallpaperManager.FLAG_SYSTEM);

    assertThat(returnCode).isEqualTo(1);
    assertThat(Shadows.shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM))
        .isEqualTo(TEST_IMAGE_1);
    assertThat(Shadows.shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_multipleCallsWithFlagSystem_shouldCacheLastBitmapInMemory()
      throws Exception {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());
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

    assertThat(Shadows.shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM))
        .isEqualTo(TEST_IMAGE_3);
    assertThat(Shadows.shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_flagLock_shouldCacheInMemory() throws Exception {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());
    int returnCode =
        manager.setBitmap(
            TEST_IMAGE_2,
            /* visibleCropHint= */ null,
            /* allowBackup= */ false,
            WallpaperManager.FLAG_LOCK);

    assertThat(returnCode).isEqualTo(1);
    assertThat(Shadows.shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK))
        .isEqualTo(TEST_IMAGE_2);
    assertThat(Shadows.shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_multipleCallsWithFlagLock_shouldCacheLastBitmapInMemory() throws Exception {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());
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

    assertThat(Shadows.shadowOf(manager).getBitmap(WallpaperManager.FLAG_LOCK))
        .isEqualTo(TEST_IMAGE_3);
    assertThat(Shadows.shadowOf(manager).getBitmap(WallpaperManager.FLAG_SYSTEM)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void setBitmap_unsupportedFlag_shouldNotCacheInMemory() throws Exception {
    int unsupportedFlag = WallpaperManager.FLAG_LOCK + 123;
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());
    int code =
        manager.setBitmap(
            TEST_IMAGE_1, /* visibleCropHint= */ null, /* allowBackup= */ false, unsupportedFlag);

    assertThat(code).isEqualTo(0);
    assertThat(Shadows.shadowOf(manager).getBitmap(unsupportedFlag)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_flagSystem_nothingCached_shouldReturnNull() throws Exception {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());

    assertThat(manager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_flagSystem_previouslyCached_shouldReturnParcelFileDescriptor()
      throws Exception {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());
    manager.setBitmap(
        TEST_IMAGE_1,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_SYSTEM);

    ParcelFileDescriptor parcelFileDescriptor =
        manager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
    assertThat(getBytesFromFileDescriptor(parcelFileDescriptor.getFileDescriptor()))
        .isEqualTo(getBytesFromBitmap(TEST_IMAGE_1));
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_flagLock_nothingCached_shouldReturnNull() throws Exception {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());

    assertThat(manager.getWallpaperFile(WallpaperManager.FLAG_LOCK)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_flagLock_previouslyCached_shouldReturnParcelFileDescriptor()
      throws Exception {
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());
    manager.setBitmap(
        TEST_IMAGE_3,
        /* visibleCropHint= */ null,
        /* allowBackup= */ false,
        WallpaperManager.FLAG_LOCK);

    ParcelFileDescriptor parcelFileDescriptor =
        manager.getWallpaperFile(WallpaperManager.FLAG_LOCK);
    assertThat(getBytesFromFileDescriptor(parcelFileDescriptor.getFileDescriptor()))
        .isEqualTo(getBytesFromBitmap(TEST_IMAGE_3));
  }

  @Test
  @Config(minSdk = P)
  public void getWallpaperFile_unsupportedFlag_shouldReturnNull() throws Exception {
    int unsupportedFlag = WallpaperManager.FLAG_LOCK + 123;
    WallpaperManager manager =
        WallpaperManager.getInstance(ApplicationProvider.getApplicationContext());

    assertThat(manager.getWallpaperFile(unsupportedFlag)).isNull();
  }

  private static byte[] getBytesFromFileDescriptor(FileDescriptor fileDescriptor)
      throws IOException {
    try (FileInputStream inputStream = new FileInputStream(fileDescriptor);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int numOfBytes = 0;
      while ((numOfBytes = inputStream.read(buffer, 0, buffer.length)) != -1) {
        outputStream.write(buffer, 0, numOfBytes);
      }
      return outputStream.toByteArray();
    }
  }

  private static byte[] getBytesFromBitmap(Bitmap bitmap) throws IOException {
    try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
      bitmap.compress(Bitmap.CompressFormat.PNG, /* quality= */ 0, stream);
      return stream.toByteArray();
    }
  }
}
