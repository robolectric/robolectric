package org.robolectric.shadows;

import android.widget.AdapterView;
import android.widget.ListView;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowListViewAdapterViewBehaviorTest extends AdapterViewBehavior {
  @Override public AdapterView createAdapterView() {
    return new ListView(RuntimeEnvironment.application);
  }
}
