package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.PopupWindow;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class PopupWindowTest {

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
        Robolectric.shadowOf(popupWindow).setShowing(true);

        assertTrue(popupWindow.isShowing());
    }

    @Test
    public void testDismiss() {
        Robolectric.shadowOf(popupWindow).setShowing(true);

        assertTrue(popupWindow.isShowing());

        popupWindow.dismiss();

        assertFalse(popupWindow.isShowing());
    }

    @SuppressWarnings("RedundantCast") //For some reason this is needed because of a compile error without it
    @Test
    public void testBackgroundDrawable() {
        BitmapDrawable bitmapDrawable = new BitmapDrawable();
        popupWindow.setBackgroundDrawable(bitmapDrawable);

        assertThat((BitmapDrawable)popupWindow.getBackground(), is(bitmapDrawable));
    }
}
