package android.system.ctesque;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.R;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.os.Build.VERSION;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.time.Duration;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OsTest {

  private File file;
  private String path;

  @Before
  public void setUp() throws Exception {
    assumeTrue(VERSION.SDK_INT >= O); // Robolectric only has realistic support for O and up
    File outputDir = getApplicationContext().getCacheDir();
    this.file = File.createTempFile("OsTest", ".txt", outputDir);
    this.path = file.getAbsolutePath();
    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      outputStream.write("some UTF-8\u202Fcontent in a file".getBytes(UTF_8));
    }
  }

  @After
  public void tearDown() {
    if (file != null && file.exists()) {
      file.delete();
    }
  }

  @Test
  public void rootIsExecutable() throws Exception {
    int fileMode = Os.stat("/").st_mode;
    assertThat(fileMode & OsConstants.S_IXUSR).isEqualTo(OsConstants.S_IXUSR);
    assertThat(fileMode & OsConstants.S_IXGRP).isEqualTo(OsConstants.S_IXGRP);
    assertThat(fileMode & OsConstants.S_IXOTH).isEqualTo(OsConstants.S_IXOTH);
  }

  @Test
  public void getStat_returnCorrectMode() throws Exception {
    StructStat stat = Os.stat(path);
    // S_IFREG means regular file
    assertThat(stat.st_mode & OsConstants.S_IFREG).isEqualTo(OsConstants.S_IFREG);
  }

  @Test
  public void getStat_returnCorrectSize() throws Exception {
    StructStat stat = Os.stat(path);
    assertThat(stat.st_size).isEqualTo(file.length());
  }

  @Test
  public void getStat_returnCorrectModifiedTime() throws Exception {
    StructStat stat = Os.stat(path);
    assertThat(stat.st_mtime).isEqualTo(Duration.ofMillis(file.lastModified()).getSeconds());
  }

  @Test
  public void getStat_executableFalse() throws Exception {
    int fileMode = Os.stat(path).st_mode;
    assertThat(fileMode & OsConstants.S_IXUSR).isEqualTo(0);

    assertTrue(file.setExecutable(true));
    fileMode = Os.stat(path).st_mode;
    assertThat(fileMode & OsConstants.S_IXUSR).isEqualTo(OsConstants.S_IXUSR);
  }

  @Test
  public void memfdCreate_returnNoneNullFileDescriptor() throws Exception {
    assumeTrue(VERSION.SDK_INT >= R);
    FileDescriptor arscFile = Os.memfd_create("remote_views_theme_colors.arsc", /* flags= */ 0);
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

      assertThat(Os.pread(fd, buffer, bytesOffset, bytesCount, offsetInFile)).isEqualTo(bytesCount);
      assertThat(new String(buffer, UTF_8)).isEqualTo("-----content-----");
    }
  }

  @Test
  public void pread_handleFNF() throws Exception {
    // API 21 throws IOException not ErrnoException
    assumeTrue(VERSION.SDK_INT > LOLLIPOP);
    try (FileInputStream fis = new FileInputStream(file)) {
      FileDescriptor fd = fis.getFD();
      assertThat(fd.valid()).isTrue();

      // Delete the file under test.
      fis.close();
      assertThat(file.delete()).isTrue();

      final byte[] buffer = new byte[10];
      Arrays.fill(buffer, (byte) '-');
      assertThrows(ErrnoException.class, () -> Os.pread(fd, buffer, 0, 5, 0));
    }
  }

  // verify pread can be called multiple continuous times
  // this is intended to verify behavior when reading from a large file
  @Test
  public void pread_chained() throws Exception {
    FileDescriptor fd = new RandomAccessFile(file, "r").getFD();

    final byte[] buffer = new byte[10];
    int read = 1;
    int offset = 0;
    while (read > 0) {
      read = Os.pread(fd, buffer, 0, 5, offset);
      offset += read;
    }
  }
}
