package org.robolectric.shadows;

import android.os.ParcelFileDescriptor;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.TempDirectory;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.nio.file.Path;

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
    Shadows.shadowOf(pfd).file = new RandomAccessFile(file, mode == ParcelFileDescriptor.MODE_READ_ONLY ? "r" : "rw");
    return pfd;
  }

  @Implementation
  public static ParcelFileDescriptor[] createPipe() throws IOException {
    Path path = TempDirectory.create();
    File file = new File(path.toFile(), "pipe-" + System.nanoTime());
    if (file.exists() && !file.delete()) {
      throw new IOException("Cannot delete pipe file: " + file.getAbsolutePath());
    }
    if (!file.createNewFile()) {
      throw new IOException("Cannot create pipe file: " + file.getAbsolutePath());
    }
    ParcelFileDescriptor readSide = open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    ParcelFileDescriptor writeSide = open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    file.deleteOnExit();
    return new ParcelFileDescriptor[]{readSide, writeSide};
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
  public long getStatSize() {
    try {
      return file.length();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  public void close() throws IOException {
    file.close();
  }
}
