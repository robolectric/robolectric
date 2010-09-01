package com.xtremelabs.droidsugar.fakes;

import android.view.View;
import android.widget.ZoomButtonsController;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ZoomButtonsController.class)
public class FakeZoomButtonsController {
    private ZoomButtonsController.OnZoomListener listener;

    public void __constructor__(View ownerView) {
    }

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
