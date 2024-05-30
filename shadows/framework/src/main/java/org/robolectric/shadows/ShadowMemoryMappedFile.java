package org.robolectric.shadows;

import android.system.ErrnoException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import libcore.io.BufferIterator;
import libcore.io.MemoryMappedFile;
import libcore.io.Streams;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/**
 * This is used by Android to load and inferFromValue time zone information. Robolectric emulates
 * this functionality by proxying to a time zone database file packaged into the android-all jar.
 */
@Implements(value = MemoryMappedFile.class, isInAndroidSdk = false)
public class ShadowMemoryMappedFile {
  protected byte[] bytes;
  private static final String TZ_DATA_1 = "/misc/zoneinfo/tzdata";
  private static final String TZ_DATA_2 = "/usr/share/zoneinfo/tzdata";
  private static final String TZ_DATA_3 = "/misc/zoneinfo/current/tzdata";

  @Implementation
  public static MemoryMappedFile mmapRO(String path) throws Throwable {
    if (path.endsWith(TZ_DATA_1) || path.endsWith(TZ_DATA_2) || path.endsWith(TZ_DATA_3)) {
      InputStream is = MemoryMappedFile.class.getResourceAsStream(TZ_DATA_2);
      if (is == null) {
        throw (Throwable)
            ErrnoException.class.getConstructor(String.class, int.class).newInstance("open", -1);
      }
      try {
        MemoryMappedFile memoryMappedFile = new MemoryMappedFile(0L, 0L);
        ShadowMemoryMappedFile shadowMemoryMappedFile = Shadow.extract(memoryMappedFile);
        shadowMemoryMappedFile.bytes = Streams.readFully(is);
        return memoryMappedFile;
      } catch (IOException e) {
        throw (Throwable)
            ErrnoException.class
                .getConstructor(String.class, int.class, Throwable.class)
                .newInstance("mmap", -1, e);
      }
    } else {
      throw new IllegalArgumentException("Unknown file for mmap: '" + path);
    }
  }

  @Implementation
  public synchronized void close() throws Exception {
    bytes = null;
  }

  @Implementation
  public BufferIterator bigEndianIterator() {
    return getHeapBufferIterator(ByteOrder.BIG_ENDIAN);
  }

  @Implementation
  public BufferIterator littleEndianIterator() {
    return getHeapBufferIterator(ByteOrder.LITTLE_ENDIAN);
  }

  protected BufferIterator getHeapBufferIterator(ByteOrder endianness) {
    return new RoboBufferIterator(bytes, endianness);
  }

  @Implementation
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  public int size() {
    return bytes.length;
  }

  protected static class RoboBufferIterator extends BufferIterator {
    protected final ByteBuffer buffer;

    public RoboBufferIterator(byte[] buffer, ByteOrder order) {
      this.buffer = ByteBuffer.wrap(buffer);
    }

    @Override
    public void seek(int offset) {
      ((Buffer) buffer).position(offset);
    }

    @Override
    public void skip(int byteCount) {
      ((Buffer) buffer).position(buffer.position() + byteCount);
    }

    @Override
    public int pos() {
      return 0;
    }

    @Override
    public void readByteArray(byte[] dst, int dstOffset, int byteCount) {
      System.arraycopy(buffer.array(), buffer.position(), dst, dstOffset, byteCount);
      skip(byteCount);
    }

    @Override
    public byte readByte() {
      return buffer.get();
    }

    @Override
    public int readInt() {
      return buffer.getInt();
    }

    @Override
    public void readIntArray(int[] dst, int dstOffset, int intCount) {
      for (int i = 0; i < intCount; i++) {
        dst[dstOffset + i] = buffer.getInt();
      }
    }

    @Override
    public void readLongArray(long[] dst, int dstOffset, int longCount) {
      for (int i = 0; i < longCount; i++) {
        dst[dstOffset + i] = buffer.getLong();
      }
    }

    @Override
    public short readShort() {
      return buffer.getShort();
    }
  }
}
