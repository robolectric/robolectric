package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.RuntimeEnvironment.castNativePtr;

import android.os.BadParcelableException;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Robolectric's {@link Parcel} pretends to be backed by a byte buffer, closely matching {@link
 * Parcel}'s position, size, and capacity behavior. However, its internal pure-Java representation
 * is strongly typed, to detect non-portable code and common testing mistakes. It may throw {@link
 * IllegalArgumentException} or {@link IllegalStateException} for error-prone behavior normal {@link
 * Parcel} tolerates.
 */
@Implements(Parcel.class)
public class ShadowParcel {
  private static final String TAG = "Parcel";

  @RealObject private Parcel realObject;
  private static final NativeObjRegistry<ByteBuffer> NATIVE_BYTE_BUFFER_REGISTRY =
      new NativeObjRegistry<>(ByteBuffer.class);

  @Implementation(maxSdk = JELLY_BEAN_MR1)
  @SuppressWarnings("TypeParameterUnusedInFormals")
  protected <T extends Parcelable> T readParcelable(ClassLoader loader) {
    // prior to JB MR2, readParcelableCreator() is inlined here.
    Parcelable.Creator<?> creator = readParcelableCreator(loader);
    if (creator == null) {
      return null;
    }

    if (creator instanceof Parcelable.ClassLoaderCreator<?>) {
      Parcelable.ClassLoaderCreator<?> classLoaderCreator =
          (Parcelable.ClassLoaderCreator<?>) creator;
      return (T) classLoaderCreator.createFromParcel(realObject, loader);
    }
    return (T) creator.createFromParcel(realObject);
  }

  @HiddenApi
  @Implementation(minSdk = JELLY_BEAN_MR2)
  public Parcelable.Creator<?> readParcelableCreator(ClassLoader loader) {
    //note: calling `readString` will also consume the string, and increment the data-pointer.
    //which is exactly what we need, since we do not call the real `readParcelableCreator`.
    final String name = realObject.readString();
    if (name == null) {
      return null;
    }

    Parcelable.Creator<?> creator;
    try {
      // If loader == null, explicitly emulate Class.forName(String) "caller
      // classloader" behavior.
      ClassLoader parcelableClassLoader =
          (loader == null ? getClass().getClassLoader() : loader);
      // Avoid initializing the Parcelable class until we know it implements
      // Parcelable and has the necessary CREATOR field. http://b/1171613.
      Class<?> parcelableClass = Class.forName(name, false /* initialize */,
          parcelableClassLoader);
      if (!Parcelable.class.isAssignableFrom(parcelableClass)) {
        throw new BadParcelableException("Parcelable protocol requires that the "
            + "class implements Parcelable");
      }
      Field f = parcelableClass.getField("CREATOR");

      // this is a fix for JDK8<->Android VM incompatibility:
      // Apparently, JDK will not allow access to a public field if its
      // class is not visible (private or package-private) from the call-site.
      f.setAccessible(true);

      if ((f.getModifiers() & Modifier.STATIC) == 0) {
        throw new BadParcelableException("Parcelable protocol requires "
            + "the CREATOR object to be static on class " + name);
      }
      Class<?> creatorType = f.getType();
      if (!Parcelable.Creator.class.isAssignableFrom(creatorType)) {
        // Fail before calling Field.get(), not after, to avoid initializing
        // parcelableClass unnecessarily.
        throw new BadParcelableException("Parcelable protocol requires a "
            + "Parcelable.Creator object called "
            + "CREATOR on class " + name);
      }
      creator = (Parcelable.Creator<?>) f.get(null);
    } catch (IllegalAccessException e) {
      Log.e(TAG, "Illegal access when unmarshalling: " + name, e);
      throw new BadParcelableException(
          "IllegalAccessException when unmarshalling: " + name);
    } catch (ClassNotFoundException e) {
      Log.e(TAG, "Class not found when unmarshalling: " + name, e);
      throw new BadParcelableException(
          "ClassNotFoundException when unmarshalling: " + name);
    } catch (NoSuchFieldException e) {
      throw new BadParcelableException("Parcelable protocol requires a "
          + "Parcelable.Creator object called "
          + "CREATOR on class " + name);
    }
    if (creator == null) {
      throw new BadParcelableException("Parcelable protocol requires a "
          + "non-null Parcelable.Creator object called "
          + "CREATOR on class " + name);
    }
    return creator;
  }

  @Implementation
  protected void writeByteArray(byte[] b, int offset, int len) {
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
  protected static int nativeDataSize(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).dataSize();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeDataAvail(int nativePtr) {
    return nativeDataAvail((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeDataAvail(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).dataAvailable();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeDataPosition(int nativePtr) {
    return nativeDataPosition((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeDataPosition(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).dataPosition();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeDataCapacity(int nativePtr) {
    return nativeDataCapacity((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeDataCapacity(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).dataCapacity();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeSetDataSize(int nativePtr, int size) {
    nativeSetDataSize((long) nativePtr, size);
  }

  @Implementation(minSdk = LOLLIPOP)
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  protected static void nativeSetDataSize(long nativePtr, int size) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).setDataSize(size);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeSetDataPosition(int nativePtr, int pos) {
    nativeSetDataPosition((long) nativePtr, pos);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeSetDataPosition(long nativePtr, int pos) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).setDataPosition(pos);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeSetDataCapacity(int nativePtr, int size) {
    nativeSetDataCapacity((long) nativePtr, size);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeSetDataCapacity(long nativePtr, int size) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).setDataCapacityAtLeast(size);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteByteArray(int nativePtr, byte[] b, int offset, int len) {
    nativeWriteByteArray((long) nativePtr, b, offset, len);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeWriteByteArray(long nativePtr, byte[] b, int offset, int len) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).writeByteArray(b, offset, len);
  }

  // duplicate the writeBlob implementation from latest android, to avoid referencing the
  // non-existent-in-JDK java.util.Arrays.checkOffsetAndCount method.
  @Implementation(minSdk = M)
  protected void writeBlob(byte[] b, int offset, int len) {
    if (b == null) {
      realObject.writeInt(-1);
      return;
    }
    throwsIfOutOfBounds(b.length, offset, len);
    long nativePtr = ReflectionHelpers.getField(realObject, "mNativePtr");
    nativeWriteBlob(nativePtr, b, offset, len);
  }

  private static void throwsIfOutOfBounds(int len, int offset, int count) {
    if (len < 0) {
      throw new ArrayIndexOutOfBoundsException("Negative length: " + len);
    }

    if ((offset | count) < 0 || offset > len - count) {
      throw new ArrayIndexOutOfBoundsException();
    }
  }

  // nativeWriteBlob was introduced in lollipop, thus no need for a int nativePtr variant
  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeWriteBlob(long nativePtr, byte[] b, int offset, int len) {
    nativeWriteByteArray(nativePtr, b, offset, len);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteInt(int nativePtr, int val) {
    nativeWriteInt((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = R)
  protected static void nativeWriteInt(long nativePtr, int val) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).writeInt(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteLong(int nativePtr, long val) {
    nativeWriteLong((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = R)
  protected static void nativeWriteLong(long nativePtr, long val) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).writeLong(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteFloat(int nativePtr, float val) {
    nativeWriteFloat((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = R)
  protected static void nativeWriteFloat(long nativePtr, float val) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).writeFloat(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteDouble(int nativePtr, double val) {
    nativeWriteDouble((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = R)
  protected static void nativeWriteDouble(long nativePtr, double val) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).writeDouble(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteString(int nativePtr, String val) {
    nativeWriteString((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = Q)
  protected static void nativeWriteString(long nativePtr, String val) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).writeString(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeWriteStrongBinder(int nativePtr, IBinder val) {
    nativeWriteStrongBinder((long) nativePtr, val);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeWriteStrongBinder(long nativePtr, IBinder val) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).writeStrongBinder(val);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static byte[] nativeCreateByteArray(int nativePtr) {
    return nativeCreateByteArray((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static byte[] nativeCreateByteArray(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).createByteArray();
  }

  // nativeReadBlob was introduced in lollipop, thus no need for a int nativePtr variant
  @Implementation(minSdk = LOLLIPOP)
  protected static byte[] nativeReadBlob(long nativePtr) {
    return nativeCreateByteArray(nativePtr);
  }

  @Implementation(minSdk = O_MR1)
  protected static boolean nativeReadByteArray(long nativePtr, byte[] dest, int destLen) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).readByteArray(dest, destLen);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeReadInt(int nativePtr) {
    return nativeReadInt((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeReadInt(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).readInt();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static long nativeReadLong(int nativePtr) {
    return nativeReadLong((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static long nativeReadLong(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).readLong();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static float nativeReadFloat(int nativePtr) {
    return nativeReadFloat((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static float nativeReadFloat(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).readFloat();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static double nativeReadDouble(int nativePtr) {
    return nativeReadDouble((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static double nativeReadDouble(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).readDouble();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static String nativeReadString(int nativePtr) {
    return nativeReadString((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = Q)
  protected static String nativeReadString(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).readString();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  protected static IBinder nativeReadStrongBinder(int nativePtr) {
    return nativeReadStrongBinder((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static IBinder nativeReadStrongBinder(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).readStrongBinder();
  }

  @Implementation
  @HiddenApi
  public static Number nativeCreate() {
    return castNativePtr(NATIVE_BYTE_BUFFER_REGISTRY.register(new ByteBuffer()));
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeFreeBuffer(int nativePtr) {
    nativeFreeBuffer((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  protected static void nativeFreeBuffer(long nativePtr) {
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).clear();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeDestroy(int nativePtr) {
    nativeDestroy((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeDestroy(long nativePtr) {
    NATIVE_BYTE_BUFFER_REGISTRY.unregister(nativePtr);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static byte[] nativeMarshall(int nativePtr) {
    return nativeMarshall((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static byte[] nativeMarshall(long nativePtr) {
    return NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).toByteArray();
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeUnmarshall(int nativePtr, byte[] data, int offset, int length) {
    nativeUnmarshall((long) nativePtr, data, offset, length);
  }

  @Implementation(minSdk = LOLLIPOP)
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  protected static void nativeUnmarshall(long nativePtr, byte[] data, int offset, int length) {
    NATIVE_BYTE_BUFFER_REGISTRY.update(nativePtr, ByteBuffer.fromByteArray(data, offset, length));
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeAppendFrom(
      int thisNativePtr, int otherNativePtr, int offset, int length) {
    nativeAppendFrom((long) thisNativePtr, otherNativePtr, offset, length);
  }

  @Implementation(minSdk = LOLLIPOP)
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  protected static void nativeAppendFrom(
      long thisNativePtr, long otherNativePtr, int offset, int length) {
    ByteBuffer thisByteBuffer = NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(thisNativePtr);
    ByteBuffer otherByteBuffer = NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(otherNativePtr);
    thisByteBuffer.appendFrom(otherByteBuffer, offset, length);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeWriteInterfaceToken(int nativePtr, String interfaceName) {
    nativeWriteInterfaceToken((long) nativePtr, interfaceName);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeWriteInterfaceToken(long nativePtr, String interfaceName) {
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
  protected static void nativeEnforceInterface(long nativePtr, String interfaceName) {
    // Consume StrictMode.ThreadPolicy bits (don't bother setting in test).
    nativeReadInt(nativePtr);
    String actualInterfaceName = nativeReadString(nativePtr);
    if (!Objects.equals(interfaceName, actualInterfaceName)) {
      throw new SecurityException("Binder invocation to an incorrect interface");
    }
  }

  /**
   * Robolectric-specific error thrown when tests exercise error-prone behavior in Parcel.
   *
   * <p>Standard Android parcels rarely throw exceptions, but will happily behave in unintended
   * ways. Parcels are not strongly typed, so will happily re-interpret corrupt contents in ways
   * that cause hard-to-diagnose failures, or will cause tests to pass when they should not.
   * ShadowParcel attempts to detect these conditions.
   *
   * <p>This exception is package-private because production or test code should never catch or rely
   * on this, and may be changed to be an Error (rather than Exception) in the future.
   */
  static class UnreliableBehaviorError extends AssertionError {
    UnreliableBehaviorError(String message) {
      super(message);
    }

    UnreliableBehaviorError(String message, Throwable cause) {
      super(message, cause);
    }

    UnreliableBehaviorError(
        Class<?> clazz, int position, ByteBuffer.FakeEncodedItem item, String extraMessage) {
      super(
          String.format(
              Locale.US,
              "Looking for %s at position %d, found %s [%s] taking %d bytes, %s",
              clazz.getSimpleName(),
              position,
              item.value == null ? "null" : item.value.getClass().getSimpleName(),
              item.value,
              item.sizeBytes,
              extraMessage));
    }
  }

  /**
   * ByteBuffer pretends to be the underlying Parcel implementation.
   *
   * <p>It faithfully simulates Parcel's handling of position, size, and capacity, but is strongly
   * typed internally. It was debated whether this should instead faithfully represent Android's
   * Parcel bug-for-bug as a true byte array, along with all of its error-tolerant behavior and
   * ability essentially to {@code reinterpret_cast} data. However, the fail-fast behavior here has
   * found several test bugs and avoids reliance on architecture-specific details like Endian-ness.
   *
   * <p>Quirky behavior this explicitly emulates:
   *
   * <ul>
   *   <li>Continuing to read past the end returns zeros/nulls.
   *   <li>{@link setDataCapacity} never decreases buffer size.
   *   <li>It is possible to partially or completely overwrite byte ranges in the buffer.
   *   <li>Zero bytes can be exchanged between primitive data types and empty array/string.
   * </ul>
   *
   * <p>Quirky behavior this forbids:
   *
   * <ul>
   *   <li>Reading past the end after writing without calling setDataPosition(0), since there's no
   *       legitimate reason to do this, and is a very common test bug.
   *   <li>Writing one type and reading another; for example, writing a Long and reading two
   *       Integers, or writing a byte array and reading a String. This, effectively like {@code
   *       reinterpret_cast}, may not be portable across architectures.
   *   <li>Similarly, reading from objects that have been truncated or partially overwritten, or
   *       reading from the middle of them.
   *   <li>Using appendFrom to overwrite data, which in Parcel will overwrite the data <i>and</i>
   *       expand data size by the same amount, introducing empty gaps.
   *   <li>Reading from or marshalling buffers with uninitialized gaps (e.g. where data position was
   *       expanded but nothing was written)
   * </ul>
   *
   * <p>Possibly-unwanted divergent behavior:
   *
   * <ul>
   *   <li>Reading an object will often return the same instance that was written.
   *   <li>The marshalled form does not at all resemble Parcel's. This is to maintain compatibility
   *       with existing clients that rely on the Java-serialization-based format.
   *   <li>Uses substantially more memory, since each "byte" takes at minimum 4 bytes for a pointer,
   *       and even more for the overhead of allocating a record for each write. But note there is
   *       only at most one allocation for every 4 byte positions.
   * </ul>
   */
  private static class ByteBuffer {
    /** Number of bytes in Parcel used by an int, length, or anything smaller. */
    private static final int INT_SIZE_BYTES = 4;
    /** Number of bytes in Parcel used by a long or double. */
    private static final int LONG_OR_DOUBLE_SIZE_BYTES = 8;
    /** Immutable empty byte array. */
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];


    /** Representation for an item that has been serialized in a parcel. */
    private static class FakeEncodedItem implements Serializable {
      /** Number of consecutive bytes consumed by this object. */
      final int sizeBytes;
      /** The original typed value stored. */
      final Object value;
      /**
       * Whether this item's byte-encoding is all zero.
       *
       * <p>This is the one exception to strong typing in ShadowParcel. Since zero can be portably
       * handled by many primitive types as zeros, and strings and arrays as empty. Note that when
       * zeroes are successfully read, the size of this entry may be ignored and the position may
       * progress to the middle of this, which remains safe as long as types that handle zeros are
       * used.
       */
      final boolean isEncodedAsAllZeroBytes;

      FakeEncodedItem(int sizeBytes, Object value) {
        this.sizeBytes = sizeBytes;
        this.value = value;
        this.isEncodedAsAllZeroBytes = isEncodedAsAllZeroBytes(value);
      }
    }

    /**
     * A type-safe simulation of the Parcel's data buffer.
     *
     * <p>Each index represents a byte of the parcel. Instead of storing raw bytes, this contains
     * records containing both the original data (in its original Java type) as well as the length.
     * Consecutive indices will point to the same FakeEncodedItem instance; for example, an item
     * with sizeBytes of 24 will, in normal cases, have references from 24 consecutive indices.
     *
     * <p>There are two main fail-fast features in this type-safe buffer. First, objects may only be
     * read from the parcel as the same type they were stored with, enforced by casting. Second,
     * this fails fast when reading incomplete or partially overwritten items.
     *
     * <p>Even though writing a custom resizable array is a code smell vs ArrayList, arrays' fixed
     * capacity closely models Parcel's dataCapacity (which we emulate anyway), and bulk array
     * utilities are robust compared to ArrayList's bulk operations.
     */
    private FakeEncodedItem[] data;
    /** The read/write pointer. */
    private int dataPosition;
    /** The length of the buffer; the capacity is data.length. */
    private int dataSize;
    /**
     * Whether the next read should fail if it's past the end of the array.
     *
     * <p>This is set true when modifying the end of the buffer, and cleared if a data position was
     * explicitly set.
     */
    private boolean failNextReadIfPastEnd;

    ByteBuffer() {
      clear();
    }

    /** Removes all elements from the byte buffer */
    public void clear() {
      data = new FakeEncodedItem[0];
      dataPosition = 0;
      dataSize = 0;
      failNextReadIfPastEnd = false;
    }

    /** Reads a byte array from the byte buffer based on the current data position */
    public byte[] createByteArray() {
      // It would be simpler just to store the byte array without a separate length.  However, the
      // "non-native" code in Parcel short-circuits null to -1, so this must consistently write a
      // separate length field in all cases.
      int length = readInt();
      if (length == -1) {
        return null;
      }
      if (length == 0) {
        return EMPTY_BYTE_ARRAY;
      }
      Object current = peek();
      if (current instanceof Byte) {
        // Legacy-encoded byte arrays (created by some tests) encode individual bytes, and do not
        // align to the integer.
        return readLegacyByteArray(length);
      } else if (readZeroes(alignToInt(length))) {
        return new byte[length];
      }
      byte[] result = readValue(EMPTY_BYTE_ARRAY, byte[].class, /* allowNull= */ false);
      if (result.length != length) {
        // Looks like the length doesn't correspond to the array.
        throw new UnreliableBehaviorError(
            String.format(
                Locale.US,
                "Byte array's length prefix is %d but real length is %d",
                length,
                result.length));
      }
      return result;
    }

    /** Reads a byte array encoded the way ShadowParcel previously encoded byte arrays. */
    private byte[] readLegacyByteArray(int length) {
      // Some tests rely on ShadowParcel's previous byte-by-byte encoding.
      byte[] result = new byte[length];
      for (int i = 0; i < length; i++) {
        result[i] = readPrimitive(1, (byte) 0, Byte.class);
      }
      return result;
    }

    /**
     * Reads a byte array from the byte buffer based on the current data position
     */
    public boolean readByteArray(byte[] dest, int destLen) {
      byte[] result = createByteArray();
      if (result == null || destLen != result.length) {
        // Since older versions of Android (pre O MR1) don't call this method at all, let's be more
        // consistent with them and let android.os.Parcel throw RuntimeException, instead of
        // throwing a more helpful exception.
        return false;
      }
      System.arraycopy(result, 0, dest, 0, destLen);
      return true;
    }

    /**
     * Writes a byte array starting at offset for length bytes to the byte buffer at the current
     * data position
     */
    public void writeByteArray(byte[] b, int offset, int length) {
      writeInt(length);
      // Native parcel writes a byte array as length plus the individual bytes.  But we can't write
      // bytes individually because each byte would take up 4 bytes due to Parcel's alignment
      // behavior.  Instead we write the length, and if non-empty, we write the array.
      if (length != 0) {
        writeValue(length, Arrays.copyOfRange(b, offset, offset + length));
      }
    }

    /**
     * Writes an int to the byte buffer at the current data position
     */
    public void writeInt(int i) {
      writeValue(INT_SIZE_BYTES, i);
    }

    /**
     * Reads a int from the byte buffer based on the current data position
     */
    public int readInt() {
      return readPrimitive(INT_SIZE_BYTES, 0, Integer.class);
    }

    /**
     * Writes a long to the byte buffer at the current data position
     */
    public void writeLong(long l) {
      writeValue(LONG_OR_DOUBLE_SIZE_BYTES, l);
    }

    /**
     * Reads a long from the byte buffer based on the current data position
     */
    public long readLong() {
      return readPrimitive(LONG_OR_DOUBLE_SIZE_BYTES, 0L, Long.class);
    }

    /**
     * Writes a float to the byte buffer at the current data position
     */
    public void writeFloat(float f) {
      writeValue(INT_SIZE_BYTES, f);
    }

    /**
     * Reads a float from the byte buffer based on the current data position
     */
    public float readFloat() {
      return readPrimitive(INT_SIZE_BYTES, 0f, Float.class);
    }

    /**
     * Writes a double to the byte buffer at the current data position
     */
    public void writeDouble(double d) {
      writeValue(LONG_OR_DOUBLE_SIZE_BYTES, d);
    }

    /**
     * Reads a double from the byte buffer based on the current data position
     */
    public double readDouble() {
      return readPrimitive(LONG_OR_DOUBLE_SIZE_BYTES, 0d, Double.class);
    }

    /** Writes a String to the byte buffer at the current data position */
    public void writeString(String s) {
      int nullTerminatedChars = (s != null) ? (s.length() + 1) : 0;
      // Android encodes strings as length plus a null-terminated array of 2-byte characters.
      // writeValue will pad to nearest 4 bytes.  Null is encoded as just -1.
      int sizeBytes = INT_SIZE_BYTES + (nullTerminatedChars * 2);
      writeValue(sizeBytes, s);
    }

    /**
     * Reads a String from the byte buffer based on the current data position
     */
    public String readString() {
      if (readZeroes(INT_SIZE_BYTES * 2)) {
        // Empty string is 4 bytes for length of 0, and 4 bytes for null terminator and padding.
        return "";
      }
      return readValue(null, String.class, /* allowNull= */ true);
    }

    /**
     * Writes an IBinder to the byte buffer at the current data position
     */
    public void writeStrongBinder(IBinder b) {
      // Size of struct flat_binder_object in android/binder.h used to encode binders in the real
      // parceling code.
      int length = 5 * INT_SIZE_BYTES;
      writeValue(length, b);
    }

    /**
     * Reads an IBinder from the byte buffer based on the current data position
     */
    public IBinder readStrongBinder() {
      return readValue(null, IBinder.class, /* allowNull= */ true);
    }

    /**
     * Appends the contents of the other byte buffer to this byte buffer starting at offset and
     * ending at length.
     *
     * @param other ByteBuffer to append to this one
     * @param offset number of bytes from beginning of byte buffer to start copy from
     * @param length number of bytes to copy
     */
    public void appendFrom(ByteBuffer other, int offset, int length) {
      int oldSize = dataSize;
      if (dataPosition != dataSize) {
        // Parcel.cpp will always expand the buffer by length even if it is overwriting existing
        // data, yielding extra uninitialized data at the end, in contrast to write methods that
        // won't increase the data length if they are overwriting in place.  This is surprising
        // behavior that production code should avoid.
        throw new UnreliableBehaviorError(
            "Real Android parcels behave unreliably if appendFrom is "
                + "called from any position other than the end");
      }
      setDataSize(oldSize + length);
      // Just blindly copy whatever happens to be in the buffer.  Reads will validate whether any
      // of the objects were only incompletely copied.
      System.arraycopy(other.data, offset, data, dataPosition, length);
      dataPosition += length;
      failNextReadIfPastEnd = true;
    }

    /** Returns whether a data type is encoded as all zeroes. */
    private static boolean isEncodedAsAllZeroBytes(Object value) {
      if (value == null) {
        return false; // Nulls are usually encoded as -1.
      }
      if (value instanceof Number) {
        Number number = (Number) value;
        return number.longValue() == 0 && number.doubleValue() == 0;
      }
      if (value instanceof byte[]) {
        byte[] array = (byte[]) value;
        return isAllZeroes(array, 0, array.length);
      }
      // NOTE: While empty string is all zeros, trying to read an empty string as zeroes is
      // probably unintended; the reverse is supported just so all-zero buffers don't fail.
      return false;
    }

    /** Identifies all zeroes, which can be safely reinterpreted to other types. */
    private static boolean isAllZeroes(byte[] array, int offset, int length) {
      for (int i = offset; i < length; i++) {
        if (array[i] != 0) {
          return false;
        }
      }
      return true;
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

      if (isAllZeroes(array, offset, length)) {
        // Special case: for all zeroes, it's definitely not an ObjectInputStream, because it has a
        // non-zero mandatory magic.  Zeroes have a portable, unambiguous interpretation.
        byteBuffer.setDataSize(length);
        byteBuffer.writeItem(new FakeEncodedItem(length, new byte[length]));
        return byteBuffer;
      }

      try {
        ByteArrayInputStream bis = new ByteArrayInputStream(array, offset,
            length);
        ObjectInputStream ois = new ObjectInputStream(bis);
        int numElements = ois.readInt();
        for (int i = 0; i < numElements; i++) {
          int sizeOf = ois.readInt();
          Object value = ois.readObject();
          // NOTE: Bypassing writeValue so that this will support ShadowParcels that were
          // marshalled before ShadowParcel simulated alignment.
          byteBuffer.writeItem(new FakeEncodedItem(sizeOf, value));
        }
        // Android leaves the data position at the end in this case.
        return byteBuffer;
      } catch (Exception e) {
        throw new UnreliableBehaviorError("ShadowParcel unable to unmarshall its custom format", e);
      }
    }

    /**
     * Converts a ByteBuffer to a raw byte array. This method should be symmetrical with
     * fromByteArray.
     */
    public byte[] toByteArray() {
      int oldDataPosition = dataPosition;
      try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        // NOTE: Serializing the data array would be simpler, and serialization would actually
        // preserve reference equality between entries.  However, the length-encoded format here
        // preserves the previous format, which some tests appear to rely on.
        List<FakeEncodedItem> entries = new ArrayList<>();
        // NOTE: Use readNextItem to scan so the contents can be proactively validated.
        dataPosition = 0;
        while (dataPosition < dataSize) {
          entries.add(readNextItem(Object.class));
        }
        oos.writeInt(entries.size());
        for (FakeEncodedItem item : entries) {
          oos.writeInt(item.sizeBytes);
          oos.writeObject(item.value);
        }
        oos.flush();
        return bos.toByteArray();
      } catch (IOException e) {
        throw new UnreliableBehaviorError("ErrorProne unable to serialize its custom format", e);
      } finally {
        dataPosition = oldDataPosition;
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
      return data.length;
    }

    /**
     * Current data position of byte buffer in bytes. Reads / writes are from this position.
     */
    public int dataPosition() {
      return dataPosition;
    }

    /**
     * Current amount of bytes currently written for ByteBuffer.
     */
    public int dataSize() {
      return dataSize;
    }

    /**
     * Sets the current data position.
     *
     * @param pos
     *          Desired position in bytes
     */
    public void setDataPosition(int pos) {
      if (pos > dataSize) {
        // NOTE: Real parcel ignores this until a write occurs.
        throw new UnreliableBehaviorError(pos + " greater than dataSize " + dataSize);
      }
      dataPosition = pos;
      failNextReadIfPastEnd = false;
    }

    public void setDataSize(int size) {
      if (size < dataSize) {
        // Clear all the inaccessible bytes when shrinking, to allow garbage collection, and so
        // they remain cleared if expanded again.  Note this might truncate something mid-object,
        // which would be handled at read time.
        Arrays.fill(data, size, dataSize, null);
      }
      setDataCapacityAtLeast(size);
      dataSize = size;
      if (dataPosition >= dataSize) {
        dataPosition = dataSize;
      }
    }

    public void setDataCapacityAtLeast(int newCapacity) {
      // NOTE: Oddly, Parcel only every increases data capacity, and never decreases it, so this
      // really should have never been named setDataCapacity.
      if (newCapacity > data.length) {
        FakeEncodedItem[] newData = new FakeEncodedItem[newCapacity];
        dataSize = Math.min(dataSize, newCapacity);
        dataPosition = Math.min(dataPosition, dataSize);
        System.arraycopy(data, 0, newData, 0, dataSize);
        data = newData;
      }
    }

    /** Rounds to next 4-byte bounder similar to native Parcel. */
    private int alignToInt(int unpaddedSizeBytes) {
      return ((unpaddedSizeBytes + 3) / 4) * 4;
    }

    /**
     * Ensures that the next sizeBytes are all the initial value we read.
     *
     * <p>This detects:
     *
     * <ul>
     *   <li>Reading an item, but not starting at its start position
     *   <li>Reading items that were truncated by setSize
     *   <li>Reading items that were partially overwritten by another
     * </ul>
     */
    private void checkConsistentReadAndIncrementPosition(Class<?> clazz, FakeEncodedItem item) {
      int endPosition = dataPosition + item.sizeBytes;
      for (int i = dataPosition; i < endPosition; i++) {
        FakeEncodedItem foundItemItem = i < dataSize ? data[i] : null;
        if (foundItemItem != item) {
          throw new UnreliableBehaviorError(
              clazz,
              dataPosition,
              item,
              String.format(
                  Locale.US,
                  "but [%s] interrupts it at position %d",
                  foundItemItem == null
                      ? "uninitialized data or the end of the buffer"
                      : foundItemItem.value,
                  i));
        }
      }
      dataPosition = Math.min(dataSize, dataPosition + item.sizeBytes);
    }

    /** Returns the item at the current position, or null if uninitialized or null. */
    private Object peek() {
      return dataPosition < dataSize && data[dataPosition] != null
          ? data[dataPosition].value
          : null;
    }

    /**
     * Reads a complete item in the byte buffer.
     *
     * @param clazz this is the type that is being read, but not checked in this method
     * @return null if the default value should be returned, otherwise the item holding the data
     */
    private <T> FakeEncodedItem readNextItem(Class<T> clazz) {
      FakeEncodedItem item = data[dataPosition];
      if (item == null) {
        // While Parcel will treat these as zeros, in tests, this is almost always an error.
        throw new UnreliableBehaviorError("Reading uninitialized data at position " + dataPosition);
      }
      checkConsistentReadAndIncrementPosition(clazz, item);
      return item;
    }

    /**
     * Reads the next value in the byte buffer of a specified type.
     *
     * @param pastEndValue value to return when reading past the end of the buffer
     * @param clazz this is the type that is being read, but not checked in this method
     * @param allowNull whether null values are permitted
     */
    private <T> T readValue(T pastEndValue, Class<T> clazz, boolean allowNull) {
      if (dataPosition >= dataSize) {
        // Normally, reading past the end is permitted, and returns the default values.  However,
        // writing to a parcel then reading without setting the position back to 0 is an incredibly
        // common error to make in tests, and should never really happen in production code, so
        // this shadow will fail in this condition.
        if (failNextReadIfPastEnd) {
          throw new UnreliableBehaviorError(
              "Did you forget to setDataPosition(0) before reading the parcel?");
        }
        return pastEndValue;
      }
      int startPosition = dataPosition;
      FakeEncodedItem item = readNextItem(clazz);
      if (item == null) {
        return pastEndValue;
      } else if (item.value == null && allowNull) {
        return null;
      } else if (clazz.isInstance(item.value)) {
        return clazz.cast(item.value);
      } else {
        // Numerous existing tests rely on ShadowParcel throwing RuntimeException and catching
        // them.  Many of these tests are trying to test what happens when an invalid Parcel is
        // provided.  However, Android has no concept of an "invalid parcel" because Parcel will
        // happily return garbage if you ask for it.  The only runtime exceptions are thrown on
        // array length mismatches, or higher-level APIs like Parcelable (which has its own safety
        // checks).  Tests trying to test error-handling behavior should instead craft a Parcel
        // that specifically triggers a BadParcelableException.
        throw new RuntimeException(
            new UnreliableBehaviorError(
                clazz, startPosition, item, "and it is non-portable to reinterpret it"));
      }
    }

    /**
     * Determines if there is a sequence of castable zeroes, and consumes them.
     *
     * <p>This is the only exception for strong typing, because zero bytes are portable and
     * unambiguous. There are a few situations where well-written code can rely on this, so it is
     * worthwhile making a special exception for. This tolerates partially-overwritten and truncated
     * values if all bytes are zero.
     */
    private boolean readZeroes(int bytes) {
      int endPosition = dataPosition + bytes;
      if (endPosition > dataSize) {
        return false;
      }
      for (int i = dataPosition; i < endPosition; i++) {
        if (data[i] == null || !data[i].isEncodedAsAllZeroBytes) {
          return false;
        }
      }
      // Note in this case we short-circuit other verification -- even if we are reading weirdly
      // clobbered zeroes, they're still zeroes.  Future reads might fail, though.
      dataPosition = endPosition;
      return true;
    }

    /**
     * Reads a primitive, which may reinterpret zeros of other types.
     *
     * @param defaultSizeBytes if reinterpreting zeros, the number of bytes to consume
     * @param defaultValue the default value for zeros or reading past the end
     * @param clazz this is the type that is being read, but not checked in this method
     */
    private <T> T readPrimitive(int defaultSizeBytes, T defaultValue, Class<T> clazz) {
      // Check for zeroes first, since partially-overwritten values are not an error for zeroes.
      if (readZeroes(defaultSizeBytes)) {
        return defaultValue;
      }
      return readValue(defaultValue, clazz, /* allowNull= */ false);
    }

    /** Writes an encoded item directly, bypassing alignment, and possibly repeating an item. */
    private void writeItem(FakeEncodedItem item) {
      int endPosition = dataPosition + item.sizeBytes;
      if (endPosition > data.length) {
        // Parcel grows by 3/2 of the new size.
        setDataCapacityAtLeast(endPosition * 3 / 2);
      }
      if (endPosition > dataSize) {
        failNextReadIfPastEnd = true;
        dataSize = endPosition;
      }
      Arrays.fill(data, dataPosition, endPosition, item);
      dataPosition = endPosition;
    }

    /**
     * Writes a value to the next range of bytes.
     *
     * <p>Writes are aligned to 4-byte regions.
     */
    private void writeValue(int unpaddedSizeBytes, Object o) {
      // Create the item with its final, aligned byte size.
      writeItem(new FakeEncodedItem(alignToInt(unpaddedSizeBytes), o));
    }
  }

  @Implementation(maxSdk = P)
  protected static FileDescriptor openFileDescriptor(String file, int mode) throws IOException {
    RandomAccessFile randomAccessFile =
        new RandomAccessFile(file, mode == ParcelFileDescriptor.MODE_READ_ONLY ? "r" : "rw");
    return randomAccessFile.getFD();
  }

  @Implementation(minSdk = M, maxSdk = R)
  protected static long nativeWriteFileDescriptor(long nativePtr, FileDescriptor val) {
    // The Java version of FileDescriptor stored the fd in a field called "fd", and the Android
    // version changed the field name to "descriptor". But it looks like Robolectric uses the
    // Java version of FileDescriptor instead of the Android version.
    int fd = ReflectionHelpers.getField(val, "fd");
    NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).writeInt(fd);
    return (long) nativeDataPosition(nativePtr);
  }

  @Implementation(minSdk = M)
  protected static FileDescriptor nativeReadFileDescriptor(long nativePtr) {
    int fd = NATIVE_BYTE_BUFFER_REGISTRY.getNativeObject(nativePtr).readInt();
    return ReflectionHelpers.callConstructor(
        FileDescriptor.class, ClassParameter.from(int.class, fd));
  }

  @Implementation(minSdk = R)
  protected static void nativeWriteString8(long nativePtr, String val) {
    nativeWriteString(nativePtr, val);
  }

  @Implementation(minSdk = R)
  protected static void nativeWriteString16(long nativePtr, String val) {
    nativeWriteString(nativePtr, val);
  }

  @Implementation(minSdk = R)
  protected static String nativeReadString8(long nativePtr) {
    return nativeReadString(nativePtr);
  }

  @Implementation(minSdk = R)
  protected static String nativeReadString16(long nativePtr) {
    return nativeReadString(nativePtr);
  }
}
