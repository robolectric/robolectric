package org.robolectric.shadows;

import android.telephony.SmsManager;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

@RunWith(TestRunners.WithDefaults.class)
public class SmsManagerTest {

  private SmsManager smsManager;
  private ShadowSmsManager shadowSmsManager;

  private String testDestinationAddress = "destinationAddress";
  private String testScAddress = "testServiceCenterAddress";
  private String testText = "testSmsBodyText";

  @Before
  public void setup() {
    smsManager = SmsManager.getDefault();
    shadowSmsManager = Robolectric.shadowOf(smsManager);
  }

  @After
  public void tearDown() {
    smsManager = null;
    shadowSmsManager = null;
  }

  @Test
  public void shouldHaveShadowSmsManager() {
    Assert.assertNotNull(shadowSmsManager);
  }

  @Test
  public void shouldStoreLastSentTextMessageParameters() {

    smsManager.sendTextMessage(testDestinationAddress, testScAddress, testText, null, null);

    ShadowSmsManager.TextSmsParams lastParams = shadowSmsManager.getLastSentTextMessageParams();

    Assert.assertEquals(testDestinationAddress, lastParams.getDestinationAddress());
    Assert.assertEquals(testScAddress, lastParams.getScAddress());
    Assert.assertEquals(testText, lastParams.getText());
  }

  @Test
  public void shouldClearLastSentTestMessageParameters() {
    smsManager.sendTextMessage(testDestinationAddress, testScAddress, testText, null, null);
    shadowSmsManager.clearLastSentTextMessageParams();
    Assert.assertNull(shadowSmsManager.getLastSentTextMessageParams());
  }

  @Test(expected=IllegalArgumentException.class)
  public void sendTextMessage_shouldThrowExceptionWithEmptyDestination() {
    smsManager.sendTextMessage("", testScAddress, testText, null, null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void sentTextMessage_shouldThrowExceptionWithEmptyText() {
    smsManager.sendTextMessage(testDestinationAddress, testScAddress, "", null, null);
  }
}
