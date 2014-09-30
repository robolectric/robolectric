package org.robolectric.shadows;

import android.os.ParcelFileDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.io.File;
import java.io.FileOutputStream;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowParcelFileDescriptorTest {
  private File file;

  @Before
  public void setup() throws Exception {
    file = new File(Robolectric.application.getFilesDir(), "test");
    FileOutputStream os = new FileOutputStream(file);
    os.close();
  }

  @Test
  public void testOpens() throws Exception {
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, -1);
    assertThat(pfd).isNotNull();
    assertThat(pfd.getFileDescriptor().valid()).isTrue();
    pfd.close();
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
    File f = new File(Robolectric.application.getFilesDir(), "outfile");
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, -1);
    ParcelFileDescriptor.AutoCloseOutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
    os.close();
    assertThat(pfd.getFileDescriptor().valid()).isFalse();
  }
}
