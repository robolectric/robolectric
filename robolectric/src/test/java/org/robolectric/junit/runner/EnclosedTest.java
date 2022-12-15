package org.robolectric.junit.runner;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(Enclosed.class)
public class EnclosedTest {
  public abstract static class BaseTest {
    protected String foo;
  }

  @RunWith(RobolectricTestRunner.class)
  public static class MyFirstTest extends BaseTest {
    private static final String STRING = "Hello1";

    @Before
    public void setUp() {
      foo = STRING;
    }

    @Test
    public void testStringInitialization() {
      assertThat(foo).isEqualTo(STRING);
    }
  }

  @RunWith(RobolectricTestRunner.class)
  public static class MySecondTest extends BaseTest {
    private static final String STRING = "Hello2";

    @Before
    public void setUp() {
      foo = STRING;
    }

    @Test
    public void testStringInitialization() {
      assertThat(foo).isEqualTo(STRING);
    }
  }
}
