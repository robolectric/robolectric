package org.robolectric.shadows;

import android.os.ParcelFileDescriptor;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;

import static org.robolectric.shadow.api.Shadow.directlyOn;

/**
 * Shadow for {@link android.os.ParcelFileDescriptor}.
 */
@Implements(ParcelFileDescriptor.class)
public class ShadowParcelFileDescriptor {
  @RealObject ParcelFileDescriptor realObject;

  private RandomAccessFile file;

  {
    System.out.println();
  }

  @Implementation
  public static ParcelFileDescriptor open(File file, int mode) throws FileNotFoundException {
    ParcelFileDescriptor pfd;
    try {
      Constructor<ParcelFileDescriptor> constructor = ParcelFileDescriptor.class.getDeclaredConstructor(FileDescriptor.class);
      pfd = constructor.newInstance(new FileDescriptor());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    Shadows.shadowOf(pfd).file = new RandomAccessFile(file, mode == ParcelFileDescriptor.MODE_READ_ONLY ? "r" : "rw");
    return pfd;
  }

  @Implementation
  public FileDescriptor getFileDescriptor() {
    if (file == null) {
      return directlyOn(realObject, ParcelFileDescriptor.class, "getFileDescriptor");
    }

    try {
      return file.getFD();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  public long getStatSize() {
    if (file == null) {
      return directlyOn(realObject, ParcelFileDescriptor.class, "getStatSize");
    }

    try {
      return file.length();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  public void close() throws IOException {
    if (file == null) {
      directlyOn(realObject, ParcelFileDescriptor.class, "getFileDescriptor");
      return;
    }

    file.close();
  }
}
