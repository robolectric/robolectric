package org.robolectric.shadows;

import android.os.ParcelFileDescriptor;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Implements(ParcelFileDescriptor.class)
public class ShadowParcelFileDescriptor {
  @RealObject
  ParcelFileDescriptor realObject;

  private static final BiMap<Integer, FileDescriptor> fds = HashBiMap.create();
  private static final AtomicInteger nextFd = new AtomicInteger(10);

  @Implementation
  public static ParcelFileDescriptor adoptFd(int fd) {
    return new ParcelFileDescriptor(fds.get(fd));
  }

  @Implementation
  public void closeWithStatus(int status, String msg) {
    FileDescriptor fd = realObject.getFileDescriptor();
    fds.inverse().remove(fd);
  }

  @Implementation
  public long getStatSize() {
    try (FileInputStream in = new FileInputStream(realObject.getFileDescriptor())) {
      return in.getChannel().size();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static int fd(FileDescriptor fd) {
    int fdNum = nextFd.getAndIncrement();
    fds.put(fdNum, fd);
    return fdNum;
  }
}
