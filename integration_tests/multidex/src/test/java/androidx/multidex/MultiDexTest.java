package androidx.multidex;

import static androidx.multidex.MultiDex.install;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for androidx.multidex.MultiDex Robolectric. */
@RunWith(AndroidJUnit4.class)
public class MultiDexTest {
  @Test
  public void testIntendedFailEmpty() {
    install(getApplicationContext());
  }
}
