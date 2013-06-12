package org.robolectric.shadows;


import android.widget.AdapterView;
import android.widget.Gallery;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

@RunWith(TestRunners.WithDefaults.class)
public class AbsSpinnerAdapterViewBehaviorTest extends AdapterViewBehavior {
  @Override public AdapterView createAdapterView() {
    return new Gallery(Robolectric.application);
  }
}
