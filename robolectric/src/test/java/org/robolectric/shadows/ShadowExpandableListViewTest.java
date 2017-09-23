package org.robolectric.shadows;

import android.widget.ExpandableListView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowExpandableListViewTest {

  private ExpandableListView expandableListView;

  @Before
  public void setUp() {
    expandableListView = new ExpandableListView(RuntimeEnvironment.application);
  }

  @Test
  public void shouldTolerateNullChildClickListener() throws Exception {
    expandableListView.performItemClick(null, 6, -1);
  }
}
