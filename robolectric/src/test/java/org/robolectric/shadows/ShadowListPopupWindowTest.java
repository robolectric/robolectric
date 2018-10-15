package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.view.View;
import android.widget.ListPopupWindow;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AndroidJUnit4.class)
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
