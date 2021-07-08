package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.support.v4.Shadows;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowsTest {

  @Test
  public void testGetProvidedPackageNames() throws Exception {
    String[] actualProvidedPackageNames = new Shadows().getProvidedPackageNames();
    assertThat(Arrays.asList(actualProvidedPackageNames)).containsExactly(
        "android.support.v4.media",
        "androidx.drawerlayout.widget",
        "androidx.loader.content",
        "androidx.localbroadcastmanager.content",
        "androidx.swiperefreshlayout.widget"
    );
  }

}
