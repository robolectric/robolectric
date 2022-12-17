package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import com.android.internal.util.VirtualRefBasePtr;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.VirtualRefBasePtrNatives;
import org.robolectric.shadows.ShadowNativeVirtualRefBasePtr.Picker;

/** Shadow for {@link VirtualRefBasePtr} that is backed by native code */
@Implements(
    value = VirtualRefBasePtr.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeVirtualRefBasePtr {

  @Implementation(minSdk = O)
  protected static void nIncStrong(long ptr) {
    VirtualRefBasePtrNatives.nIncStrong(ptr);
  }

  @Implementation(minSdk = O)
  protected static void nDecStrong(long ptr) {
    VirtualRefBasePtrNatives.nDecStrong(ptr);
  }

  /** Shadow picker for {@link VirtualRefBasePtr}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowVirtualRefBasePtr.class, ShadowNativeVirtualRefBasePtr.class);
    }
  }
}
