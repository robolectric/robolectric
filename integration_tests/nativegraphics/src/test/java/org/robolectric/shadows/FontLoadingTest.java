package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import androidx.core.content.res.ResourcesCompat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(minSdk = O)
@RunWith(RobolectricTestRunner.class)
public class FontLoadingTest {

  /**
   * There is a potential static initializer cycle that can happen when creating fonts triggers
   * loading RNR. Because statically initializing Typeface is the last step of loading RNR, and the
   * static initializer of Typeface causes a lot of Font objects to be created, there has to be
   * special logic when fonts are loaded to avoid statically initializing Typeface. This is captured
   * in a test here.
   */
  @Test
  public void loadFont_doesNotCauseStaticInitializerCycle() {
    ResourcesCompat.getFont(RuntimeEnvironment.getApplication(), R.font.multiweight_family);
  }
}
