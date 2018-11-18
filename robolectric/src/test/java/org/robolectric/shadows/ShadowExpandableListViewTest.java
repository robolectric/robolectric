package org.robolectric.shadows;

import android.widget.ExpandableListView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowExpandableListViewTest {

  private ExpandableListView expandableListView;

  @Before
  public void setUp() {
    expandableListView = new ExpandableListView(ApplicationProvider.getApplicationContext());
  }

  @Test
  public void shouldTolerateNullChildClickListener() throws Exception {
    expandableListView.performItemClick(null, 6, -1);
  }
}
