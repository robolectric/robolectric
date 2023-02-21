package org.robolectric.integrationtests.jacoco.onthefly;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(
    shadows = {
      ShadowJaCoCoOnTheFlyTester.class,
    })
@RunWith(RobolectricTestRunner.class)
public class ShadowJaCoCoOnTheFlyTesterTest {
  @Test
  public void testGetValue() {
    Assert.assertNotEquals(JaCoCoOnTheFlyTester.VALUE, ShadowJaCoCoOnTheFlyTester.VALUE);
    Assert.assertEquals(ShadowJaCoCoOnTheFlyTester.VALUE, new JaCoCoOnTheFlyTester().getValue());
  }
}
