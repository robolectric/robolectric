package org.robolectric.shadows;

import android.os.Build;
import android.os.Environment;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowEnvironmentTest {

  @After
  public void tearDown() throws Exception {
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_REMOVED);
  }

  @Test
  public void getExternalStorageState_shouldReturnStorageState() {
    assertThat(Environment.getExternalStorageState()).isEqualTo(Environment.MEDIA_REMOVED);
    ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
    assertThat(Environment.getExternalStorageState()).isEqualTo(Environment.MEDIA_MOUNTED);
  }

  @Test
  public void getExternalStorageDirectory_shouldReturnDirectory() {
    assertThat(Environment.getExternalStorageDirectory().exists()).isTrue();
  }

  @Test
  public void getExternalStoragePublicDirectory_shouldReturnDirectory() {
    final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    assertThat(path.exists()).isTrue();
    assertThat(path).isEqualTo(new File(ShadowEnvironment.EXTERNAL_FILES_DIR.toFile(), Environment.DIRECTORY_MOVIES));
  }

  @Test
  public void getExternalStoragePublicDirectory_shouldReturnSameDirectory() {
    File path1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    File path2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

    assertThat(path1).isEqualTo(path2);
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.LOLLIPOP })
  public void isExternalStorageRemovable_shouldReturnSavedValue() {
    final File file = new File("/mnt/media/file");
    assertThat(Environment.isExternalStorageRemovable(file)).isFalse();
    ShadowEnvironment.setExternalStorageRemovable(file, true);
    assertThat(Environment.isExternalStorageRemovable(file)).isTrue();
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.LOLLIPOP })
  public void isExternalStorageEmulated_shouldReturnSavedValue() {
    final File file = new File("/mnt/media/file");
    assertThat(Environment.isExternalStorageEmulated(file)).isFalse();
    ShadowEnvironment.setExternalStorageEmulated(file, true);
    assertThat(Environment.isExternalStorageEmulated(file)).isTrue();
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.LOLLIPOP })
  public void storageIsLazy() {
    assertNull(ShadowEnvironment.EXTERNAL_CACHE_DIR);
    assertNull(ShadowEnvironment.EXTERNAL_FILES_DIR);

    Environment.getExternalStorageDirectory();
    Environment.getExternalStoragePublicDirectory(null);

    assertNotNull(ShadowEnvironment.EXTERNAL_CACHE_DIR);
    assertNotNull(ShadowEnvironment.EXTERNAL_FILES_DIR);
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.LOLLIPOP })
  public void reset_shouldClearRemovableFiles() {
    final File file = new File("foo");
    ShadowEnvironment.setExternalStorageRemovable(file, true);

    assertThat(Environment.isExternalStorageRemovable(file)).isTrue();
    ShadowEnvironment.reset();
    assertThat(Environment.isExternalStorageRemovable(file)).isFalse();
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.LOLLIPOP })
  public void reset_shouldClearEmulatedFiles() {
    final File file = new File("foo");
    ShadowEnvironment.setExternalStorageEmulated(file, true);

    assertThat(Environment.isExternalStorageEmulated(file)).isTrue();
    ShadowEnvironment.reset();
    assertThat(Environment.isExternalStorageEmulated(file)).isFalse();
  }

  @Test
  public void reset_shouldCleanupTempDirectories() {
    Environment.getExternalStorageDirectory();
    Environment.getExternalStoragePublicDirectory(null);
    File c = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    ShadowEnvironment.reset();

    // only c should actually be deleted
    assertNull(ShadowEnvironment.EXTERNAL_CACHE_DIR);
    assertNull(ShadowEnvironment.EXTERNAL_FILES_DIR);
    assertThat(c).doesNotExist();
  }
}
