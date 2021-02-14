package org.robolectric.lazyloadenabled;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LazyLoadApplication.LazyLoad.OFF;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LazyLoadApplication;

/** Test case to make sure the application is lazily loaded when requested at the package level */
@RunWith(AndroidJUnit4.class)
public class LazyApplicationPackageTest {
  @Test
  public void testLazyLoad() {
    assertThat(RuntimeEnvironment.application).isNull();
    assertThat(RuntimeEnvironment.getApplication()).isNotNull();
    assertThat(RuntimeEnvironment.application).isNotNull();
  }

  @LazyLoadApplication(OFF)
  @Test
  public void testMethodLevelOverride() {
    assertThat(RuntimeEnvironment.application).isNotNull();
  }
}
