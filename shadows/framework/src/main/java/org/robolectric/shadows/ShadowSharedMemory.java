package org.robolectric.shadows;

import android.os.Build;
import android.os.SharedMemory;
import android.system.ErrnoException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.WeakHashMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.TempDirectory;

/**
 * This is not a faithful "shared" memory implementation. Since Robolectric tests only operate
 * within a single process, this shadow just allocates an in-process memory chunk.
 */
@Implements(value = SharedMemory.class,
    minSdk = Build.VERSION_CODES.O_MR1,
    /* not quite true, but this prevents a useless `shadowOf()` accessor showing
     * up, which would break people compiling against API 26 and earlier.
    */
    isInAndroidSdk = false
)
public class ShadowSharedMemory {
  private static final Map<FileDescriptor, File> filesByFd = new WeakHashMap<>();

  /**
   * For tests, returns a {@link ByteBuffer} of the requested size.
   */
  @Implementation
  public ByteBuffer map(int prot, int offset, int length) throws ErrnoException {
    return ByteBuffer.allocate(length);
  }

  @Implementation
  public static FileDescriptor nCreate(String name, int size) throws ErrnoException {
    TempDirectory tempDirectory = RuntimeEnvironment.getTempDirectory();

    try {
      File sharedMemoryFile =
          tempDirectory.createIfNotExists("SharedMemory").resolve("shmem-" + name).toFile();
      RandomAccessFile randomAccessFile = new RandomAccessFile(sharedMemoryFile, "rw");
      randomAccessFile.setLength(0);
      randomAccessFile.setLength(size);
      synchronized (filesByFd) {
        filesByFd.put(randomAccessFile.getFD(), sharedMemoryFile);
      }
      return randomAccessFile.getFD();
    } catch (IOException e) {
      throw new RuntimeException("Unable to create file descriptior", e);
    }
  }

  @Implementation
  public static int nGetSize(FileDescriptor fd) {
    synchronized (filesByFd) {
      return (int) filesByFd.get(fd).length();
    }
  }
}