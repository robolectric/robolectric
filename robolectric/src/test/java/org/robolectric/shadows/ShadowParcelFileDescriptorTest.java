package org.robolectric.shadows;

import android.os.ParcelFileDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

import java.io.File;
import java.io.FileOutputStream;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.os.ParcelFileDescriptor.MODE_READ_WRITE;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiSelfTest.class)
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
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    pfd.close();
  }

  @Test
  public void testOpens_readOnlyFile() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(readOnlyFile, MODE_READ_ONLY);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    pfd.close();
  }

  @Test
  public void testOpens_canWriteWritableFile() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(5);
    os.close();
  }

  @Test
  public void testStatSize_emptyFile() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    assertThat(pfd.getStatSize()).isEqualTo(0);
    pfd.close();
  }

  @Test
  public void testStatSize_writtenFile() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, MODE_READ_WRITE);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
    os.write(5);
    assertThat(pfd.getStatSize()).isEqualTo(1);  // One byte.
    os.close();
  }

  @Test
  public void testCloses() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
    pfd.close();

    // this assertion doesn't really make sense, but because FileDescriptors can only be closed
    // as a side-effect of closing the InputStreams that rely on them, the close method is a no-op
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
  }

  @Test
  public void testAutoCloseInputStream() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
    ParcelFileDescriptor.AutoCloseInputStream is = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
    is.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }

  @Test
  public void testAutoCloseOutputStream() throws Exception {
    File f = new File(RuntimeEnvironment.application.getFilesDir(), "outfile");
    f.createNewFile();
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, MODE_READ_ONLY);
    ParcelFileDescriptor.AutoCloseOutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
    os.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }
}
