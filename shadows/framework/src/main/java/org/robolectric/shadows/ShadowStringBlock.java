package org.robolectric.shadows;

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

  @Implementation
  public static int nativeGetSize(long nativeId) {
    ResStringPool resStringPool = nativeObjToIdMap.inverse().get(nativeId);
    Preconditions.checkNotNull(resStringPool);
    return resStringPool.size();
  }

  @Implementation
  public static String nativeGetString(long nativeId, int index) {
    ResStringPool resStringPool = nativeObjToIdMap.inverse().get(nativeId);
    Preconditions.checkNotNull(resStringPool);
    return resStringPool.stringAt(index);
  }

  @Implementation
  public static int[] nativeGetStyle(long var0, int var2) {
    // TODO: implement me properly
    // throw new UnsupportedOperationException("Implement me");
    return null;
  }
}
