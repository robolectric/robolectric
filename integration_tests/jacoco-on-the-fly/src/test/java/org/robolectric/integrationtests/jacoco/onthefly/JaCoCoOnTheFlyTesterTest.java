package org.robolectric.integrationtests.jacoco.onthefly;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class JaCoCoOnTheFlyTesterTest {
  @Test
  public void testGetValueBeforeShadow() {
    Assert.assertEquals(JaCoCoOnTheFlyTester.VALUE, new JaCoCoOnTheFlyTester().getValue());
  }
}
