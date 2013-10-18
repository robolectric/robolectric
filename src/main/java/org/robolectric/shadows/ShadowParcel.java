package org.robolectric.shadows;

import android.os.Parcel;
import android.util.Pair;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.HiddenApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Implements(Parcel.class)
@SuppressWarnings("unchecked")
public class ShadowParcel {
  @RealObject private Parcel realObject;
  private static final Map<Integer, ByteBuffer> NATIVE_PTR_TO_PARCEL = new LinkedHashMap<Integer, ByteBuffer>();

  // Unfortunately method must be shadowed since the implementation in API 16
  // calls Arrays.checkOffsetAndCount which is not preset in most JDK implementations.
  @Implementation
  public void writeByteArray(byte[] b, int offset, int len) {
    if (b == null) {
      realObject.writeInt(-1);
      return;
    }
    nativeWriteByteArray((Integer) getPrivateField(realObject, "mNativePtr"), b, offset, len);
  }

  @Implementation @HiddenApi
  public static int nativeDataSize(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).dataSize();
  }

  @Implementation @HiddenApi
  public static int nativeDataAvail(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).dataAvailable();
  }

  @Implementation @HiddenApi
  public static int nativeDataPosition(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).dataPosition();
  }

  @Implementation @HiddenApi
  public static int nativeDataCapacity(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).dataCapacity();
  }

  @Implementation @HiddenApi
  public static void nativeSetDataSize(int nativePtr, int size) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).setDataSize(size);
  }

  @Implementation @HiddenApi
  public static void nativeSetDataPosition(int nativePtr, int pos) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).setDataPosition(pos);
  }

  @Implementation @HiddenApi
  public static void nativeSetDataCapacity(int nativePtr, int size) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).setDataCapacity(size);
  }

  @Implementation @HiddenApi
  public static void nativeWriteByteArray(int nativePtr, byte[] b, int offset, int len) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeByteArray(b, offset, len);
  }

  @Implementation @HiddenApi
  public static void nativeWriteInt(int nativePtr, int val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeInt(val);
  }

  @Implementation @HiddenApi
  public static void nativeWriteLong(int nativePtr, long val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeLong(val);
  }

  @Implementation @HiddenApi
  public static void nativeWriteFloat(int nativePtr, float val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeFloat(val);
  }

  @Implementation @HiddenApi
  public static void nativeWriteDouble(int nativePtr, double val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeDouble(val);
  }

  @Implementation @HiddenApi
  public static void nativeWriteString(int nativePtr, String val) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).writeString(val);
  }

  @Implementation @HiddenApi
  public static byte[] nativeCreateByteArray(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readByteArray();
  }

  @Implementation @HiddenApi
  public static int nativeReadInt(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readInt();
  }

  @Implementation @HiddenApi
  public static long nativeReadLong(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readLong();
  }

  @Implementation @HiddenApi
  public static float nativeReadFloat(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readFloat();
  }

  @Implementation @HiddenApi
  public static double nativeReadDouble(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readDouble();
  }

  @Implementation @HiddenApi
  public static String nativeReadString(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).readString();
  }

  @Implementation @HiddenApi
  public static int nativeCreate() {
    // Pick a native ptr that hasn't been used.
    int nativePtrUsed = 0;
    while (NATIVE_PTR_TO_PARCEL.containsKey(nativePtrUsed)) {
      nativePtrUsed++;
    }
    NATIVE_PTR_TO_PARCEL.put(nativePtrUsed, new ByteBuffer());
    return nativePtrUsed;
  }

  @Implementation @HiddenApi
  public static void nativeFreeBuffer(int nativePtr) {
    NATIVE_PTR_TO_PARCEL.get(nativePtr).clear();
  }

  @Implementation @HiddenApi
  public static void nativeDestroy(int nativePtr) {
    NATIVE_PTR_TO_PARCEL.remove(nativePtr);
  }

  @Implementation @HiddenApi
  public static byte[] nativeMarshall(int nativePtr) {
    return NATIVE_PTR_TO_PARCEL.get(nativePtr).toByteArray();
  }

  @Implementation @HiddenApi
  public static void nativeUnmarshall(int nativePtr, byte[] data, int offset, int length) {
    NATIVE_PTR_TO_PARCEL.put(nativePtr, ByteBuffer.fromByteArray(data, offset, length));
  }

  @Implementation @HiddenApi
  public static void nativeAppendFrom(int thisNativePtr, int otherNativePtr, int offset, int length) {
    ByteBuffer thisByteBuffer = NATIVE_PTR_TO_PARCEL.get(thisNativePtr);
    ByteBuffer otherByteBuffer = NATIVE_PTR_TO_PARCEL.get(otherNativePtr);
    thisByteBuffer.appendFrom(otherByteBuffer, offset, length);
  }

  private Object getPrivateField(Object o, String fieldName) {
    try {
      Field f = o.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      return f.get(o);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static class ByteBuffer {

    // List of elements where a pair is a piece of data and the sizeof that data
    private List<Pair<Integer, ?>> buffer = new ArrayList<Pair<Integer, ?>>();
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
      return readValue(0l);
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
      int length = s == null ? Integer.SIZE / 8 : s.length();
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

    private <T extends Object> T readValue(T defaultValue) {
      T value = (index < buffer.size()) ? (T) buffer.get(index++).second
          : defaultValue;
      return value;
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
