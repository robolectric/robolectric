package org.robolectric.lazyloadenabled;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LazyLoadApplication.LazyLoad.OFF;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LazyLoadApplication;

/**
 * Test case to make sure the application is eagerly loaded when lazy is requested at package level
 * but eager loading requested at class level
 */
@LazyLoadApplication(OFF)
@RunWith(AndroidJUnit4.class)
public class LazyApplicationClassOverrideTest {
  @Test
  public void testClassLevelOverride() {
    assertThat(RuntimeEnvironment.application).isNotNull();
  }
}
