package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;

import android.system.StructStat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Unit tests for ShadowPosix to check values returned from stat() call. */
@RunWith(AndroidJUnit4.class)
public final class ShadowPosixTest {
  private File file;
  private String path;

  @Before
  public void setUp() throws Exception {
    file = File.createTempFile("ShadowPosixTest", null);
    path = file.getAbsolutePath();
    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      outputStream.write(1234);
    }
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getStatAtLeastLollipop_returnCorrectMode() throws Exception {
    StructStat stat = (StructStat) ShadowPosix.stat(path);
    assertThat(stat.st_mode).isEqualTo(OsConstantsValues.S_IFREG_VALUE);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getStatAtLeastLollipop_returnCorrectSize() throws Exception {
    StructStat stat = (StructStat) ShadowPosix.stat(path);
    assertThat(stat.st_size).isEqualTo(file.length());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getStatAtLeastLollipop_returnCorrectModifiedTime() throws Exception {
    StructStat stat = (StructStat) ShadowPosix.stat(path);
    assertThat(stat.st_mtime).isEqualTo(Duration.ofMillis(file.lastModified()).getSeconds());
  }

  @Test
  @Config(maxSdk = KITKAT)
  public void getStatBelowLollipop_returnCorrectMode() throws Exception {
    Object stat = ShadowPosix.stat(path);
    int mode = ReflectionHelpers.getField(stat, "st_mode");
    assertThat(mode).isEqualTo(OsConstantsValues.S_IFREG_VALUE);
  }

  @Test
  @Config(minSdk = KITKAT)
  public void getStatBelowLollipop_returnCorrectSize() throws Exception {
    Object stat = ShadowPosix.stat(path);
    long size = ReflectionHelpers.getField(stat, "st_size");
    assertThat(size).isEqualTo(file.length());
  }

  @Test
  @Config(minSdk = KITKAT)
  public void getStatBelowtLollipop_returnCorrectModifiedTime() throws Exception {
    Object stat = ShadowPosix.stat(path);
    long modifiedTime = ReflectionHelpers.getField(stat, "st_mtime");
    assertThat(modifiedTime).isEqualTo(Duration.ofMillis(file.lastModified()).getSeconds());
  }
}
