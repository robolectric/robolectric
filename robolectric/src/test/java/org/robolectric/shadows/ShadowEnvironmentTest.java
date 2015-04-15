package org.robolectric.shadows;

import android.os.Environment;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowEnvironmentTest {

  @After
  public void tearDown() throws Exception {
    deleteDir(ShadowContext.EXTERNAL_CACHE_DIR);
    deleteDir(ShadowContext.EXTERNAL_FILES_DIR);
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
    assertThat(path).isEqualTo(new File(ShadowContext.EXTERNAL_FILES_DIR, Environment.DIRECTORY_MOVIES));
  }

  @Test
  public void isExternalStorageRemovable_shouldReturnSavedValue() {
    final File file = new File("/mnt/media/file");
    assertThat(Environment.isExternalStorageRemovable(file)).isFalse();
    ShadowEnvironment.setExternalStorageRemovable(file, true);
    assertThat(Environment.isExternalStorageRemovable(file)).isTrue();
  }

  @Test
  public void isExternalStorageEmulated_shouldReturnSavedValue() {
    final File file = new File("/mnt/media/file");
    assertThat(Environment.isExternalStorageEmulated(file)).isFalse();
    ShadowEnvironment.setExternalStorageEmulated(file, true);
    assertThat(Environment.isExternalStorageEmulated(file)).isTrue();
  }

  @Test
  public void reset_shouldClearRemovableFiles() {
    final File file = new File("foo");
    ShadowEnvironment.setExternalStorageRemovable(file, true);

    assertThat(Environment.isExternalStorageRemovable(file)).isTrue();
    ShadowEnvironment.reset();
    assertThat(Environment.isExternalStorageRemovable(file)).isFalse();
  }

  @Test
  public void reset_shouldClearEmulatedFiles() {
    final File file = new File("foo");
    ShadowEnvironment.setExternalStorageEmulated(file, true);

    assertThat(Environment.isExternalStorageEmulated(file)).isTrue();
    ShadowEnvironment.reset();
    assertThat(Environment.isExternalStorageEmulated(file)).isFalse();
  }

  private static void deleteDir(File path) {
    if (path.isDirectory()) {
      for (File f : path.listFiles()) {
        deleteDir(f);
      }
    }
    path.delete();
  }
}
