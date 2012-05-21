package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.view.TestWindowManager;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowPopupWindowTest {

    @Test
    public void showAsDropDown_sticksWindowIntoWindowManager() throws Exception {
        TestWindowManager windowManager = (TestWindowManager) Robolectric.application.getSystemService(Context.WINDOW_SERVICE);
        View contentView = new View(Robolectric.application);
        contentView.setId(R.id.content_view);
        PopupWindow popupWindow = new PopupWindow(contentView, 0, 0, true);
        View anchor = new View(Robolectric.application);
        popupWindow.showAsDropDown(anchor);
        assertNotNull(windowManager.getViews().get(0).findViewById(R.id.content_view));
    }
}