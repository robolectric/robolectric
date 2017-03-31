package org.robolectric.shadows;

import android.widget.TabWidget;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.widget.TabWidget}.
 */
@Implements(TabWidget.class)
public class ShadowTabWidget extends ShadowLinearLayout {

  @HiddenApi @Implementation
  public void initTabWidget() {
  }
}
