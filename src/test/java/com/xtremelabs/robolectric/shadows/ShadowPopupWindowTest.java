package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.view.TestWindowManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowPopupWindowTest {

    private TestWindowManager windowManager;
    private View contentView;
    private View anchor;

    @Before
    public void setUp() throws Exception {
        windowManager = (TestWindowManager) Robolectric.application.getSystemService(Context.WINDOW_SERVICE);
        contentView = new View(Robolectric.application);
        contentView.setId(R.id.content_view);
        anchor = new View(Robolectric.application);
    }

    @Test
    public void showAsDropDown_sticksWindowIntoWindowManager() throws Exception {
        PopupWindow popupWindow = new PopupWindow(contentView, 0, 0, true);
        popupWindow.showAsDropDown(anchor);
        assertNotNull(windowManager.getViews().get(0).findViewById(R.id.content_view));
    }

    @Test
    public void supportsViewConstructor() throws Exception {
        PopupWindow popupWindow = new PopupWindow(contentView);
        popupWindow.showAsDropDown(anchor);
        assertNotNull(windowManager.getViews().get(0).findViewById(R.id.content_view));
    }
}