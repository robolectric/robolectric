package org.robolectric.shadows;

import android.app.TabActivity;
import android.widget.TabHost;
import android.widget.TabWidget;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TabActivity.class)
public class ShadowTabActivity extends ShadowActivityGroup {
  @RealObject private TabActivity realTabActivity;
  private TabHost tabhost;

  @Implementation
  protected TabHost getTabHost() {
    if (tabhost==null) {
      tabhost = new TabHost(realTabActivity);
    }
    return tabhost;
  }

  @Implementation
  protected TabWidget getTabWidget() {
    return getTabHost().getTabWidget();
  }
}
