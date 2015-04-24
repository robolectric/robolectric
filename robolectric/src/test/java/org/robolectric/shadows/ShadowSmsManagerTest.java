package org.robolectric.shadows;

import android.telephony.SmsManager;
import com.google.android.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowSmsManagerTest {
  private SmsManager smsManager = SmsManager.getDefault();
  private final String testScAddress = "testServiceCenterAddress";
  private final String testDestinationAddress = "destinationAddress";

  @Test
  public void sendTextMessage_shouldStoreLastSentTextParameters() {
    smsManager.sendTextMessage(testDestinationAddress, testScAddress, "Body Text", null, null);
    ShadowSmsManager.TextSmsParams lastParams = shadowOf(smsManager).getLastSentTextMessageParams();

    assertThat(lastParams.getDestinationAddress()).isEqualTo(testDestinationAddress);
    assertThat(lastParams.getScAddress()).isEqualTo(testScAddress);
    assertThat(lastParams.getText()).isEqualTo("Body Text");
    assertThat(lastParams.getSentIntent()).isNull();
    assertThat(lastParams.getDeliveryIntent()).isNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void sendTextMessage_shouldThrowExceptionWithEmptyDestination() {
    smsManager.sendTextMessage("", testScAddress, "testSmsBodyText", null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void sentTextMessage_shouldThrowExceptionWithEmptyText() {
    smsManager.sendTextMessage(testDestinationAddress, testScAddress, "", null, null);
  }

  @Test
  public void sendMultipartMessage_shouldStoreLastSendMultimediaParameters() {
    smsManager.sendMultipartTextMessage(testDestinationAddress, testScAddress, Lists.newArrayList("Foo", "Bar", "Baz"), null, null);
    ShadowSmsManager.TextMultipartParams lastParams = shadowOf(smsManager).getLastSentMultipartTextMessageParams();

    assertThat(lastParams.getDestinationAddress()).isEqualTo(testDestinationAddress);
    assertThat(lastParams.getScAddress()).isEqualTo(testScAddress);
    assertThat(lastParams.getParts()).containsExactly("Foo", "Bar", "Baz");
    assertThat(lastParams.getSentIntents()).isNull();
    assertThat(lastParams.getDeliveryIntents()).isNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void sendMultipartTextMessage_shouldThrowExceptionWithEmptyDestination() {
    smsManager.sendMultipartTextMessage("", testScAddress, Lists.newArrayList("Foo"), null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void sentMultipartTextMessage_shouldThrowExceptionWithEmptyText() {
    smsManager.sendMultipartTextMessage(testDestinationAddress, testScAddress, null, null, null);
  }

  @Test
  public void clearLastSentTextMessageParams_shouldClearParameters() {
    smsManager.sendTextMessage(testDestinationAddress, testScAddress, "testSmsBodyText", null, null);
    assertThat(shadowOf(smsManager).getLastSentTextMessageParams()).isNotNull();

    shadowOf(smsManager).clearLastSentTextMessageParams();
    assertThat(shadowOf(smsManager).getLastSentTextMessageParams()).isNull();
  }

  @Test
  public void clearLastSentMultipartTextMessageParams_shouldClearParameters() {
    smsManager.sendMultipartTextMessage(testDestinationAddress, testScAddress, Lists.newArrayList("Foo", "Bar", "Baz"), null, null);
    assertThat(shadowOf(smsManager).getLastSentMultipartTextMessageParams()).isNotNull();

    shadowOf(smsManager).clearLastSentMultipartTextMessageParams();
    assertThat(shadowOf(smsManager).getLastSentMultipartTextMessageParams()).isNull();
  }
}
