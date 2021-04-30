package org.robolectric.shadows;

import android.content.res.Resources;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.annotation.experimental.LazyApplication.LazyLoad;

/** Tests for interactions with shadows when lazily loading application */
@LazyApplication(LazyLoad.ON)
@RunWith(AndroidJUnit4.class)
public class LazyApplicationShadowTest {

  /**
   * Test to make sure that (Shadow)Resources.getSystem can safely be called when lazy loading is
   * turned on
   */
  @Test
  public void testResourcesGetSystem_doesNotCrash_whenLazyLoading() {
    Resources.getSystem();
  }

  @Test
  public void testShadowContentResolverGetProvider_doesNotCrash_whenLazyLoading() {
    ShadowContentResolver.getProvider(Uri.parse("content://my.provider"));
  }
}
