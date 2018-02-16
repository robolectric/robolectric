package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.ContentProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowContentProviderTest {
  @Config(minSdk = KITKAT)
  @Test public void testSetCallingPackage() throws Exception {
    ContentProvider provider = new ShadowContentResolverTest.TestContentProvider();
    shadowOf(provider).setCallingPackage("calling-package");
    assertThat(provider.getCallingPackage()).isEqualTo("calling-package");
  }
}
