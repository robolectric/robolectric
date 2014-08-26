package org.robolectric.shadows;

import android.widget.TabWidget;
import org.robolectric.internal.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TabWidget.class)
public class ShadowTabWidget extends ShadowLinearLayout {
  @HiddenApi @Implementation
  public void initTabWidget() {
  }
}
