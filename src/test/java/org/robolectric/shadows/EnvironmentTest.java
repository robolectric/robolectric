package org.robolectric.shadows;

import android.os.Environment;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class EnvironmentTest {

  @After
  public void tearDown() throws Exception {
    deleteDir(ShadowContext.EXTERNAL_CACHE_DIR);
    deleteDir(ShadowContext.EXTERNAL_FILES_DIR);
    ShadowEnvironment.setExternalStorageState("removed");
  }

  @Test
  public void testExternalStorageState() {
    assertThat(Environment.getExternalStorageState()).isEqualTo("removed");
    ShadowEnvironment.setExternalStorageState("mounted");
    assertThat(Environment.getExternalStorageState()).isEqualTo("mounted");
  }

  @Test
  public void testGetExternalStorageDirectory() {
    assertTrue(Environment.getExternalStorageDirectory().exists());
  }

  @Test
  public void testGetExternalStoragePublicDirectory() {
    File extStoragePublic = Environment.getExternalStoragePublicDirectory("Movies");
    assertTrue(extStoragePublic.exists());
    assertThat(extStoragePublic).isEqualTo(new File(ShadowContext.EXTERNAL_FILES_DIR, "Movies"));
  }

  public void deleteDir(File path) {
    if (path.isDirectory()) {
      File[] files = path.listFiles();
      for (File f : files) {
        deleteDir(f);
      }
    }
    path.delete();
  }

}
