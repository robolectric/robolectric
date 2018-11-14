package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.os.Environment;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
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
  @Config(minSdk = M)
  public void isExternalStorageRemovable_primaryShouldReturnSavedValue() {
    assertThat(Environment.isExternalStorageRemovable()).isFalse();
    ShadowEnvironment.setExternalStorageRemovable(Environment.getExternalStorageDirectory(), true);
    assertThat(Environment.isExternalStorageRemovable()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isExternalStorageRemovable_shouldReturnSavedValue() {
    final File file = new File("/mnt/media/file");
    assertThat(Environment.isExternalStorageRemovable(file)).isFalse();
    ShadowEnvironment.setExternalStorageRemovable(file, true);
    assertThat(Environment.isExternalStorageRemovable(file)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isExternalStorageEmulated_shouldReturnSavedValue() {
    final File file = new File("/mnt/media/file");
    assertThat(Environment.isExternalStorageEmulated(file)).isFalse();
    ShadowEnvironment.setExternalStorageEmulated(file, true);
    assertThat(Environment.isExternalStorageEmulated(file)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void storageIsLazy() {
    assertNull(ShadowEnvironment.EXTERNAL_CACHE_DIR);
    assertNull(ShadowEnvironment.EXTERNAL_FILES_DIR);

    Environment.getExternalStorageDirectory();
    Environment.getExternalStoragePublicDirectory(null);

    assertNotNull(ShadowEnvironment.EXTERNAL_CACHE_DIR);
    assertNotNull(ShadowEnvironment.EXTERNAL_FILES_DIR);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void reset_shouldClearRemovableFiles() {
    final File file = new File("foo");
    ShadowEnvironment.setExternalStorageRemovable(file, true);

    assertThat(Environment.isExternalStorageRemovable(file)).isTrue();
    ShadowEnvironment.reset();
    assertThat(Environment.isExternalStorageRemovable(file)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void reset_shouldClearEmulatedFiles() {
    final File file = new File("foo");
    ShadowEnvironment.setExternalStorageEmulated(file, true);

    assertThat(Environment.isExternalStorageEmulated(file)).isTrue();
    ShadowEnvironment.reset();
    assertThat(Environment.isExternalStorageEmulated(file)).isFalse();
  }

  @Test
  public void isExternalStorageEmulatedNoArg_shouldReturnSavedValue() {
    ShadowEnvironment.setIsExternalStorageEmulated(true);
    assertThat(Environment.isExternalStorageEmulated()).isTrue();
    ShadowEnvironment.reset();
    assertThat(Environment.isExternalStorageEmulated()).isFalse();
  }

  // TODO: failing test
  @Ignore
  @Test
  @Config(minSdk = KITKAT)
  public void getExternalFilesDirs() throws Exception {
    ShadowEnvironment.addExternalDir("external_dir_1");
    ShadowEnvironment.addExternalDir("external_dir_2");

    File[] externalFilesDirs =
        ApplicationProvider.getApplicationContext()
            .getExternalFilesDirs(Environment.DIRECTORY_MOVIES);

    assertThat(externalFilesDirs).isNotEmpty();
    assertThat(externalFilesDirs[0].getCanonicalPath()).contains("external_dir_1");
    assertThat(externalFilesDirs[1].getCanonicalPath()).contains("external_dir_2");

    // TODO(jongerrish): This fails because ShadowContext overwrites getExternalFilesDir.
//     assertThat(RuntimeEnvironment.application.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
//         .getCanonicalPath()).contains("external_dir_1");
  }

  @Test
  @Config(sdk = JELLY_BEAN_MR1)
  public void getExternalStorageStateJB() throws Exception {
    ShadowEnvironment.setExternalStorageState("blah");
    assertThat(ShadowEnvironment.getExternalStorageState()).isEqualTo("blah");
  }

  @Test
  @Config(minSdk = KITKAT, maxSdk = LOLLIPOP)
  public void getExternalStorageStatePreLollipopMR1() throws Exception {
    File storageDir1 = ShadowEnvironment.addExternalDir("dir1");
    File storageDir2 = ShadowEnvironment.addExternalDir("dir2");
    ShadowEnvironment.setExternalStorageState(storageDir1, Environment.MEDIA_MOUNTED);
    ShadowEnvironment.setExternalStorageState(storageDir2, Environment.MEDIA_REMOVED);
    ShadowEnvironment.setExternalStorageState("blah");

    assertThat(ShadowEnvironment.getStorageState(storageDir1))
        .isEqualTo(Environment.MEDIA_MOUNTED);
    assertThat(ShadowEnvironment.getStorageState(storageDir2))
        .isEqualTo(Environment.MEDIA_REMOVED);
    assertThat(ShadowEnvironment.getStorageState(new File(storageDir1, "subpath")))
        .isEqualTo(Environment.MEDIA_MOUNTED);
    assertThat(ShadowEnvironment.getExternalStorageState()).isEqualTo("blah");
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void getExternalStorageState() throws Exception {
    File storageDir1 = ShadowEnvironment.addExternalDir("dir1");
    File storageDir2 = ShadowEnvironment.addExternalDir("dir2");
    ShadowEnvironment.setExternalStorageState(storageDir1, Environment.MEDIA_MOUNTED);
    ShadowEnvironment.setExternalStorageState(storageDir2, Environment.MEDIA_REMOVED);
    ShadowEnvironment.setExternalStorageState("blah");

    assertThat(ShadowEnvironment.getExternalStorageState(storageDir1))
        .isEqualTo(Environment.MEDIA_MOUNTED);
    assertThat(ShadowEnvironment.getStorageState(storageDir1))
        .isEqualTo(Environment.MEDIA_MOUNTED);
    assertThat(ShadowEnvironment.getExternalStorageState(storageDir2))
        .isEqualTo(Environment.MEDIA_REMOVED);
    assertThat(ShadowEnvironment.getStorageState(storageDir2))
        .isEqualTo(Environment.MEDIA_REMOVED);
    assertThat(ShadowEnvironment.getExternalStorageState(new File(storageDir1, "subpath")))
        .isEqualTo(Environment.MEDIA_MOUNTED);
    assertThat(ShadowEnvironment.getStorageState(new File(storageDir1, "subpath")))
        .isEqualTo(Environment.MEDIA_MOUNTED);
    assertThat(ShadowEnvironment.getExternalStorageState()).isEqualTo("blah");
  }

  @Test
  @Config(minSdk = M)
  public void isExternalStorageEmulated() {
    ShadowEnvironment.setIsExternalStorageEmulated(true);
    assertThat(Environment.isExternalStorageEmulated()).isTrue();

    ShadowEnvironment.setIsExternalStorageEmulated(false);
    assertThat(Environment.isExternalStorageEmulated()).isFalse();
  }
}
