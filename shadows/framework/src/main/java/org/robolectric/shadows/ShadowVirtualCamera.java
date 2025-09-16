package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.companion.virtual.camera.VirtualCamera;
import android.companion.virtual.camera.VirtualCameraCallback;
import java.util.Objects;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowVirtualDeviceManager.ShadowVirtualDevice;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link VirtualCamera}. */
@Implements(value = VirtualCamera.class, minSdk = VANILLA_ICE_CREAM, isInAndroidSdk = false)
public class ShadowVirtualCamera {

  @RealObject VirtualCamera realObject;

  @Implementation
  protected void close() {
    reflector(VirtualCameraReflector.class, realObject).close();
    ShadowVirtualDevice.virtualCameras.removeIf(
        virtualCamera -> Objects.equals(virtualCamera.getId(), realObject.getId()));
  }

  /** Returns the callback which was passed into VirtualCameraConfig when the camera was created. */
  public VirtualCameraCallback getVirtualCameraCallback() {
    return reflector(
            VirtualCameraCallbackInternalReflector.class, realObject.getConfig().getCallback())
        .getUnwrappedCallback();
  }

  /** Reflector for VirtualCamera. */
  @ForType(VirtualCamera.class)
  public interface VirtualCameraReflector {
    @Direct
    void close();
  }

  /** Reflector for VirtualCameraCallbackInternal. */
  @ForType(
      className =
          "android.companion.virtual.camera.VirtualCameraConfig$VirtualCameraCallbackInternal")
  public interface VirtualCameraCallbackInternalReflector {
    @Accessor("mCallback")
    VirtualCameraCallback getUnwrappedCallback();
  }
}
