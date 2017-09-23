package org.robolectric.shadows;

import android.widget.AdapterView;
import android.widget.Gallery;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowAbsSpinnerAdapterViewBehaviorTest extends AdapterViewBehavior {
  @Override public AdapterView createAdapterView() {
    return new Gallery(RuntimeEnvironment.application);
  }
}
