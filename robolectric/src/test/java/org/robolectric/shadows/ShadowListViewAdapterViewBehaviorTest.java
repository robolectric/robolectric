package org.robolectric.shadows;

import android.widget.AdapterView;
import android.widget.ListView;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowListViewAdapterViewBehaviorTest extends AdapterViewBehavior {
  @Override public AdapterView createAdapterView() {
    return new ListView(RuntimeEnvironment.application);
  }
}
