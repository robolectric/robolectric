package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowParcelFileDescriptorTest {
  private File file;
  private File readOnlyFile;

  @Before
  public void setup() throws Exception {
    file = new File(RuntimeEnvironment.application.getFilesDir(), "test");
    FileOutputStream os = new FileOutputStream(file);
    os.close();
    readOnlyFile = new File(RuntimeEnvironment.application.getFilesDir(), "test_readonly");
    os = new FileOutputStream(readOnlyFile);
    os.close();
    readOnlyFile.setReadOnly();
  }

  @Test
  public void testOpens() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    pfd.close();
  }

  @Test
  public void testOpens_readOnlyFile() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(readOnlyFile, ParcelFileDescriptor.MODE_READ_ONLY);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    pfd.close();
  }

  @Test
  public void testOpens_canWriteWritableFile() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(5);
    os.close();
  }

  @Test
  public void testStatSize_emptyFile() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    assertThat(pfd.getStatSize()).isEqualTo(0);
    pfd.close();
  }

  @Test
  public void testStatSize_writtenFile() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(5);
    assertThat(pfd.getStatSize()).isEqualTo(1);  // One byte.
    os.close();
  }

  @Test
  public void testCloses() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, -1);
    pfd.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }

  @Test
  public void testAutoCloseInputStream() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, -1);
    ParcelFileDescriptor.AutoCloseInputStream is = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
    is.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }

  @Test
  public void testAutoCloseOutputStream() throws Exception {
    File f = new File(RuntimeEnvironment.application.getFilesDir(), "outfile");
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, -1);
    ParcelFileDescriptor.AutoCloseOutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
    os.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }
}
