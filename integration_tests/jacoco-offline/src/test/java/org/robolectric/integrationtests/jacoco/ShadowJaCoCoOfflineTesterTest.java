package org.robolectric.integrationtests.jacoco;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(
    shadows = {
      ShadowJaCoCoOfflineTester.class,
    })
@RunWith(RobolectricTestRunner.class)
public class ShadowJaCoCoOfflineTesterTest {
  @Test
  public void testGetValue() {
    Assert.assertNotEquals(JaCoCoOfflineTester.VALUE, ShadowJaCoCoOfflineTester.VALUE);
    Assert.assertEquals(new JaCoCoOfflineTester().getValue(), ShadowJaCoCoOfflineTester.VALUE);
  }
}
