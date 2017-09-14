package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.ResStringPool;

@Implements(className = "android.content.res.StringBlock", isInAndroidSdk = false)
public class ShadowStringBlock {

  @RealObject
  Object realObject;

  private static final NativeObjRegistry<ResStringPool> NATIVE_STRING_BLOCKS = new NativeObjRegistry<>();

  static long getNativePointer(ResStringPool tableStringBlock) {
    return NATIVE_STRING_BLOCKS.getNativeObjectId(tableStringBlock);
  }

  public static void removeNativePointer(ResStringPool removed) {
    NATIVE_STRING_BLOCKS.unregister(removed);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeGetSize(int nativeId) {
    return nativeGetSize((long) nativeId);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeGetSize(long nativeId) {
    return NATIVE_STRING_BLOCKS.getNativeObject(nativeId).size();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static String nativeGetString(int nativeId, int index) {
    return nativeGetString((long) nativeId, index);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static String nativeGetString(long nativeId, int index) {
    return NATIVE_STRING_BLOCKS.getNativeObject(nativeId).stringAt(index);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static int[] nativeGetStyle(int var0, int var2) {
    return nativeGetStyle((long) var0, var2);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int[] nativeGetStyle(long var0, int var2) {
    // TODO: implement me properly
    // throw new UnsupportedOperationException("Implement me");
    return null;
  }

  @Resetter
  public static void reset() {
    NATIVE_STRING_BLOCKS.clear();
  }
}
