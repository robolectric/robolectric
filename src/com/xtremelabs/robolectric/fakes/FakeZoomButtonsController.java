package com.xtremelabs.robolectric.fakes;

import android.view.View;
import android.widget.ZoomButtonsController;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ZoomButtonsController.class)
public class FakeZoomButtonsController {
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
