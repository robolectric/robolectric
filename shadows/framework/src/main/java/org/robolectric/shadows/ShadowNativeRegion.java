package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.Parcel;
import com.google.errorprone.annotations.DoNotCall;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RegionNatives;
import org.robolectric.shadows.ShadowNativeRegion.Picker;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link Region} that is backed by native code */
@Implements(value = Region.class, minSdk = O, shadowPicker = Picker.class, isInAndroidSdk = false)
public class ShadowNativeRegion {

  RegionNatives regionNatives = new RegionNatives();
  @RealObject Region realRegion;

  @Implementation(minSdk = O)
  protected void __constructor__(long ni) {
    invokeConstructor(Region.class, realRegion, ClassParameter.from(long.class, ni));
    regionNatives.mNativeRegion = ni;
  }

  @Implementation(minSdk = O)
  protected void __constructor__(int left, int top, int right, int bottom) {
    invokeConstructor(
        Region.class,
        realRegion,
        ClassParameter.from(int.class, left),
        ClassParameter.from(int.class, top),
        ClassParameter.from(int.class, right),
        ClassParameter.from(int.class, bottom));
    regionNatives.mNativeRegion = reflector(RegionReflector.class, realRegion).getNativeRegion();
  }

  @Implementation(minSdk = O)
  protected void __constructor__(Rect rect) {
    invokeConstructor(Region.class, realRegion, ClassParameter.from(Rect.class, rect));
    regionNatives.mNativeRegion = reflector(RegionReflector.class, realRegion).getNativeRegion();
  }

  @Implementation(minSdk = O)
  protected static boolean nativeEquals(long nativeR1, long nativeR2) {
    return RegionNatives.nativeEquals(nativeR1, nativeR2);
  }

  @Implementation(minSdk = O)
  protected static long nativeConstructor() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RegionNatives.nativeConstructor();
  }

  @Implementation(minSdk = O)
  protected static void nativeDestructor(long nativeRegion) {
    RegionNatives.nativeDestructor(nativeRegion);
  }

  @Implementation(minSdk = O)
  protected static void nativeSetRegion(long nativeDst, long nativeSrc) {
    RegionNatives.nativeSetRegion(nativeDst, nativeSrc);
  }

  @Implementation(minSdk = O)
  protected static boolean nativeSetRect(long nativeDst, int left, int top, int right, int bottom) {
    return RegionNatives.nativeSetRect(nativeDst, left, top, right, bottom);
  }

  @Implementation(minSdk = O)
  protected static boolean nativeSetPath(long nativeDst, long nativePath, long nativeClip) {
    return RegionNatives.nativeSetPath(nativeDst, nativePath, nativeClip);
  }

  @Implementation(minSdk = O)
  protected static boolean nativeGetBounds(long nativeRegion, Rect rect) {
    return RegionNatives.nativeGetBounds(nativeRegion, rect);
  }

  @Implementation(minSdk = O)
  protected static boolean nativeGetBoundaryPath(long nativeRegion, long nativePath) {
    return RegionNatives.nativeGetBoundaryPath(nativeRegion, nativePath);
  }

  @Implementation(minSdk = O)
  protected static boolean nativeOp(
      long nativeDst, int left, int top, int right, int bottom, int op) {
    return RegionNatives.nativeOp(nativeDst, left, top, right, bottom, op);
  }

  @Implementation(minSdk = O)
  protected static boolean nativeOp(long nativeDst, Rect rect, long nativeRegion, int op) {
    return RegionNatives.nativeOp(nativeDst, rect, nativeRegion, op);
  }

  @Implementation(minSdk = O)
  protected static boolean nativeOp(
      long nativeDst, long nativeRegion1, long nativeRegion2, int op) {
    return RegionNatives.nativeOp(nativeDst, nativeRegion1, nativeRegion2, op);
  }

  @DoNotCall("Always throws java.lang.UnsupportedOperationException")
  @Implementation(minSdk = O)
  protected static long nativeCreateFromParcel(Parcel p) {
    throw new UnsupportedOperationException();
  }

  @DoNotCall("Always throws java.lang.UnsupportedOperationException")
  @Implementation(minSdk = O)
  protected static boolean nativeWriteToParcel(long nativeRegion, Parcel p) {
    throw new UnsupportedOperationException();
  }

  @Implementation(minSdk = O)
  protected static String nativeToString(long nativeRegion) {
    return RegionNatives.nativeToString(nativeRegion);
  }

  @Implementation(minSdk = O)
  protected boolean isEmpty() {
    return regionNatives.isEmpty();
  }

  @Implementation(minSdk = O)
  protected boolean isRect() {
    return regionNatives.isRect();
  }

  @Implementation(minSdk = O)
  protected boolean isComplex() {
    return regionNatives.isComplex();
  }

  @Implementation(minSdk = O)
  protected boolean contains(int x, int y) {
    return regionNatives.contains(x, y);
  }

  @Implementation(minSdk = O)
  protected boolean quickContains(int left, int top, int right, int bottom) {
    return regionNatives.quickContains(left, top, right, bottom);
  }

  @Implementation(minSdk = O)
  protected boolean quickReject(int left, int top, int right, int bottom) {
    return regionNatives.quickReject(left, top, right, bottom);
  }

  @Implementation(minSdk = O)
  protected boolean quickReject(Region rgn) {
    return regionNatives.quickReject(rgn);
  }

  @Implementation(minSdk = O)
  protected void translate(int dx, int dy, Region dst) {
    regionNatives.translate(dx, dy, dst);
  }

  @Implementation(minSdk = O)
  protected void scale(float scale, Region dst) {
    regionNatives.scale(scale, dst);
  }

  @ForType(Region.class)
  interface RegionReflector {
    @Accessor("mNativeRegion")
    long getNativeRegion();
  }

  /** Shadow picker for {@link Region}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowRegion.class, ShadowNativeRegion.class);
    }
  }
}
