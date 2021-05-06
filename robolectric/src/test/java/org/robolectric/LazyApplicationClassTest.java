package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.experimental.LazyApplication.LazyLoad.OFF;
import static org.robolectric.annotation.experimental.LazyApplication.LazyLoad.ON;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.experimental.LazyApplication;

/** Test case to make sure the application is lazily loaded when requested at the class level */
@LazyApplication(ON)
@RunWith(AndroidJUnit4.class)
public class LazyApplicationClassTest {
  @Test
  public void testLazyLoad() {
    assertThat(RuntimeEnvironment.application).isNull();
    assertThat(RuntimeEnvironment.getApplication()).isNotNull();
    assertThat(RuntimeEnvironment.application).isNotNull();
  }

  @LazyApplication(OFF)
  @Test
  public void testMethodLevelOverride() {
    assertThat(RuntimeEnvironment.application).isNotNull();
  }
}
