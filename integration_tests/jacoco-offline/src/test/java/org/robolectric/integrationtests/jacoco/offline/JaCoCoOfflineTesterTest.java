package org.robolectric.integrationtests.jacoco.offline;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class JaCoCoOfflineTesterTest {
  @Test
  public void testGetValueBeforeShadow() {
    Assert.assertEquals(JaCoCoOfflineTester.VALUE, new JaCoCoOfflineTester().getValue());
  }
}
