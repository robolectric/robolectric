package org.robolectric.shadows;

import android.content.ContentProvider;
import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowContentProviderTest {
  @Config(sdk = {
      Build.VERSION_CODES.KITKAT,
      Build.VERSION_CODES.LOLLIPOP,
      Build.VERSION_CODES.LOLLIPOP_MR1,
      Build.VERSION_CODES.M,
      // todo: should be from = KITKAT
  })
  @Test public void testSetCallingPackage() throws Exception {
    ContentProvider provider = new ShadowContentResolverTest.TestContentProvider();
    shadowOf(provider).setCallingPackage("calling-package");
    assertThat(provider.getCallingPackage()).isEqualTo("calling-package");
  }
}
