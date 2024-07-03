package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S_V2;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.view.Surface;
import java.lang.ref.WeakReference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.ImageReaderNatives;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowNativeImageReader.Picker;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.versioning.AndroidVersions.T;
import org.robolectric.versioning.AndroidVersions.U;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for {@link ImageReader} that is backed by native code */
@Implements(
    value = ImageReader.class,
    minSdk = P,
    looseSignatures = true,
    isInAndroidSdk = false,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeImageReader {

  @RealObject ImageReader realImageReader;

  /**
   * The {@link ImageReader} static initializer invokes its own native methods in static
   * initializer. This has to be deferred starting in Android V.
   */
  @Implementation(minSdk = V.SDK_INT)
  protected static void __staticInitializer__() {
    // deferred
  }

  @ReflectorObject private ImageReaderReflector imageReaderReflector;
  private final ImageReaderNatives natives = new ImageReaderNatives();

  @Implementation(maxSdk = S_V2)
  protected synchronized void nativeInit(
      Object weakSelf, int w, int h, int fmt, int maxImgs, long consumerUsage) {
    natives.nativeInit(weakSelf, w, h, fmt, maxImgs, consumerUsage);
    imageReaderReflector.setMemberNativeContext(natives.mNativeContext);
  }

  @Implementation(minSdk = T.SDK_INT, maxSdk = U.SDK_INT)
  protected synchronized void nativeInit(
      Object weakSelf,
      int w,
      int h,
      int maxImgs,
      long consumerUsage,
      int hardwareBufferFormat,
      int dataSpace) {
    // Up to S, "fmt" is a PublicFormat (JNI), aka ImageFormat.format (java), which is then
    // split into a hal format + data space in the JNI code.
    // In T+, the hal format and data space are provided directly instead.
    // However the format values overlap and the conversion is merely a cast.
    // Reference: android12/.../frameworks/base/libs/hostgraphics/PublicFormat.cpp
    int fmt = hardwareBufferFormat;
    natives.nativeInit(weakSelf, w, h, fmt, maxImgs, consumerUsage);
    imageReaderReflector.setMemberNativeContext(natives.mNativeContext);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected void nativeClose() {
    natives.nativeClose();
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected void nativeReleaseImage(Image i) {
    natives.nativeReleaseImage(i);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected Surface nativeGetSurface() {
    return natives.nativeGetSurface();
  }

  @Implementation(maxSdk = S_V2)
  protected int nativeDetachImage(Image i) {
    return natives.nativeDetachImage(i);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected void nativeDiscardFreeBuffers() {
    natives.nativeDiscardFreeBuffers();
  }

  /**
   * This is a Java shim to support {@link
   * ImageReader#setOnImageAvailableListener(OnImageAvailableListener, Handler)}} until it is
   * supported in native libhostgraphics.
   */
  @Implementation
  protected Surface getSurface() {
    Surface surface = reflector(ImageReaderReflector.class, realImageReader).getSurface();
    ShadowNativeSurface shadowNativeSurface = Shadow.extract(surface);
    shadowNativeSurface.setContainerImageReader(realImageReader);
    return surface;
  }

  /**
   * @return A return code {@code ACQUIRE_*}
   */
  @Implementation(maxSdk = S_V2)
  protected int nativeImageSetup(Image i) {
    return natives.nativeImageSetup(i);
  }

  @Implementation(minSdk = T.SDK_INT, maxSdk = T.SDK_INT)
  protected int nativeImageSetup(Image i, boolean legacyValidateImageFormat) {
    return natives.nativeImageSetup(i);
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected Object nativeImageSetup(Object i) {
    // Note: reverted to Q-S API
    return natives.nativeImageSetup((Image) i);
  }

  /** We use a class initializer to allow the native code to cache some field offsets. */
  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeClassInit() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    ImageReaderNatives.nativeClassInit();
  }

  static void triggerOnImageAvailableCallbacks(ImageReader imageReader) {
    reflector(ImageReaderReflector.class).postEventFromNative(new WeakReference<>(imageReader));
  }

  @ForType(ImageReader.class)
  interface ImageReaderReflector {
    @Accessor("mNativeContext")
    void setMemberNativeContext(long mNativeContext);

    @Direct
    Surface getSurface();

    @Static
    void postEventFromNative(Object selfRef);
  }

  /** Shadow picker for {@link ImageReader}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowImageReader.class, ShadowNativeImageReader.class);
    }

    @Override
    protected int getMinApiLevel() {
      return P;
    }
  }
}
