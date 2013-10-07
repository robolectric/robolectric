package org.robolectric.shadows;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.PopupWindow;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.obtain;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(Enclosed.class)
public class PopupWindowTest {

  @RunWith(TestRunners.WithDefaults.class)
  public static class WithoutContentView {

    private PopupWindow popupWindow;

    @Before
    public void beforeTests() {
      new Animation() {};
      popupWindow = new PopupWindow();
    }

    @Test
    public void testSetContentView() {
      View contentView = new View(Robolectric.application);
      popupWindow.setContentView(contentView);

      assertThat(popupWindow.getContentView()).isSameAs(contentView);
    }

    @Test
    public void testSetWidth() {
      popupWindow.setWidth(1);

      assertThat(popupWindow.getWidth()).isEqualTo(1);
    }

    @Test
    public void testSetHeight() {
      popupWindow.setHeight(2);

      assertThat(popupWindow.getHeight()).isEqualTo(2);
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

    @SuppressWarnings("RedundantCast") //For some reason this is needed because of a compile error without it
    @Test
    public void testBackgroundDrawable() {
      BitmapDrawable bitmapDrawable = new BitmapDrawable();
      popupWindow.setBackgroundDrawable(bitmapDrawable);

      assertThat((BitmapDrawable) popupWindow.getBackground()).isSameAs(bitmapDrawable);
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

  @RunWith(TestRunners.WithDefaults.class)
  public static class WithContentView {

    private WindowManager windowManager;
    private View contentView;
    private View anchor;
    private ShadowWindowManagerImpl shadowWindowManager;

    @Before
    public void setUp() throws Exception {
      windowManager = (WindowManager) Robolectric.application.getSystemService(Context.WINDOW_SERVICE);
      contentView = new View(Robolectric.application);
      contentView.setId(R.id.content_view);
      anchor = new View(Robolectric.application);

      shadowWindowManager = (ShadowWindowManagerImpl) shadowOf(windowManager);
    }

    @Test
    public void testShowing() {
      PopupWindow popupWindow = new PopupWindow(contentView, 0, 0, true);
      popupWindow.showAsDropDown(anchor);

      assertTrue(popupWindow.isShowing());
    }

    @Test
    public void testDismiss() {
      PopupWindow popupWindow = new PopupWindow(contentView, 0, 0, true);
      popupWindow.showAsDropDown(anchor);

      assertTrue(popupWindow.isShowing());

      popupWindow.dismiss();

      assertFalse(popupWindow.isShowing());
    }

    @Test
    public void showAsDropDown_sticksWindowIntoWindowManager() throws Exception {
      PopupWindow popupWindow = new PopupWindow(contentView, 0, 0, true);
      popupWindow.showAsDropDown(anchor);
      assertNotNull(shadowWindowManager.getViews().get(0).findViewById(R.id.content_view));
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
      assertNotNull(shadowWindowManager.getViews().get(0).findViewById(R.id.content_view));
    }

    @Test
    public void backgroundDrawableIsBehindPopupViewContainer() throws Exception {
      PopupWindow popupWindow = new PopupWindow(contentView);
      BitmapDrawable background = new BitmapDrawable();
      popupWindow.setBackgroundDrawable(background);
      popupWindow.showAsDropDown(anchor);
      assertSame(background, shadowWindowManager.getViews().get(0).getBackground());
    }

    @Test
    public void dismiss_removesContainerFromWindowManager() throws Exception {
      PopupWindow popupWindow = new PopupWindow(contentView);
      popupWindow.showAsDropDown(anchor);
      popupWindow.dismiss();
      assertEquals(0, shadowWindowManager.getViews().size());
    }
  }
}
