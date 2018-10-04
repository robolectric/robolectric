package org.robolectric.shadows;

import android.view.View;
import android.widget.ZoomButtonsController;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ZoomButtonsController.class)
public class ShadowZoomButtonsController {
  private ZoomButtonsController.OnZoomListener listener;

  @Implementation
  protected void __constructor__(View ownerView) {}

  @Implementation
  protected void setOnZoomListener(ZoomButtonsController.OnZoomListener listener) {
    this.listener = listener;
  }

  public void simulateZoomInButtonClick() {
    listener.onZoom(true);
  }

  public void simulateZoomOutButtonClick() {
    listener.onZoom(false);
  }
}
