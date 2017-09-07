package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.HashMap;
import java.util.Map;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.ResStringPool;
import org.robolectric.util.ReflectionHelpers;

@Implements(className = "android.content.res.StringBlock", isInAndroidSdk = false)
public class ShadowStringBlock {

  @RealObject
  Object realObject;

  private static long ids = 0;
  private static BiMap<ResStringPool, Long> nativeObjToIdMap = HashBiMap.create();

  public long getNativePointer() {
    return ReflectionHelpers.getField(realObject, "mNative");
  }

  public static long getNativePointer(ResStringPool tableStringBlock) {
    Preconditions.checkNotNull(tableStringBlock);
    Long nativeId  = nativeObjToIdMap.get(tableStringBlock);
    if (nativeId == null) {
      nativeId = ids;
      nativeObjToIdMap.put(tableStringBlock, nativeId);
      ids++;
    }
    return nativeId;
  }

  public static void removeNativePointer(ResStringPool removed) {
    nativeObjToIdMap.remove(removed);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeGetSize(int nativeId) {
    return nativeGetSize((long) nativeId);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeGetSize(long nativeId) {
    ResStringPool resStringPool = nativeObjToIdMap.inverse().get(nativeId);
    Preconditions.checkNotNull(resStringPool);
    return resStringPool.size();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static String nativeGetString(int nativeId, int index) {
    return nativeGetString((long) nativeId, index);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static String nativeGetString(long nativeId, int index) {
    ResStringPool resStringPool = nativeObjToIdMap.inverse().get(nativeId);
    Preconditions.checkNotNull(resStringPool);
    return resStringPool.stringAt(index);
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
}
