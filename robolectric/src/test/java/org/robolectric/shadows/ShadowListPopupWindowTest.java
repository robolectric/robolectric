package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import android.view.View;
import android.widget.ListPopupWindow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowListPopupWindowTest {
  @Test
  public void show_setsLastListPopupWindow() throws Exception {
    Context context = RuntimeEnvironment.application;
    ListPopupWindow popupWindow = new ListPopupWindow(context);
    assertThat(ShadowListPopupWindow.getLatestListPopupWindow()).isNull();
    popupWindow.setAnchorView(new View(context));
    popupWindow.show();
    assertThat(ShadowListPopupWindow.getLatestListPopupWindow()).isSameAs(popupWindow);
  }
}
