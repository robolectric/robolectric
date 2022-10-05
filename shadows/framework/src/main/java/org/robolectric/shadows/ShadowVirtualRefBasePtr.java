package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import com.android.internal.util.VirtualRefBasePtr;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.android.NativeObjRegistry;

@Implements(value = VirtualRefBasePtr.class, isInAndroidSdk = false)
public class ShadowVirtualRefBasePtr {
  private static final NativeObjRegistry<RefHolder> NATIVE_REGISTRY =
      new NativeObjRegistry<>(RefHolder.class);

  protected static synchronized <T> long put(T object) {
    return NATIVE_REGISTRY.register(new RefHolder<T>(object));
  }

  protected static synchronized <T> T get(long nativePtr, Class<T> clazz) {
    return clazz.cast(NATIVE_REGISTRY.getNativeObject(nativePtr).nativeThing);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static synchronized void nIncStrong(long ptr) {
    if (ptr == 0) {
      return;
    }
    NATIVE_REGISTRY.getNativeObject(ptr).incr();
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static synchronized void nDecStrong(long ptr) {
    if (ptr == 0) {
      return;
    }
    if (NATIVE_REGISTRY.getNativeObject(ptr).decr()) {
      NATIVE_REGISTRY.unregister(ptr);
    }
  }

  private static final class RefHolder<T> {
    private T nativeThing;
    private int refCount;

    private RefHolder(T object) {
      this.nativeThing = object;
    }

    private synchronized void incr() {
      refCount++;
    }

    private synchronized boolean decr() {
      refCount--;
      return refCount == 0;
    }
  }
}
