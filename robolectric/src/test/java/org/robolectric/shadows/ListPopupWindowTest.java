package org.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.widget.ListPopupWindow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ListPopupWindowTest {
  @Test
  public void show_setsLastListPopupWindow() throws Exception {
    Context context = Robolectric.application;
    ListPopupWindow popupWindow = new ListPopupWindow(context);
    assertThat(ShadowListPopupWindow.getLatestListPopupWindow()).isNull();
    popupWindow.setAnchorView(new View(context));
    popupWindow.show();
    assertThat(ShadowListPopupWindow.getLatestListPopupWindow()).isSameAs(popupWindow);
  }
}
