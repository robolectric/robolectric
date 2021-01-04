package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RobolectricTestRunnerThreadTest {
  private static Thread sThread;
  private static ClassLoader sClassLoader;

  @BeforeClass
  public static void beforeClass() throws Exception {
    sThread = Thread.currentThread();
    sClassLoader = Thread.currentThread().getContextClassLoader();
  }

  @Before
  public void setUp() throws Exception {
    assertThat(Thread.currentThread() == sThread).isTrue();
    assertThat(Thread.currentThread().getContextClassLoader() == sClassLoader).isTrue();
  }

  @After
  public void tearDown() throws Exception {
    assertThat(Thread.currentThread() == sThread).isTrue();
    assertThat(Thread.currentThread().getContextClassLoader() == sClassLoader).isTrue();
  }

  @Test
  public void firstTest() {
    assertThat(Thread.currentThread() == sThread).isTrue();
    assertThat(Thread.currentThread().getContextClassLoader() == sClassLoader).isTrue();
  }

  @Test
  public void secondTest() {
    assertThat(Thread.currentThread() == sThread).isTrue();
    assertThat(Thread.currentThread().getContextClassLoader() == sClassLoader).isTrue();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    assertThat(Thread.currentThread() == sThread).isTrue();
    assertThat(Thread.currentThread().getContextClassLoader() == sClassLoader).isTrue();
  }
}
