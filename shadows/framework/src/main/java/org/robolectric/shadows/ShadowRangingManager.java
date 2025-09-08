package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.RemoteException;
import android.ranging.IRangingAdapter;
import android.ranging.RangingCapabilities;
import android.ranging.RangingManager;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.Baklava;

/** Adds Robolectric support for UWB ranging. */
@Implements(value = RangingManager.class, minSdk = Baklava.SDK_INT, isInAndroidSdk = false)
public class ShadowRangingManager {

  @RealObject private RangingManager realRangingManager;

  /**
   * Sets the ranging capabilities and trigger any registered
   * RangingCapabilitiesCallback.onRangingCapabilities callbacks.
   */
  public void updateCapabilities(RangingCapabilities rangingCapabilities) throws RemoteException {
    FakeRangingAdapter rangingAdapter =
        (FakeRangingAdapter)
            reflector(RangingManagerReflector.class, realRangingManager).getRangingAdapter();
    rangingAdapter.updateRangingCapabilities(rangingCapabilities);
  }

  @ForType(RangingManager.class)
  private interface RangingManagerReflector {

    @Accessor("mRangingAdapter")
    IRangingAdapter getRangingAdapter();
  }
}
