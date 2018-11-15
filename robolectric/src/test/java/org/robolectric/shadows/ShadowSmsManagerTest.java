package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.PendingIntent;
import android.telephony.SmsManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR2)
public class ShadowSmsManagerTest {
  private SmsManager smsManager = SmsManager.getDefault();
  private final String scAddress = "serviceCenterAddress";
  private final String destAddress = "destinationAddress";

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void getForSubscriptionId() {
    final int subId = 101;

    smsManager = SmsManager.getSmsManagerForSubscriptionId(subId);
    assertThat(smsManager.getSubscriptionId()).isEqualTo(subId);
  }

  @Test
  public void sendTextMessage_shouldStoreLastSentTextParameters() {
    smsManager.sendTextMessage(destAddress, scAddress, "Body Text", null, null);
    ShadowSmsManager.TextSmsParams params = shadowOf(smsManager).getLastSentTextMessageParams();

    assertThat(params.getDestinationAddress()).isEqualTo(destAddress);
    assertThat(params.getScAddress()).isEqualTo(scAddress);
    assertThat(params.getText()).isEqualTo("Body Text");
    assertThat(params.getSentIntent()).isNull();
    assertThat(params.getDeliveryIntent()).isNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void sendTextMessage_shouldThrowExceptionWithEmptyDestination() {
    smsManager.sendTextMessage("", scAddress, "testSmsBodyText", null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void sentTextMessage_shouldThrowExceptionWithEmptyText() {
    smsManager.sendTextMessage(destAddress, scAddress, "", null, null);
  }

  @Test
  public void sendMultipartMessage_shouldStoreLastSendMultimediaParameters() {
    smsManager.sendMultipartTextMessage(destAddress, scAddress, Lists.newArrayList("Foo", "Bar", "Baz"), null, null);
    ShadowSmsManager.TextMultipartParams params = shadowOf(smsManager).getLastSentMultipartTextMessageParams();

    assertThat(params.getDestinationAddress()).isEqualTo(destAddress);
    assertThat(params.getScAddress()).isEqualTo(scAddress);
    assertThat(params.getParts()).containsExactly("Foo", "Bar", "Baz");
    assertThat(params.getSentIntents()).isNull();
    assertThat(params.getDeliveryIntents()).isNull();
  }

  @Test(expected = IllegalArgumentException.class)
  public void sendMultipartTextMessage_shouldThrowExceptionWithEmptyDestination() {
    smsManager.sendMultipartTextMessage("", scAddress, Lists.newArrayList("Foo"), null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void sentMultipartTextMessage_shouldThrowExceptionWithEmptyText() {
    smsManager.sendMultipartTextMessage(destAddress, scAddress, null, null, null);
  }

  @Test
  public void sendDataMessage_shouldStoreLastParameters() {
    final short destPort = 24;
    final byte[] data = new byte[]{0, 1, 2, 3, 4};
    final PendingIntent sentIntent =
        PendingIntent.getActivity(ApplicationProvider.getApplicationContext(), 10, null, 0);
    final PendingIntent deliveryIntent =
        PendingIntent.getActivity(ApplicationProvider.getApplicationContext(), 10, null, 0);

    smsManager.sendDataMessage(destAddress, scAddress, destPort, data, sentIntent, deliveryIntent);

    final ShadowSmsManager.DataMessageParams params = shadowOf(smsManager).getLastSentDataMessageParams();
    assertThat(params.getDestinationAddress()).isEqualTo(destAddress);
    assertThat(params.getScAddress()).isEqualTo(scAddress);
    assertThat(params.getDestinationPort()).isEqualTo(destPort);
    assertThat(params.getData()).isEqualTo(data);
    assertThat(params.getSentIntent()).isSameAs(sentIntent);
    assertThat(params.getDeliveryIntent()).isSameAs(deliveryIntent);
  }

  @Test(expected = IllegalArgumentException.class)
  public void sendDataMessage_shouldThrowExceptionWithEmptyDestination() {
    smsManager.sendDataMessage("", null, (short) 0, null, null, null);
  }

  @Test
  public void clearLastSentDataMessageParams_shouldClearParameters() {
    smsManager.sendDataMessage(destAddress, scAddress, (short) 0, null, null, null);
    assertThat(shadowOf(smsManager).getLastSentDataMessageParams()).isNotNull();

    shadowOf(smsManager).clearLastSentDataMessageParams();
    assertThat(shadowOf(smsManager).getLastSentDataMessageParams()).isNull();
  }

  @Test
  public void clearLastSentTextMessageParams_shouldClearParameters() {
    smsManager.sendTextMessage(destAddress, scAddress, "testSmsBodyText", null, null);
    assertThat(shadowOf(smsManager).getLastSentTextMessageParams()).isNotNull();

    shadowOf(smsManager).clearLastSentTextMessageParams();
    assertThat(shadowOf(smsManager).getLastSentTextMessageParams()).isNull();
  }

  @Test
  public void clearLastSentMultipartTextMessageParams_shouldClearParameters() {
    smsManager.sendMultipartTextMessage(destAddress, scAddress, Lists.newArrayList("Foo", "Bar", "Baz"), null, null);
    assertThat(shadowOf(smsManager).getLastSentMultipartTextMessageParams()).isNotNull();

    shadowOf(smsManager).clearLastSentMultipartTextMessageParams();
    assertThat(shadowOf(smsManager).getLastSentMultipartTextMessageParams()).isNull();
  }
}
