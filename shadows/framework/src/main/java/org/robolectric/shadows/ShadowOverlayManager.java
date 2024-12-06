package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.om.OverlayInfo;
import android.content.om.OverlayManager;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Basic shadow implementation for the {@link OverlayManager}.
 *
 * <p>This shadow exists because the overlays managed by the OverlayManager require set up at the
 * Android base image level. This is not something that can exist for host level unit tests.
 *
 * <p>To simulate the image configuration, unit tests must call the {@link addOverlayInfo} function
 * to define the available overlays.
 *
 * <p>This basic shadow only implements the {@link getOverlayInfo} and {@link setEnabled} functions,
 * enabling a basic workflow for enabling or disabling Runtime Resource Overlays (RROs). It enforces
 * the android.permissions.CHANGE_OVERLAY_PACKAGES permission.
 *
 * <p>It does not validate the android.permission.INTERACT_ACROSS_USERS or
 * android.INTERACT_ACROSS_USERS_FULL permissions, which are necessary when changing packages owned
 * by a different user.
 */
@Implements(value = OverlayManager.class, minSdk = UPSIDE_DOWN_CAKE, isInAndroidSdk = false)
public final class ShadowOverlayManager {
  private final Map<String, OverlayInfo> overlaysByPackageName = new HashMap<>();

  @RealObject private OverlayManager realOverlayManager;

  /**
   * Adds or replaces the overlay based on the packageName. Expected to be used by unit tests to
   * setup the expected overlays.
   */
  public void addOverlayInfo(OverlayInfo overlayInfo) {
    overlaysByPackageName.put(overlayInfo.packageName, overlayInfo);
  }

  /**
   * Removes an overlay with the specified packageName if it is present. If not present, nothing
   * happens.
   */
  public void removeOverlayInfo(String packageName) {
    overlaysByPackageName.remove(packageName);
  }

  @Implementation
  @Nullable
  protected OverlayInfo getOverlayInfo(
      @NonNull String packageName, @NonNull UserHandle userHandle) {
    return overlaysByPackageName.get(packageName);
  }

  @Implementation
  protected void setEnabled(@NonNull String packageName, boolean enable, @NonNull UserHandle user) {
    checkPermission();

    OverlayInfo overlay = overlaysByPackageName.get(packageName);
    if (overlay == null) {
      throw new IllegalStateException(
          "setEnabled failed; overlay name " + packageName + " not found");
    }

    if (!overlay.isMutable) {
      throw new IllegalStateException(
          "setEnabled failed; overlay name " + packageName + " is not mutable");
    }

    int state = enable ? OverlayInfo.STATE_ENABLED : OverlayInfo.STATE_DISABLED;

    addOverlayInfo(new OverlayInfo(overlay, state));
  }

  @ForType(OverlayManager.class)
  interface OverlayManagerReflector {
    @Accessor("mContext")
    Context getContext();
  }

  private void checkPermission() {
    Context context = reflector(OverlayManagerReflector.class, realOverlayManager).getContext();
    if (context.checkSelfPermission(android.Manifest.permission.CHANGE_OVERLAY_PACKAGES)
        != PackageManager.PERMISSION_GRANTED) {
      throw new SecurityException(
          "Missing required permission: " + android.Manifest.permission.CHANGE_OVERLAY_PACKAGES);
    }
  }
}
