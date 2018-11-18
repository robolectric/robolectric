package org.robolectric.shadows;

import android.widget.AdapterView;
import android.widget.Gallery;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowAbsSpinnerAdapterViewBehaviorTest extends AdapterViewBehavior {
  @Override public AdapterView createAdapterView() {
    return new Gallery(ApplicationProvider.getApplicationContext());
  }
}
