package org.robolectric.shadows;

import android.webkit.WebStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link ShadowWebStorage} */
@RunWith(RobolectricTestRunner.class)
public final class ShadowWebStorageTest {

  @Test
  public void webStorageDoesNotCrash() throws Exception {
    WebStorage.getInstance().deleteAllData();
  }
}
