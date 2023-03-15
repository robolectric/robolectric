package org.robolectric.integrationtests.jacoco;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowJaCoCoTester}. */
@Config(
    shadows = {
      ShadowJaCoCoTester.class,
    })
@RunWith(RobolectricTestRunner.class)
public class ShadowJaCoCoTesterTest {
  @Test
  public void testGetValue() {
    Assert.assertNotEquals(JaCoCoTester.VALUE, ShadowJaCoCoTester.VALUE);
    Assert.assertEquals(ShadowJaCoCoTester.VALUE, new JaCoCoTester().getValue());
  }
}
