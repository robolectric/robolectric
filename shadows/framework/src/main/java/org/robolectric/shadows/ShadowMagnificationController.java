package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.accessibilityservice.AccessibilityService.MagnificationController;
import android.graphics.Region;
import android.os.Handler;
import android.os.Looper;
import java.util.HashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow of MagnificationController. */
@Implements(value = MagnificationController.class, minSdk = N)
public class ShadowMagnificationController {

  private static final float DEFAULT_CENTER_X = 0.0f;
  private static final float DEFAULT_CENTER_Y = 0.0f;
  private static final float DEFAULT_SCALE = 1.0f;

  @RealObject private MagnificationController realObject;

  private final HashMap<MagnificationController.OnMagnificationChangedListener, Handler> listeners =
      new HashMap<>();

  private final Region magnificationRegion = new Region();
  private float centerX = DEFAULT_CENTER_X;
  private float centerY = DEFAULT_CENTER_Y;
  private float scale = DEFAULT_SCALE;

  @Implementation
  protected void addListener(
      MagnificationController.OnMagnificationChangedListener listener, Handler handler) {
    listeners.put(listener, handler);
  }

  @Implementation
  protected void addListener(MagnificationController.OnMagnificationChangedListener listener) {
    addListener(listener, new Handler(Looper.getMainLooper()));
  }

  @Implementation
  protected float getCenterX() {
    return centerX;
  }

  @Implementation
  protected float getCenterY() {
    return centerY;
  }

  @Implementation
  protected Region getMagnificationRegion() {
    return magnificationRegion;
  }

  @Implementation
  protected float getScale() {
    return scale;
  }

  @Implementation
  protected boolean removeListener(
      MagnificationController.OnMagnificationChangedListener listener) {
    if (!listeners.containsKey(listener)) {
      return false;
    }
    listeners.remove(listener);
    return true;
  }

  @Implementation
  protected boolean reset(boolean animate) {
    centerX = DEFAULT_CENTER_X;
    centerY = DEFAULT_CENTER_Y;
    scale = DEFAULT_SCALE;
    notifyListeners();
    return true;
  }

  @Implementation
  protected boolean setCenter(float centerX, float centerY, boolean animate) {
    this.centerX = centerX;
    this.centerY = centerY;
    notifyListeners();
    return true;
  }

  @Implementation
  protected boolean setScale(float scale, boolean animate) {
    this.scale = scale;
    notifyListeners();
    return true;
  }

  private void notifyListeners() {
    for (MagnificationController.OnMagnificationChangedListener listener : listeners.keySet()) {
      Handler handler = listeners.get(listener);
      handler.post(
          () ->
              listener.onMagnificationChanged(
                  realObject, magnificationRegion, scale, centerX, centerY));
    }
  }
}
