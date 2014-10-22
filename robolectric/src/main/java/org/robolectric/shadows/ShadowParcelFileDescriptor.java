package org.robolectric.shadows;

import android.os.ParcelFileDescriptor;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

@Implements(ParcelFileDescriptor.class)
public class ShadowParcelFileDescriptor {
  private RandomAccessFile file;

  @Implementation
  public static ParcelFileDescriptor open(File file, int mode) throws FileNotFoundException {
    ParcelFileDescriptor pfd;
    try {
      Constructor<ParcelFileDescriptor> constructor = ParcelFileDescriptor.class.getDeclaredConstructor(FileDescriptor.class);
      pfd = constructor.newInstance(new FileDescriptor());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    ((ShadowParcelFileDescriptor) Robolectric.shadowOf_(pfd)).file = new RandomAccessFile(file, "rw");
    return pfd;
  }

  @Implementation
  public FileDescriptor getFileDescriptor() {
    try {
      return file.getFD();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  public void close() throws IOException {
    file.close();
  }
}