package org.robolectric.integrationtests.jacoco;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class JaCoCoOfflineTesterTest {
  @Test
  public void testGetValueBeforeShadow() {
    Assert.assertEquals(new JaCoCoOfflineTester().getValue(), JaCoCoOfflineTester.VALUE);
  }
}
