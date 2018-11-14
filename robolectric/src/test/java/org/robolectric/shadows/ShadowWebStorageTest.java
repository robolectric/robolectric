package org.robolectric.shadows;

import android.webkit.WebStorage;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowWebStorage} */
@RunWith(AndroidJUnit4.class)
public final class ShadowWebStorageTest {

  @Test
  public void webStorageDoesNotCrash() throws Exception {
    WebStorage.getInstance().deleteAllData();
  }
}
