package org.robolectric.shadows;

import android.content.res.Resources;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LazyLoadApplication;
import org.robolectric.annotation.LazyLoadApplication.LazyLoad;

/** Tests for interactions with shadows when lazily loading application */
@LazyLoadApplication(LazyLoad.ON)
@RunWith(AndroidJUnit4.class)
public class LazyLoadApplicationShadowTest {

  /**
   * Test to make sure that (Shadow)Resources.getSystem can safely be called when lazy loading is
   * turned on
   */
  @Test
  public void testResourcesGetSystem_doesNotCrash_whenLazyLoading() {
    Resources.getSystem();
  }
}
