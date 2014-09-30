package org.robolectric.shadows;

import android.os.ParcelFileDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowParcelFileDescriptorTest {
  @Test
  public void testParcelFileDescriptor() throws Exception {
    byte[] data = new byte[] {1, 2, 3, 4};
    File filePath = new File(Robolectric.application.getFilesDir(), "test");

    FileOutputStream os = new FileOutputStream(filePath);
    os.write(data);
    os.close();

    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(filePath, -1);
    assertThat(pfd).isNotNull();

    byte[] readData = new byte[4];
    FileInputStream is = new FileInputStream(pfd.getFileDescriptor());
    is.read(readData);
    is.close();
    pfd.close();

    assertThat(readData).isEqualTo(data);
  }
}
