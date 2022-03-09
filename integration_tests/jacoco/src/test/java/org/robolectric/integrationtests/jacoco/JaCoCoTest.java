package org.robolectric.integrationtests.jacoco;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class JaCoCoTest {
  @Test
  public void test() {
    assertEquals(3, Foo.divide(6, 2));
  }
}
