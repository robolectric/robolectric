package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import com.google.common.base.Preconditions;
import dalvik.system.VMRuntime;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value = VMRuntime.class, isInAndroidSdk = false)
public class ShadowVMRuntime {

  /**
   * A map of allocated non movable arrays to the (Direct)ByteBuffer backing it
   *
   * <p>The JVM does not directly support newNonMovableArray. So as a workaround, this class will
   * allocate a direct ByteBuffer for use in native code. It is the responsibility the shadow code
   * to update any associated buffers with the data from native code.
   */
  private final Map<Object, ByteBuffer> realNonMovableArrays =
      Collections.synchronizedMap(new WeakHashMap<>());

  private final Map<Long, WeakReference<Object>> nonMovableArraysReverse =
      Collections.synchronizedMap(new HashMap<>());

  /**
   * Currently, {@link android.content.res.TypedArray} uses newNonMovableArray, but does not need to
   * access the data from native code. So in this case we will allocate a fake pointer.
   */
  private final Map<Object, Long> fakeNonMovableArrays =
      Collections.synchronizedMap(new WeakHashMap<>());

  private final AtomicLong nextFakeArrayPointer = new AtomicLong();

  // This is a hack to get the address of a DirectByteBuffer. The Method object is cached to reduce
  // the overhead of reflection. This method is invoked extensively during layout inflation. This
  // reflection requires the `--add-opens=java.base/java.nio=ALL-UNNAMED` JVM flag. This value is
  // lazy so tests can avoid having to add the flag where it is not needed.
  private static Method addressMethod;

  // There actually isn't any android JNI code to call through to in Robolectric due to
  // cross-platform compatibility issues. We default to a reasonable value that reflects the devices
  // that would commonly run this code.
  private static boolean is64Bit = true;

  @Nullable private static String currentInstructionSet = null;

  @Implementation
  public Object newUnpaddedArray(Class<?> klass, int size) {
    return Array.newInstance(klass, size);
  }

  @Implementation
  public Object newNonMovableArray(Class<?> type, int size) {
    Preconditions.checkArgument(
        type == int.class || type == float.class, "unsupported type %s", type.getName());
    Object arrayInstance = Array.newInstance(type, size);
    if (type == float.class && size == 8) {
      // This is being called from android.graphics.PathIterator, so we need to allocate a real
      // ByteBuffer that can be accessed from native code.
      ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * size);
      byteBuffer.order(ByteOrder.nativeOrder());
      realNonMovableArrays.put(arrayInstance, byteBuffer);
      nonMovableArraysReverse.put(
          getAddressOfDirectByteBuffer(byteBuffer), new WeakReference<>(arrayInstance));
    } else {
      // This is being called from android.content.res.TypedArray, so we need to allocate a fake
      // pointer.
      long fakePointer = nextFakeArrayPointer.incrementAndGet();
      fakeNonMovableArrays.put(arrayInstance, fakePointer);
      nonMovableArraysReverse.put(fakePointer, new WeakReference<>(arrayInstance));
    }
    return arrayInstance;
  }

  /** Returns a unique identifier of the object instead of a 'native' address. */
  @Implementation
  public long addressOf(Object obj) {
    if (obj == null) {
      return 0;
    }
    Preconditions.checkArgument(
        obj.getClass().isArray(), "addressOf(Object) is only supported for array objects");
    Class<?> arrayClass = obj.getClass().getComponentType();
    Preconditions.checkArgument(
        arrayClass.isPrimitive(),
        "addressOf(Object) is only supported for primitive array objects");
    if (arrayClass == float.class && Array.getLength(obj) == 8) {
      // This is being called from android.graphics.PathIterator.
      ByteBuffer byteBuffer = realNonMovableArrays.get(obj);
      if (byteBuffer == null) {
        throw new IllegalArgumentException("Trying to get address of unknown object");
      }
      return getAddressOfDirectByteBuffer(byteBuffer);
    } else {
      // This is being called from android.content.res.TypedArray.
      Long address = fakeNonMovableArrays.get(obj);
      if (address == null) {
        throw new IllegalArgumentException("Trying to get address of unknown object");
      }
      return address;
    }
  }

  private long getAddressOfDirectByteBuffer(ByteBuffer byteBuffer) {
    synchronized (ShadowVMRuntime.class) {
      if (addressMethod == null) {
        try {
          addressMethod = Class.forName("java.nio.DirectByteBuffer").getMethod("address");
          addressMethod.setAccessible(true);
        } catch (ReflectiveOperationException e) {
          throw new LinkageError("Error accessing address method", e);
        }
      }
    }

    try {
      return (long) addressMethod.invoke(byteBuffer);
    } catch (ReflectiveOperationException e) {
      throw new LinkageError("Error invoking address method", e);
    }
  }

  /** Returns the object previously registered with {@link #addressOf(Object)}. */
  @Nullable
  Object getObjectForAddress(long address) {
    WeakReference<Object> weakReference = nonMovableArraysReverse.get(address);
    if (weakReference == null) {
      return null;
    }
    return weakReference.get();
  }

  /**
   * Returns whether the VM is running in 64-bit mode. Available in Android L+. Defaults to true.
   */
  @Implementation
  protected boolean is64Bit() {
    return ShadowVMRuntime.is64Bit;
  }

  /** Sets whether the VM is running in 64-bit mode. */
  public static void setIs64Bit(boolean is64Bit) {
    ShadowVMRuntime.is64Bit = is64Bit;
  }

  /** Returns the instruction set of the current runtime. */
  @Implementation
  protected static String getCurrentInstructionSet() {
    return currentInstructionSet;
  }

  /** Sets the instruction set of the current runtime. */
  public static void setCurrentInstructionSet(@Nullable String currentInstructionSet) {
    ShadowVMRuntime.currentInstructionSet = currentInstructionSet;
  }

  ByteBuffer getBackingBuffer(long address) {
    Object array = getObjectForAddress(address);
    if (array == null) {
      return null;
    }
    return realNonMovableArrays.get(array);
  }

  @Resetter
  public static void reset() {
    ShadowVMRuntime.is64Bit = true;
    ShadowVMRuntime.currentInstructionSet = null;
  }

  @Implementation(minSdk = Q)
  protected static int getNotifyNativeInterval() {
    // The value '384' is from
    // https://cs.android.com/android/platform/superproject/+/android-12.0.0_r18:art/runtime/gc/heap.h;l=172
    // Note that value returned is irrelevant for the JVM, it just has to be greater than zero to
    // avoid a divide-by-zero error in VMRuntime.notifyNativeAllocation.
    return 384; // must be greater than 0
  }
}
