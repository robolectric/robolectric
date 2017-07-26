package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.castNativePtr;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(Parcel.class)
@SuppressWarnings("unchecked")
public class ShadowParcel {
  @RealObject private Parcel realObject;
  private static final Map<Long, ByteBuffer> NATIVE_PTR_TO_PARCEL = new LinkedHashMap<>();
  private static long nextNativePtr = 1; // this needs to start above 0, which is a magic number to Parcel

  @Implementation
  public void writeByteArray(byte[] b, int offset, int len) {
    if (b == null) {
      realObject.writeInt(-1);
      return;
    }
    Number nativePtr = ReflectionHelpers.getField(realObject, "mNativePtr");
    nativeWriteByteArray(nativePtr.longValue(), b, offset, len);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeDataSize(int nativePtr) {
    return nativeDataSize((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeDataSize(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).dataSize();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeDataAvail(int nativePtr) {
    return nativeDataAvail((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeDataAvail(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).dataAvailable();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeDataPosition(int nativePtr) {
    return nativeDataPosition((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeDataPosition(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).dataPosition();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeDataCapacity(int nativePtr) {
    return nativeDataCapacity((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeDataCapacity(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).dataCapacity();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeSetDataSize(int nativePtr, int size) {
    nativeSetDataSize((long) nativePtr, size);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeSetDataSize(long nativePtr, int size) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).setDataSize(size);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeSetDataPosition(int nativePtr, int pos) {
    nativeSetDataPosition((long) nativePtr, pos);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeSetDataPosition(long nativePtr, int pos) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).setDataPosition(pos);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeSetDataCapacity(int nativePtr, int size) {
    nativeSetDataCapacity((long) nativePtr, size);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeSetDataCapacity(long nativePtr, int size) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).setDataCapacity(size);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteByteArray(int nativePtr, byte[] b, int offset, int len) {
    nativeWriteByteArray((long) nativePtr, b, offset, len);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeWriteByteArray(long nativePtr, byte[] b, int offset, int len) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeByteArray(b, offset, len);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteInt(int nativePtr, int val) {
    nativeWriteInt((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeWriteInt(long nativePtr, int val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeInt(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteLong(int nativePtr, long val) {
    nativeWriteLong((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeWriteLong(long nativePtr, long val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeLong(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteFloat(int nativePtr, float val) {
    nativeWriteFloat((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeWriteFloat(long nativePtr, float val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeFloat(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteDouble(int nativePtr, double val) {
    nativeWriteDouble((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeWriteDouble(long nativePtr, double val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeDouble(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteString(int nativePtr, String val) {
    nativeWriteString((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeWriteString(long nativePtr, String val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeString(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static byte[] nativeCreateByteArray(int nativePtr) {
    return nativeCreateByteArray((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static byte[] nativeCreateByteArray(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readByteArray();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeReadInt(int nativePtr) {
    return nativeReadInt((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeReadInt(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readInt();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static long nativeReadLong(int nativePtr) {
    return nativeReadLong((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static long nativeReadLong(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readLong();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static float nativeReadFloat(int nativePtr) {
    return nativeReadFloat((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static float nativeReadFloat(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readFloat();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static double nativeReadDouble(int nativePtr) {
    return nativeReadDouble((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static double nativeReadDouble(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readDouble();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static String nativeReadString(int nativePtr) {
    return nativeReadString((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static String nativeReadString(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readString();
  }

  @Implementation @HiddenApi
  synchronized public static Number nativeCreate() {
    long nativePtr = nextNativePtr++;
    NATIVE_PTR_TO_PARCEL.put(nativePtr, new ByteBuffer());
    return castNativePtr(nativePtr);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeFreeBuffer(int nativePtr) {
    nativeFreeBuffer((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeFreeBuffer(long nativePtr) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).clear();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeDestroy(int nativePtr) {
    nativeDestroy((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeDestroy(long nativePtr) {
    NATIVE_PTR_TO_PARCEL.remove(nativePtr);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static byte[] nativeMarshall(int nativePtr) {
    return nativeMarshall((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static byte[] nativeMarshall(long nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).toByteArray();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeUnmarshall(int nativePtr, byte[] data, int offset, int length) {
    nativeUnmarshall((long) nativePtr, data, offset, length);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeUnmarshall(long nativePtr, byte[] data, int offset, int length) {
    NATIVE_PTR_TO_PARCEL.put(nativePtr, ByteBuffer.fromByteArray(data, offset, length));
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeAppendFrom(int thisNativePtr, int otherNativePtr, int offset, int length) {
    nativeAppendFrom((long) thisNativePtr, otherNativePtr, offset, length);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeAppendFrom(long thisNativePtr, long otherNativePtr, int offset, int length) {
    ByteBuffer thisByteBuffer = NATIVE_PTR_TO_PARCEL.get(thisNativePtr);
    ByteBuffer otherByteBuffer = NATIVE_PTR_TO_PARCEL.get(otherNativePtr);
    thisByteBuffer.appendFrom(otherByteBuffer, offset, length);
  }
  
  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteInterfaceToken(int nativePtr, String interfaceName) {
    nativeWriteInterfaceToken((long) nativePtr, interfaceName);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeWriteInterfaceToken(long nativePtr, String interfaceName) {
    // Write StrictMode.ThreadPolicy bits (assume 0 for test).
    nativeWriteInt(nativePtr, 0);
    nativeWriteString(nativePtr, interfaceName);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeEnforceInterface(int nativePtr, String interfaceName) {
    nativeEnforceInterface((long) nativePtr, interfaceName);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeEnforceInterface(long nativePtr, String interfaceName) {
    // Consume StrictMode.ThreadPolicy bits (don't bother setting in test).
    nativeReadInt(nativePtr);
    String actualInterfaceName = nativeReadString(nativePtr);
    if (!Objects.equals(interfaceName, actualInterfaceName)) {
      throw new SecurityException("Binder invocation to an incorrect interface");
    }
  }

  private static class ByteBuffer {

    // List of elements where a pair is a piece of data and the sizeof that data
    private List<Pair<Integer, ?>> buffer = new ArrayList<>();
    private int index;

    /**
     * Removes all elements from the byte buffer
     */
    public void clear() {
      index = 0;
      buffer.clear();
    }

    /**
     * Reads a byte array from the byte buffer based on the current data position
     */
    public byte[] readByteArray() {
      int length = readInt();
      if (length == -1) {
        return null;
      }
      byte[] array = new byte[length];
      for (int i = 0; i < length; i++) {
        array[i] = readByte();
      }
      return array;
    }

    /**
     * Writes a byte to the byte buffer at the current data position
     */
    public void writeByte(byte b) {
      writeValue(Byte.SIZE / 8, b);
    }

    /**
     * Writes a byte array starting at offset for length bytes to the byte buffer at the current
     * data position
     */
    public void writeByteArray(byte[] b, int offset, int length) {
      writeInt(b.length);
      for (int i = offset; i < offset + length && i < b.length; i++) {
        writeByte(b[i]);
      }
    }

    /**
     * Reads a byte from the byte buffer based on the current data position
     */
    public byte readByte() {
      return readValue((byte) 0);
    }

    /**
     * Writes an int to the byte buffer at the current data position
     */
    public void writeInt(int i) {
      writeValue(Integer.SIZE / 8, i);
    }

    /**
     * Reads a int from the byte buffer based on the current data position
     */
    public int readInt() {
      return readValue(0);
    }

    /**
     * Writes a long to the byte buffer at the current data position
     */
    public void writeLong(long l) {
      writeValue(Long.SIZE / 8, l);
    }

    /**
     * Reads a long from the byte buffer based on the current data position
     */
    public long readLong() {
      return readValue(0L);
    }

    /**
     * Writes a float to the byte buffer at the current data position
     */
    public void writeFloat(float f) {
      writeValue(Float.SIZE / 8, f);
    }

    /**
     * Reads a float from the byte buffer based on the current data position
     */
    public float readFloat() {
      return readValue(0f);
    }

    /**
     * Writes a double to the byte buffer at the current data position
     */
    public void writeDouble(double d) {
      writeValue(Double.SIZE / 8, d);
    }

    /**
     * Reads a double from the byte buffer based on the current data position
     */
    public double readDouble() {
      return readValue(0d);
    }

    /**
     * Writes a String to the byte buffer at the current data position
     */
    public void writeString(String s) {
      int length = TextUtils.isEmpty(s) ? Integer.SIZE / 8 : s.length();
      writeValue(length, s);
    }

    /**
     * Reads a String from the byte buffer based on the current data position
     */
    public String readString() {
      return readValue(null);
    }

    /**
     * Appends the contents of the other byte buffer to this byte buffer
     * starting at offset and ending at length.
     *
     * @param other ByteBuffer to append to this one
     * @param offset number of bytes from beginning of byte buffer to start copy from
     * @param length number of bytes to copy
     */
    public void appendFrom(ByteBuffer other, int offset, int length) {
      int otherIndex = other.toIndex(offset);
      int otherEndIndex = other.toIndex(offset + length);
      for (int i = otherIndex; i < otherEndIndex && i < other.buffer.size(); i++) {
        int elementSize = other.buffer.get(i).first;
        Object elementValue = other.buffer.get(i).second;
        writeValue(elementSize, elementValue);
      }
    }

    /**
     * Creates a Byte buffer from a raw byte array.
     *
     * @param array byte array to read from
     * @param offset starting position in bytes to start reading array at
     * @param length number of bytes to read from array
     */
    public static ByteBuffer fromByteArray(byte[] array, int offset, int length) {
      ByteBuffer byteBuffer = new ByteBuffer();

      try {
        ByteArrayInputStream bis = new ByteArrayInputStream(array, offset,
            length);
        ObjectInputStream ois = new ObjectInputStream(bis);
        int numElements = ois.readInt();
        for (int i = 0; i < numElements; i++) {
          int sizeOf = ois.readInt();
          Object value = ois.readObject();
          byteBuffer.buffer.add(Pair.create(sizeOf, value));
        }
        return byteBuffer;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Converts a ByteBuffer to a raw byte array. This method should be
     * symmetrical with fromByteArray.
     */
    public byte[] toByteArray() {
      try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        int length = buffer.size();
        oos.writeInt(length);
        for (Pair<Integer, ?> element : buffer) {
          oos.writeInt(element.first);
          oos.writeObject(element.second);
        }
        return bos.toByteArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Number of unused bytes in this byte buffer.
     */
    public int dataAvailable() {
      return dataSize() - dataPosition();
    }

    /**
     * Total buffer size in bytes of byte buffer included unused space.
     */
    public int dataCapacity() {
      return dataSize();
    }

    /**
     * Current data position of byte buffer in bytes. Reads / writes are from this position.
     */
    public int dataPosition() {
      return toDataPosition(index);
    }

    /**
     * Current amount of bytes currently written for ByteBuffer.
     */
    public int dataSize() {
      int totalSize = totalSize();
      int dataPosition = dataPosition();
      return totalSize > dataPosition ? totalSize : dataPosition;
    }

    /**
     * Sets the current data position.
     *
     * @param pos
     *          Desired position in bytes
     */
    public void setDataPosition(int pos) {
      index = toIndex(pos);
    }

    public void setDataSize(int size) {
      // TODO
    }

    public void setDataCapacity(int size) {
      // TODO
    }

    private int totalSize() {
      int size = 0;
      for (Pair<Integer, ?> element : buffer) {
        size += element.first;
      }
      return size;
    }

    private <T> T readValue(T defaultValue) {
      return (index < buffer.size()) ? (T) buffer.get(index++).second : defaultValue;
    }

    private void writeValue(int i, Object o) {
      Pair<Integer, ?> value = Pair.create(i, o);
      if (index < buffer.size()) {
        buffer.set(index, value);
      } else {
        buffer.add(value);
      }
      index++;
    }

    private int toDataPosition(int index) {
      int pos = 0;
      for (int i = 0; i < index; i++) {
        pos += buffer.get(i).first;
      }
      return pos;
    }

    private int toIndex(int dataPosition) {
      int calculatedPos = 0;
      int i = 0;
      for (; i < buffer.size() && calculatedPos < dataPosition; i++) {
        calculatedPos += buffer.get(i).first;
      }
      return i;
    }
  }
}
