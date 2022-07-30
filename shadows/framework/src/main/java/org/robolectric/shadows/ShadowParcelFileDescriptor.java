package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SuppressLint;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(ParcelFileDescriptor.class)
@SuppressLint("NewApi")
public class ShadowParcelFileDescriptor {
  // TODO: consider removing this shadow in favor of shadowing file operations at the libcore.os
  // level
  private static final String PIPE_TMP_DIR = "ShadowParcelFileDescriptor";
  private static final String PIPE_FILE_NAME = "pipe";
  private static final OpenedFileTable openedFileTable = new OpenedFileTable();
  private RandomAccessFile file;
  private int fd;
  private boolean closed;
  @RealObject ParcelFileDescriptor realParcelFd;

  private @RealObject ParcelFileDescriptor realObject;

  @Implementation
  protected void __constructor__(ParcelFileDescriptor wrapped) {
    invokeConstructor(
        ParcelFileDescriptor.class, realObject, from(ParcelFileDescriptor.class, wrapped));
    if (wrapped != null) {
      ShadowParcelFileDescriptor shadowParcelFileDescriptor = Shadow.extract(wrapped);
      this.file = shadowParcelFileDescriptor.file;
      this.fd = shadowParcelFileDescriptor.fd;
    }
  }

  @Implementation
  protected static ParcelFileDescriptor open(File file, int mode) throws FileNotFoundException {
    return openInternal(new RandomAccessFile(file, getFileMode(mode)), mode);
  }

  private static ParcelFileDescriptor openInternal(RandomAccessFile randomAccessFile, int mode)
      throws FileNotFoundException {
    ParcelFileDescriptor pfd;
    try {
      Constructor<ParcelFileDescriptor> constructor =
          ParcelFileDescriptor.class.getDeclaredConstructor(FileDescriptor.class);
      pfd = constructor.newInstance(new FileDescriptor());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    ShadowParcelFileDescriptor shadowParcelFileDescriptor = Shadow.extract(pfd);
    shadowParcelFileDescriptor.file = randomAccessFile;
    int fd = shadowParcelFileDescriptor.getFd();
    shadowParcelFileDescriptor.fd = fd;
    if ((mode & ParcelFileDescriptor.MODE_TRUNCATE) != 0) {
      try {
        shadowParcelFileDescriptor.file.setLength(0);
      } catch (IOException ioe) {
        FileNotFoundException fnfe = new FileNotFoundException("Unable to truncate");
        fnfe.initCause(ioe);
        throw fnfe;
      }
    }
    if ((mode & ParcelFileDescriptor.MODE_APPEND) != 0) {
      try {
        shadowParcelFileDescriptor.file.seek(shadowParcelFileDescriptor.file.length());
      } catch (IOException ioe) {
        FileNotFoundException fnfe = new FileNotFoundException("Unable to append");
        fnfe.initCause(ioe);
        throw fnfe;
      }
    }

    try {
      openedFileTable.open(shadowParcelFileDescriptor.file, mode, fd);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return pfd;
  }

  @Implementation
  protected static ParcelFileDescriptor adoptFd(int fd) {
    // For some limitation, the given fd must be opened from PFD.
    // The behavior is different from AOSP, it must be checked early. While AOSP throws IOException
    // in later attempt I/O on the stream of the file descriptor.
    OpenedFileItem item = openedFileTable.getItem(fd);
    if (item == null) {
      throw new IllegalStateException(
          "Only the given fd opened and tracked by PFD can be adopted as a new PFD.");
    }

    try {
      return openInternal(item.file, item.mode);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
    return "rw";
  }

  @Implementation
  protected static ParcelFileDescriptor[] createPipe() throws IOException {
    File file =
        new File(
            RuntimeEnvironment.getTempDirectory().createIfNotExists(PIPE_TMP_DIR).toFile(),
            PIPE_FILE_NAME + "-" + UUID.randomUUID());
    if (!file.createNewFile()) {
      throw new IOException("Cannot create pipe file: " + file.getAbsolutePath());
    }
    ParcelFileDescriptor readSide = open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    ParcelFileDescriptor writeSide = open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    file.deleteOnExit();
    return new ParcelFileDescriptor[] {readSide, writeSide};
  }

  @Implementation(minSdk = KITKAT)
  protected static ParcelFileDescriptor[] createReliablePipe() throws IOException {
    return createPipe();
  }

  @Implementation
  protected FileDescriptor getFileDescriptor() {
    try {
      return file.getFD();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  protected int detachFd() {
    if (closed) {
      throw new IllegalStateException("Already closed");
    }
    int fd = getFd();
    closed = true;
    return fd;
  }

  @Implementation
  protected long getStatSize() {
    try {
      return file.length();
    } catch (IOException e) {
      // This might occur when the file object has been closed.
      return -1;
    }
  }

  @Implementation
  protected int getFd() {
    if (closed) {
      throw new IllegalStateException("Already closed");
    }

    try {
      return ReflectionHelpers.getField(file.getFD(), "fd");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  protected void close() throws IOException {
    // Act this status check the same as real close operation in AOSP.
    if (closed) {
      return;
    }

    // MUST get the fd before the close status was updated and the file was closed.
    int fd = getFd();

    // The file descriptor can be closed in another way before it was closed by the attached PFD.
    // But it still need to be untracked from opened file table.
    if (fd != -1) {
      openedFileTable.close(fd);
    } else {
      openedFileTable.close(this.fd);
    }

    // MUST update close status as early as possible. Avoid multiple closes being called as a chain
    // when the file descriptor was attached to multiple parents.
    closed = true;
    file.close();
    reflector(ParcelFileDescriptorReflector.class, realParcelFd).close();
  }

  @ForType(ParcelFileDescriptor.class)
  interface ParcelFileDescriptorReflector {

    @Direct
    void close();
  }

  /**
   * Tracks all the opened files opened by {@link ShadowParcelFileDescriptor#open(File, int)}.
   *
   * <p>Only for internal used by PFD.
   *
   * <p>The concept of "OpenedFileTable" is quite similar to *nix.
   *
   * <p>Note that is not supposed to be run on Windows, for the key `fd` is only available in *nix.
   * In Windows the `handle` should be used instead of the `fd`.
   */
  private static class OpenedFileTable {
    private final Map<FileDescriptor, OpenedFileItem> openedFileItems = new WeakHashMap<>();

    void open(RandomAccessFile file, int mode, int fd) throws IOException {
      synchronized (openedFileItems) {
        openedFileItems.put(file.getFD(), new OpenedFileItem(file, mode, fd));
      }
    }

    OpenedFileItem getItem(int fd) {
      Map.Entry<FileDescriptor, OpenedFileItem> entry = this.getEntry(fd);
      if (entry == null) {
        return null;
      } else {
        return entry.getValue();
      }
    }

    private Map.Entry<FileDescriptor, OpenedFileItem> getEntry(int fd) {
      synchronized (openedFileItems) {
        int matchedEntriesCount = 0;
        Map.Entry<FileDescriptor, OpenedFileItem> targetEntry = null;
        for (Map.Entry<FileDescriptor, OpenedFileItem> entry : openedFileItems.entrySet()) {
          OpenedFileItem item = openedFileItems.get(entry.getKey());
          if (item == null) {
            continue;
          }

          if (entry.getValue().fd == fd && !entry.getValue().closed) {
            matchedEntriesCount++;
            targetEntry = entry;
          }
        }

        if (matchedEntriesCount > 1) {
          throw new RuntimeException("Exactly one opened file item should be matched.");
        } else {
          return targetEntry;
        }
      }
    }

    void close(int fd) throws IOException {
      synchronized (openedFileItems) {
        Map.Entry<FileDescriptor, OpenedFileItem> entry = this.getEntry(fd);
        if (entry == null) {
          throw new IOException(
              "Can not find the opened file item from the table by given fd: " + fd);
        }
        entry.getValue().closed = true;
      }
    }
  }

  private static class OpenedFileItem {
    final RandomAccessFile file;
    final int mode;
    final int fd;
    boolean closed;

    OpenedFileItem(RandomAccessFile file, int mode, int fd) {
      this.file = file;
      this.mode = mode;
      this.fd = fd;
      this.closed = false;
    }
  }
}
