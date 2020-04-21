package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.view.View;
import android.widget.TabWidget;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TabWidget.class)
public class ShadowTabWidget extends ShadowLinearLayout {

  @HiddenApi @Implementation(maxSdk = M)
  public void initTabWidget() {
  }

  @Implementation
  public View getChildTabViewAt(int index) {
    return realViewGroup.getChildAt(index);
  }
}
