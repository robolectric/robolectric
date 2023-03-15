package org.robolectric.integrationtests.jacoco;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link JaCoCoTester}. */
@RunWith(RobolectricTestRunner.class)
public class JaCoCoTesterTest {
  @Test
  public void testGetValueBeforeShadow() {
    Assert.assertEquals(JaCoCoTester.VALUE, new JaCoCoTester().getValue());
  }
}
