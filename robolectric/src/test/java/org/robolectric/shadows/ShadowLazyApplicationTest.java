package org.robolectric.shadows;

import static org.robolectric.annotation.LazyLoadApplication.LazyLoad.ON;

import android.app.Application;
import android.content.res.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LazyLoadApplication;

/** Tests for logic related to lazy Applications. */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class)
public class ShadowLazyApplicationTest {
  @LazyLoadApplication(ON)
  @Test
  public void resourcesGetSystem_supportedWithLazyApplication() {
    Resources.getSystem();
  }
}
