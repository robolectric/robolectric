package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.view.TestWindowManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.obtain;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class PopupWindowTest {

    @RunWith(WithTestDefaultsRunner.class)
    public static class WithoutContentView {

        private PopupWindow popupWindow;

        @Before
        public void beforeTests() {
            popupWindow = new PopupWindow();
        }

        @Test
        public void testSetContentView() {
            View contentView = new View(null);
            popupWindow.setContentView(contentView);

            assertThat(popupWindow.getContentView(), is(contentView));
        }

        @Test
        public void testSetWidth() {
            popupWindow.setWidth(1);

            assertThat(popupWindow.getWidth(), is(1));
        }

        @Test
        public void testSetHeight() {
            popupWindow.setHeight(2);

            assertThat(popupWindow.getHeight(), is(2));
        }

        @Test
        public void testSetFocusable() {
            popupWindow.setFocusable(true);

            assertTrue(popupWindow.isFocusable());
        }

        @Test
        public void testSetTouchable() {
            popupWindow.setTouchable(true);

            assertTrue(popupWindow.isTouchable());
        }

        @Test
        public void testSetOutsideTouchable() {
            popupWindow.setOutsideTouchable(true);

            assertTrue(popupWindow.isOutsideTouchable());
        }

        @Test
        public void testShowing() {
            shadowOf(popupWindow).setShowing(true);

            assertTrue(popupWindow.isShowing());
        }

        @Test
        public void testDismiss() {
            shadowOf(popupWindow).setShowing(true);

            assertTrue(popupWindow.isShowing());

            popupWindow.dismiss();

            assertFalse(popupWindow.isShowing());
        }

        @SuppressWarnings("RedundantCast") //For some reason this is needed because of a compile error without it
        @Test
        public void testBackgroundDrawable() {
            BitmapDrawable bitmapDrawable = new BitmapDrawable();
            popupWindow.setBackgroundDrawable(bitmapDrawable);

            assertThat((BitmapDrawable) popupWindow.getBackground(), is(bitmapDrawable));
        }

        @Test
        public void testTouchInterceptor() {
            popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return event.getAction() == ACTION_DOWN;
                }
            });

            assertTrue(shadowOf(popupWindow).dispatchTouchEvent(obtain(1, 1, ACTION_DOWN, 1f, 1f, 0)));
        }
    }

    @RunWith(WithTestDefaultsRunner.class)
    public static class WithContentView {

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
        public void showAsDropdownWithOffsets_setsOffsetFields() throws Exception {
            PopupWindow popupWindow = new PopupWindow(contentView, 0, 0, true);
            popupWindow.showAsDropDown(anchor, 56, 69);
            assertEquals(shadowOf(popupWindow).getXOffset(), 56);
            assertEquals(shadowOf(popupWindow).getYOffset(), 69);
        }

        @Test
        public void supportsViewConstructor() throws Exception {
            PopupWindow popupWindow = new PopupWindow(contentView);
            popupWindow.showAsDropDown(anchor);
            assertNotNull(windowManager.getViews().get(0).findViewById(R.id.content_view));
        }

        @Test
        public void backgroundDrawableIsBehindPopupViewContainer() throws Exception {
            PopupWindow popupWindow = new PopupWindow(contentView);
            BitmapDrawable background = new BitmapDrawable();
            popupWindow.setBackgroundDrawable(background);
            popupWindow.showAsDropDown(anchor);
            assertSame(background, windowManager.getViews().get(0).getBackground());
        }

        @Test
        public void dismiss_removesContainerFromWindowManager() throws Exception {
            PopupWindow popupWindow = new PopupWindow(contentView);
            popupWindow.showAsDropDown(anchor);
            popupWindow.dismiss();
            assertEquals(0, windowManager.getViews().size());
        }
    }
}
