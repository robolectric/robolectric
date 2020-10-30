package org.robolectric.integrationtests.multidex;

import android.support.multidex.MultiDex;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for MultiDex Robolectric. */
@RunWith(AndroidJUnit4.class)
public class MultiDexTest {

  @Test
  public void testIntendedFailEmpty() {
    MultiDex.install(ApplicationProvider.getApplicationContext());
  }
}
