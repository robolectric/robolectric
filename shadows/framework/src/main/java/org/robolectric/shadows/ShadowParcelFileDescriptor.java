package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.extract;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.shadow.api.Shadow.newInstance;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(ParcelFileDescriptor.class)
@SuppressLint("NewApi")
public class ShadowParcelFileDescriptor {
  // TODO: consider removing this shadow in favor of shadowing file operations at the libcore.os
  // level
  private static final String PIPE_TMP_DIR = "ShadowParcelFileDescriptor";
  private static final String PIPE_FILE_NAME = "pipe";
  @Nullable private RandomAccessFile file;
  private FileDescriptor fd;
  @RealObject ParcelFileDescriptor realParcelFd;

  private @RealObject ParcelFileDescriptor realObject;

  @Implementation
  protected void __constructor__(ParcelFileDescriptor wrapped) {
    invokeConstructor(
        ParcelFileDescriptor.class, realObject, from(ParcelFileDescriptor.class, wrapped));
    if (wrapped != null) {
      ShadowParcelFileDescriptor shadowParcelFileDescriptor = extract(wrapped);
      this.file = shadowParcelFileDescriptor.file;
      try {
        this.fd = file.getFD();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Implementation
  protected static ParcelFileDescriptor open(File file, int mode) throws FileNotFoundException {
    ParcelFileDescriptor pfd;
    try {
      Constructor<ParcelFileDescriptor> constructor =
          ParcelFileDescriptor.class.getDeclaredConstructor(FileDescriptor.class);
      pfd = constructor.newInstance(new FileDescriptor());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    ShadowParcelFileDescriptor shadowParcelFileDescriptor = extract(pfd);
    shadowParcelFileDescriptor.file = new RandomAccessFile(file, getFileMode(mode));
    try {
      shadowParcelFileDescriptor.fd = shadowParcelFileDescriptor.file.getFD();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return pfd;
  }

  @Implementation
  protected static ParcelFileDescriptor dup(FileDescriptor orig) throws IOException {
    ParcelFileDescriptor pfd =
        newInstance(
            ParcelFileDescriptor.class, new Class<?>[] {FileDescriptor.class}, new Object[] {orig});

    ShadowParcelFileDescriptor shadowParcelFileDescriptor = extract(pfd);
    shadowParcelFileDescriptor.fd = orig;

    return pfd;
  }

  private static String getFileMode(int mode) {
    if ((mode & ParcelFileDescriptor.MODE_CREATE) != 0) {
      return "rw";
    }
    switch (mode & ParcelFileDescriptor.MODE_READ_WRITE) {
      case ParcelFileDescriptor.MODE_READ_ONLY:
        return "r";
      case ParcelFileDescriptor.MODE_WRITE_ONLY:
      case ParcelFileDescriptor.MODE_READ_WRITE:
        return "rw";
    }

    // TODO: this probably should be an error that we reach here, but default to 'rw' for now
    return "rw";
  }

  @Implementation
  protected static ParcelFileDescriptor[] createPipe() throws IOException {
    File file =
        new File(
            RuntimeEnvironment.getTempDirectory().create(PIPE_TMP_DIR).toFile(), PIPE_FILE_NAME);
    if (!file.createNewFile()) {
      throw new IOException("Cannot create pipe file: " + file.getAbsolutePath());
    }
    ParcelFileDescriptor readSide = open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    ParcelFileDescriptor writeSide = open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    file.deleteOnExit();
    return new ParcelFileDescriptor[] {readSide, writeSide};
  }

  @Implementation
  protected FileDescriptor getFileDescriptor() {
    return fd;
  }

  @Implementation
  protected long getStatSize() {
    if (file == null) {
      return -1;
    }

    try {
      return file.length();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  protected int getFd() {
    return ReflectionHelpers.getField(fd, "fd");
  }

  @Implementation
  protected void close() throws IOException {
    if (file == null) {
      return;
    }

    file.close();
  }
}
