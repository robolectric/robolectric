package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.ContentProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestContentProvider1;

@RunWith(AndroidJUnit4.class)
public class ShadowContentProviderTest {
  @Config(minSdk = KITKAT)
  @Test public void testSetCallingPackage() throws Exception {
    ContentProvider provider = new TestContentProvider1();
    shadowOf(provider).setCallingPackage("calling-package");
    assertThat(provider.getCallingPackage()).isEqualTo("calling-package");
  }
}
