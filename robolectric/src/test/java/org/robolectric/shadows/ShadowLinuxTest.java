package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
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

/** Unit tests for ShadowLinux to check values returned from stat() call. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public final class ShadowLinuxTest {
  private File file;
  private String path;
  private ShadowLinux shadowLinux;

  @Before
  public void setUp() throws Exception {
    shadowLinux = new ShadowLinux();
    file = File.createTempFile("ShadowLinuxTest", null);
    path = file.getAbsolutePath();
    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      outputStream.write(1234);
    }
  }

  @Test
  public void getStat_returnCorrectMode() throws Exception {
    StructStat stat = shadowLinux.stat(path);
    assertThat(stat.st_mode).isEqualTo(OsConstantsValues.S_IFREG_VALUE);
  }

  @Test
  public void getStat_returnCorrectSize() throws Exception {
    StructStat stat = shadowLinux.stat(path);
    assertThat(stat.st_size).isEqualTo(file.length());
  }

  @Test
  public void getStat_returnCorrectModifiedTime() throws Exception {
    StructStat stat = shadowLinux.stat(path);
    assertThat(stat.st_mtime).isEqualTo(Duration.ofMillis(file.lastModified()).getSeconds());
  }
}
