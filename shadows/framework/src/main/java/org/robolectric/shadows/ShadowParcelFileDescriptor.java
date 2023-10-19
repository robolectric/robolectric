package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(ParcelFileDescriptor.class)
@SuppressLint("NewApi")
public class ShadowParcelFileDescriptor {
  // TODO: consider removing this shadow in favor of shadowing file operations at the libcore.os
  // level
  private static final String PIPE_TMP_DIR = "ShadowParcelFileDescriptor";
  private static final String PIPE_FILE_NAME = "pipe";
  private static final Map<Integer, RandomAccessFile> filesInTransitById =
      Collections.synchronizedMap(new HashMap<>());
  private static final AtomicInteger NEXT_FILE_ID = new AtomicInteger();

  private RandomAccessFile file;
  private int fileIdPledgedOnClose; // != 0 if 'file' was written to a Parcel.
  private int lazyFileId; // != 0 if we were created from a Parcel but don't own a 'file' yet.
  private boolean closed;
  private Handler handler;
  private ParcelFileDescriptor.OnCloseListener onCloseListener;

  @RealObject private ParcelFileDescriptor realParcelFd;
  @RealObject private ParcelFileDescriptor realObject;

  @Implementation
  protected static void __staticInitializer__() {
    Shadow.directInitialize(ParcelFileDescriptor.class);
    ReflectionHelpers.setStaticField(
        ParcelFileDescriptor.class, "CREATOR", ShadowParcelFileDescriptor.CREATOR);
  }

  @Resetter
  public static void reset() {
    filesInTransitById.clear();
  }

  @Implementation
  protected void __constructor__(ParcelFileDescriptor wrapped) {
    invokeConstructor(
        ParcelFileDescriptor.class, realObject, from(ParcelFileDescriptor.class, wrapped));
    if (wrapped != null) {
      ShadowParcelFileDescriptor shadowParcelFileDescriptor = Shadow.extract(wrapped);
      this.file = shadowParcelFileDescriptor.file;
    }
  }

  static final Parcelable.Creator<ParcelFileDescriptor> CREATOR =
      new Parcelable.Creator<ParcelFileDescriptor>() {
        @Override
        public ParcelFileDescriptor createFromParcel(Parcel source) {
          int fileId = source.readInt();
          ParcelFileDescriptor result = newParcelFileDescriptor();
          ShadowParcelFileDescriptor shadowResult = Shadow.extract(result);
          shadowResult.lazyFileId = fileId;
          return result;
        }

        @Override
        public ParcelFileDescriptor[] newArray(int size) {
          return new ParcelFileDescriptor[size];
        }
      };

  @Implementation
  protected void writeToParcel(Parcel out, int flags) {
    if (fileIdPledgedOnClose == 0) {
      fileIdPledgedOnClose = (lazyFileId != 0) ? lazyFileId : NEXT_FILE_ID.incrementAndGet();
    }
    out.writeInt(fileIdPledgedOnClose);

    if ((flags & PARCELABLE_WRITE_RETURN_VALUE) != 0) {
      try {
        close();
      } catch (IOException e) {
        // Close "quietly", just like Android does.
      }
    }
  }

  private static ParcelFileDescriptor newParcelFileDescriptor() {
    if (RuntimeEnvironment.getApiLevel() > JELLY_BEAN) {
      return new ParcelFileDescriptor(new FileDescriptor());
    } else {
      // In Jelly Bean, the ParcelFileDescriptor(FileDescriptor) constructor was non-public.
      return ReflectionHelpers.callConstructor(
          ParcelFileDescriptor.class,
          ClassParameter.from(FileDescriptor.class, new FileDescriptor()));
    }
  }

  @Implementation
  protected static ParcelFileDescriptor open(File file, int mode) throws FileNotFoundException {
    ParcelFileDescriptor pfd = newParcelFileDescriptor();
    ShadowParcelFileDescriptor shadowParcelFileDescriptor = Shadow.extract(pfd);
    shadowParcelFileDescriptor.file = new RandomAccessFile(file, getFileMode(mode));
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
    return pfd;
  }

  @Implementation(minSdk = KITKAT)
  protected static ParcelFileDescriptor open(
      File file, int mode, Handler handler, ParcelFileDescriptor.OnCloseListener listener)
      throws IOException {
    if (handler == null) {
      throw new IllegalArgumentException("Handler must not be null");
    }
    if (listener == null) {
      throw new IllegalArgumentException("Listener must not be null");
    }
    ParcelFileDescriptor pfd = open(file, mode);
    ShadowParcelFileDescriptor shadowParcelFileDescriptor = Shadow.extract(pfd);
    shadowParcelFileDescriptor.handler = handler;
    shadowParcelFileDescriptor.onCloseListener = listener;
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

  private RandomAccessFile getFile() {
    if (file == null && lazyFileId != 0) {
      file = filesInTransitById.remove(lazyFileId);
      lazyFileId = 0;
      if (file == null) {
        throw new FileDescriptorFromParcelUnavailableException();
      }
    }
    return file;
  }

  @Implementation
  protected FileDescriptor getFileDescriptor() {
    try {
      return getFile().getFD();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  protected long getStatSize() {
    try {
      return getFile().length();
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
      return ReflectionHelpers.getField(getFile().getFD(), "fd");
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

    if (file != null) {
      if (fileIdPledgedOnClose != 0) {
        // Don't actually close 'file'! Instead stash it where our Parcel reader(s) can find it.
        filesInTransitById.put(fileIdPledgedOnClose, file);
        fileIdPledgedOnClose = 0;

        // Replace this.file with a dummy instance to be close()d below. This leaves instances that
        // have been written to Parcels and never-parceled ones in exactly the same state.
        File tempFile = Files.createTempFile(null, null).toFile();
        file = new RandomAccessFile(tempFile, "rw");
        tempFile.delete();
      }
      file.close();
    }

    reflector(ParcelFileDescriptorReflector.class, realParcelFd).close();
    closed = true;
    if (handler != null && onCloseListener != null) {
      handler.post(() -> onCloseListener.onClose(null));
    }
  }

  @Implementation
  protected ParcelFileDescriptor dup() throws IOException {
    return new ParcelFileDescriptor(realParcelFd);
  }

  static class FileDescriptorFromParcelUnavailableException extends RuntimeException {
    FileDescriptorFromParcelUnavailableException() {
      super(
          "ParcelFileDescriptors created from a Parcel refer to the same content as the"
              + " ParcelFileDescriptor that originally wrote it. Robolectric has the unfortunate"
              + " limitation that only one of these instances can be functional at a time. Try"
              + " closing the original ParcelFileDescriptor before using any duplicates created via"
              + " the Parcelable API.");
    }
  }

  @ForType(ParcelFileDescriptor.class)
  interface ParcelFileDescriptorReflector {

    @Direct
    void close();
  }
}
