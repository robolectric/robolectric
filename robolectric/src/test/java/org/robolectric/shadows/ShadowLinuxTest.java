package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.system.StructStat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@code ShadowLinux}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public final class ShadowLinuxTest {
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  private File file;
  private String path;
  private ShadowLinux shadowLinux;

  @Before
  public void setUp() throws Exception {
    shadowLinux = new ShadowLinux();
    file = tempFolder.newFile("ShadowLinuxTest");
    path = file.getAbsolutePath();
    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      outputStream.write("some UTF-8\u202Fcontent in a file".getBytes(UTF_8));
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

  @Test
  @Config(minSdk = R)
  public void memfdCreate_returnNoneNullFileDescriptor() throws Exception {
    FileDescriptor arscFile =
        shadowLinux.memfd_create("remote_views_theme_colors.arsc", /* flags= */ 0);
    assertThat(arscFile).isNotNull();
  }

  @Test
  public void pread_validateExtractsContentWithOffset() throws Exception {
    try (FileInputStream fis = new FileInputStream(file)) {
      FileDescriptor fd = fis.getFD();
      assertThat(fd.valid()).isTrue();

      final int bytesCount = "content".length();
      final int bytesOffset = 5;
      final byte[] buffer = new byte[bytesCount + 2 * bytesOffset];
      Arrays.fill(buffer, (byte) '-');

      final int offsetInFile = "some UTF-8\u202F".getBytes(UTF_8).length;

      assertThat(shadowLinux.pread(fd, buffer, bytesOffset, bytesCount, offsetInFile))
          .isEqualTo(bytesCount);
      assertThat(new String(buffer, UTF_8)).isEqualTo("-----content-----");
    }
  }

  @Test
  public void pread_handleFNF() throws Exception {
    try (FileInputStream fis = new FileInputStream(file)) {
      FileDescriptor fd = fis.getFD();
      assertThat(fd.valid()).isTrue();

      // Delete the file under test.
      fis.close();
      assertThat(file.delete()).isTrue();

      final byte[] buffer = new byte[10];
      Arrays.fill(buffer, (byte) '-');
      assertThat(shadowLinux.pread(fd, buffer, 0, 5, 0)).isEqualTo(-1);
    }
  }

  @Test
  public void pread_readPastEnd() throws Exception {
    try (FileInputStream fis = new FileInputStream(file)) {
      FileDescriptor fd = fis.getFD();
      assertThat(fd.valid()).isTrue();

      final byte[] buffer = new byte[10];
      Arrays.fill(buffer, (byte) '-');
      assertThat(shadowLinux.pread(fd, buffer, 0, 5, 500)).isEqualTo(-1);
    }
  }
}
