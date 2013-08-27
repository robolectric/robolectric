package org.robolectric.shadows;

import android.app.TabActivity;
import android.widget.TabHost;
import android.widget.TabWidget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class TabActivityTest {

  @Test
  public void tabActivityShouldNotMakeNewTabHostEveryGet() throws Exception {
    TabActivity activity = Robolectric.buildActivity(TabActivity.class).create().get();
    TabHost tabHost1 = activity.getTabHost();
    TabHost tabHost2 = activity.getTabHost();

    assertThat(tabHost1).isEqualTo(tabHost2);
  }

  @Test
  public void shouldGetTabWidget() throws Exception {
    TabActivity activity = Robolectric.buildActivity(TabActivity.class).create().get();
    activity.setContentView(R.layout.tab_activity);
    assertThat(activity.getTabWidget()).isInstanceOf(TabWidget.class);
  }
}
