package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.res.android.Util.SIZEOF_INT;

import java.nio.ByteBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.ResStringPool;
import org.robolectric.res.android.ResourceTypes.ResStringPool_span;

@Implements(className = "android.content.res.StringBlock", isInAndroidSdk = false)
public class ShadowStringBlock {

  @RealObject
  Object realObject;

  @Implementation
  protected static Number nativeCreate(byte[] data, int offset, int size) {
    throw new UnsupportedOperationException();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int nativeGetSize(int nativeId) {
    return nativeGetSize((long) nativeId);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeGetSize(long nativeId) {
    return ResStringPool.getNativeObject(nativeId).size();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static String nativeGetString(int nativeId, int index) {
    return nativeGetString((long) nativeId, index);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static String nativeGetString(long nativeId, int index) {
    return ResStringPool.getNativeObject(nativeId).stringAt(index);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int[] nativeGetStyle(int obj, int idx) {
    return nativeGetStyle((long) obj, idx);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int[] nativeGetStyle(long obj, int idx) {
    ResStringPool osb = ResStringPool.getNativeObject(obj);

    ResStringPool_span spans = osb.styleAt(idx);
    if (spans == null) {
      return null;
    }

    ResStringPool_span pos = spans;
    int num = 0;
    while (pos.name.index != ResStringPool_span.END) {
      num++;
      // pos++;
      pos = new ResStringPool_span(pos.myBuf(), pos.myOffset() + ResStringPool_span.SIZEOF);
    }

    if (num == 0) {
      return null;
    }

    // jintArray array = env->NewIntArray((num*sizeof(ResStringPool_span))/sizeof(jint));
    int[] array = new int[num * ResStringPool_span.SIZEOF / SIZEOF_INT];
    if (array == null) { // NewIntArray already threw OutOfMemoryError.
      return null;
    }

    num = 0;
    final int numInts = ResStringPool_span.SIZEOF / SIZEOF_INT;
    while (spans.name.index != ResStringPool_span.END) {
      // env->SetIntArrayRegion(array,
      //     num*numInts, numInts,
      //     (jint*)spans);
      setIntArrayRegion(array, num, numInts, spans);
      // spans++;
      spans = new ResStringPool_span(spans.myBuf(), spans.myOffset() + ResStringPool_span.SIZEOF);
      num++;
    }

    return array;
  }

  private static void setIntArrayRegion(int[] array, int num, int numInts, ResStringPool_span spans) {
    ByteBuffer buf = spans.myBuf();
    int startOffset = spans.myOffset();

    int start = num * numInts;
    for (int i = 0; i < numInts; i++) {
      array[start + i] = buf.getInt(startOffset + i * SIZEOF_INT);
    }
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeDestroy(int obj) {
    nativeDestroy((long) obj);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeDestroy(long obj) {
    throw new UnsupportedOperationException();
  }

  @Resetter
  public static void reset() {
    // NATIVE_STRING_POOLS.clear(); // nope!
  }
}
