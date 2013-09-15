package org.robolectric.shadows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import android.os.Parcel;
import android.util.Pair;

@Implements(Parcel.class)
@SuppressWarnings("unchecked")
public class ShadowParcel {

  @RealObject
  private Parcel realObject;

  private static final Map<Integer, ByteBuffer> nativePtrToParcel = 
      new HashMap<Integer, ByteBuffer>();

  // Unfortunately method must be shadowed since the implementation in API 16
  // calls
  // Arrays.checkOffsetAndCount which is not preset in most JDK implementations.
  @Implementation
  public void writeByteArray(byte[] b, int offset, int len) {
    if (b == null) {
      realObject.writeInt(-1);
      return;
    }
    nativeWriteByteArray((Integer) getPrivateField(realObject, "mNativePtr"), b, offset, len);
  }

  @Implementation
  public static int nativeDataSize(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).dataSize();
  }

  @Implementation
  public static int nativeDataAvail(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).dataAvailable();
  }

  @Implementation
  public static int nativeDataPosition(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).dataPosition();
  }

  @Implementation
  public static int nativeDataCapacity(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).dataCapacity();
  }

  @Implementation
  public static void nativeSetDataSize(int nativePtr, int size) {
    nativePtrToParcel.get(nativePtr).setDataSize(size);
  }

  @Implementation
  public static void nativeSetDataPosition(int nativePtr, int pos) {
    nativePtrToParcel.get(nativePtr).setDataPosition(pos);
  }

  @Implementation
  public static void nativeSetDataCapacity(int nativePtr, int size) {
    nativePtrToParcel.get(nativePtr).setDataCapacity(size);
  }

  @Implementation
  public static void nativeWriteByteArray(int nativePtr, byte[] b, int offset, int len) {
    nativePtrToParcel.get(nativePtr).writeByteArray(b, offset, len);
  }

  @Implementation
  public static void nativeWriteInt(int nativePtr, int val) {
    nativePtrToParcel.get(nativePtr).writeInt(val);
  }

  @Implementation
  public static void nativeWriteLong(int nativePtr, long val) {
    nativePtrToParcel.get(nativePtr).writeLong(val);
  }

  @Implementation
  public static void nativeWriteFloat(int nativePtr, float val) {
    nativePtrToParcel.get(nativePtr).writeFloat(val);
  }

  @Implementation
  public static void nativeWriteDouble(int nativePtr, double val) {
    nativePtrToParcel.get(nativePtr).writeDouble(val);
  }

  @Implementation
  public static void nativeWriteString(int nativePtr, String val) {
    nativePtrToParcel.get(nativePtr).writeString(val);
  }

  @Implementation
  public static byte[] nativeCreateByteArray(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).readByteArray();
  }

  @Implementation
  public static int nativeReadInt(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).readInt();
  }

  @Implementation
  public static long nativeReadLong(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).readLong();
  }

  @Implementation
  public static float nativeReadFloat(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).readFloat();
  }

  @Implementation
  public static double nativeReadDouble(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).readDouble();
  }

  @Implementation
  public static String nativeReadString(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).readString();
  }

  @Implementation
  public static int nativeCreate() {
    // Pick a native ptr that hasn't been used.
    int nativePtrUsed = 0;
    while (nativePtrToParcel.containsKey(nativePtrUsed)) {
      nativePtrUsed++;
    }
    nativePtrToParcel.put(nativePtrUsed, new ByteBuffer());
    return nativePtrUsed;
  }

  @Implementation
  public static void nativeFreeBuffer(int nativePtr) {
    nativePtrToParcel.get(nativePtr).clear();
  }

  @Implementation
  public static void nativeDestroy(int nativePtr) {
    nativePtrToParcel.clear();
  }

  @Implementation
  public static byte[] nativeMarshall(int nativePtr) {
    return nativePtrToParcel.get(nativePtr).toByteArray();
  }

  @Implementation
  public static void nativeUnmarshall(int nativePtr, byte[] data, int offset, int length) {
    nativePtrToParcel.put(nativePtr, ByteBuffer.fromByteArray(data, offset, length));
  }

  @Implementation
  public static void nativeAppendFrom(int thisNativePtr, int otherNativePtr,
      int offset, int length) {
    ByteBuffer thisByteBuffer = nativePtrToParcel.get(thisNativePtr);
    ByteBuffer otherByteBuffer = nativePtrToParcel.get(otherNativePtr);
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
