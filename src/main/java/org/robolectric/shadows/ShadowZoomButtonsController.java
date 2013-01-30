package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.ZoomButtonsController;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow of {@code ZoomButtonsController} that allows simulated clicking of the zoom button controls to trigger
 * events on the registered listener.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(ZoomButtonsController.class)
public class ShadowZoomButtonsController {
    private ZoomButtonsController.OnZoomListener listener;

    public void __constructor__(View ownerView) {
    }

    @Implementation
    public void setOnZoomListener(ZoomButtonsController.OnZoomListener listener) {
        this.listener = listener;
    }

    public void simulateZoomInButtonClick() {
        listener.onZoom(true);
    }

    public void simulateZoomOutButtonClick() {
        listener.onZoom(false);
    }
}
