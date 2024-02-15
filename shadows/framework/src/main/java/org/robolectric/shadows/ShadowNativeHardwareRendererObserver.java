package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.graphics.HardwareRendererObserver;
import java.lang.ref.WeakReference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.HardwareRendererObserverNatives;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowNativeHardwareRendererObserver.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link HardwareRendererObserver} that is backed by native code */
@Implements(
    value = HardwareRendererObserver.class,
    minSdk = R,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeHardwareRendererObserver {

  public HardwareRendererObserverNatives hardwareRendererObserverNatives =
      new HardwareRendererObserverNatives();

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nGetNextBuffer(long nativePtr, long[] data) {
    return HardwareRendererObserverNatives.nGetNextBuffer(nativePtr, data);
  }

  @Implementation(minSdk = R, maxSdk = R)
  protected long nCreateObserver() {
    return nCreateObserver(false);
  }

  @Implementation(minSdk = S, maxSdk = S_V2)
  protected long nCreateObserver(boolean waitForPresentTime) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return hardwareRendererObserverNatives.nCreateObserver(waitForPresentTime);
  }

  @Implementation(minSdk = TIRAMISU, maxSdk = U.SDK_INT)
  protected static long nCreateObserver(
      WeakReference<HardwareRendererObserver> observer, boolean waitForPresentTime) {
    HardwareRendererObserver hardwareRendererObserver = observer.get();
    ShadowNativeHardwareRendererObserver shadowNativeHardwareRendererObserver =
        Shadow.extract(hardwareRendererObserver);
    return shadowNativeHardwareRendererObserver.hardwareRendererObserverNatives.nCreateObserver(
        waitForPresentTime);
  }

  /** Shadow picker for {@link HardwareRendererObserver}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeHardwareRendererObserver.class);
    }
  }
}
