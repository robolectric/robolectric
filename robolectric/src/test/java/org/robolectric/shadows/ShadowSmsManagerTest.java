package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSmsManager.TextMultipartParams;
import org.robolectric.shadows.ShadowSmsManager.TextSmsParams;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR2)
public class ShadowSmsManagerTest {
  private SmsManager smsManager = SmsManager.getDefault();
  private final String scAddress = "serviceCenterAddress";
  private final String destAddress = "destinationAddress";
  private final Uri mmsContentUri = Uri.parse("content://mms/123");
  private final String mmsLocationUrl = "https://somewherefancy.com/myMms";

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
    smsManager.sendMultipartTextMessage(
        destAddress, scAddress, Lists.newArrayList("Foo", "Bar", "Baz"), null, null);
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
    assertThat(params.getSentIntent()).isSameInstanceAs(sentIntent);
    assertThat(params.getDeliveryIntent()).isSameInstanceAs(deliveryIntent);
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
    smsManager.sendMultipartTextMessage(
        destAddress, scAddress, Lists.newArrayList("Foo", "Bar", "Baz"), null, null);
    assertThat(shadowOf(smsManager).getLastSentMultipartTextMessageParams()).isNotNull();

    shadowOf(smsManager).clearLastSentMultipartTextMessageParams();
    assertThat(shadowOf(smsManager).getLastSentMultipartTextMessageParams()).isNull();
  }

  // Tests for {@link SmsManager#sendMultimediaMessage}

  @Test
  @Config(minSdk = LOLLIPOP)
  public void sendMultimediaMessage_shouldStoreLastSentMultimediaMessageParameters() {
    Bundle configOverrides = new Bundle();
    configOverrides.putBoolean("enableMMSDeliveryReports", true);
    PendingIntent sentIntent = ReflectionHelpers.callConstructor(PendingIntent.class);

    smsManager.sendMultimediaMessage(
        null, mmsContentUri, mmsLocationUrl, configOverrides, sentIntent);
    ShadowSmsManager.SendMultimediaMessageParams params =
        shadowOf(smsManager).getLastSentMultimediaMessageParams();

    assertThat(params.getContentUri()).isEqualTo(mmsContentUri);
    assertThat(params.getLocationUrl()).isEqualTo(mmsLocationUrl);
    assertThat(params.getConfigOverrides()).isSameInstanceAs(configOverrides);
    assertThat(params.getSentIntent()).isSameInstanceAs(sentIntent);
  }

  @Test(expected = IllegalArgumentException.class)
  @Config(minSdk = LOLLIPOP)
  public void sendMultimediaMessage_shouldThrowExceptionWithEmptyContentUri() {
    smsManager.sendMultimediaMessage(
        null,
        null,
        mmsLocationUrl,
        new Bundle(),
        ReflectionHelpers.callConstructor(PendingIntent.class));
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void clearLastSentMultimediaMessageParams_shouldClearParameters() {
    smsManager.sendMultimediaMessage(
        null,
        mmsContentUri,
        mmsLocationUrl,
        new Bundle(),
        ReflectionHelpers.callConstructor(PendingIntent.class));
    assertThat(shadowOf(smsManager).getLastSentMultimediaMessageParams()).isNotNull();

    shadowOf(smsManager).clearLastSentMultimediaMessageParams();
    assertThat(shadowOf(smsManager).getLastSentMultimediaMessageParams()).isNull();
  }

  // Tests for {@link SmsManager#downloadMultimediaMessage}

  @Test
  @Config(minSdk = LOLLIPOP)
  public void downloadMultimediaMessage_shouldStoreLastDownloadedMultimediaMessageParameters() {
    Bundle configOverrides = new Bundle();
    configOverrides.putBoolean("enableMMSDeliveryReports", true);
    PendingIntent downloadedIntent = ReflectionHelpers.callConstructor(PendingIntent.class);

    smsManager.downloadMultimediaMessage(
        null, mmsLocationUrl, mmsContentUri, configOverrides, downloadedIntent);
    ShadowSmsManager.DownloadMultimediaMessageParams params =
        shadowOf(smsManager).getLastDownloadedMultimediaMessageParams();

    assertThat(params.getContentUri()).isEqualTo(mmsContentUri);
    assertThat(params.getLocationUrl()).isEqualTo(mmsLocationUrl);
    assertThat(params.getConfigOverrides()).isSameInstanceAs(configOverrides);
    assertThat(params.getDownloadedIntent()).isSameInstanceAs(downloadedIntent);
  }

  @Test(expected = IllegalArgumentException.class)
  @Config(minSdk = LOLLIPOP)
  public void downloadMultimediaMessage_shouldThrowExceptionWithEmptyLocationUrl() {
    smsManager.downloadMultimediaMessage(
        null,
        null,
        mmsContentUri,
        new Bundle(),
        ReflectionHelpers.callConstructor(PendingIntent.class));
  }

  @Test(expected = IllegalArgumentException.class)
  @Config(minSdk = LOLLIPOP)
  public void downloadMultimediaMessage_shouldThrowExceptionWithEmptyContentUri() {
    smsManager.downloadMultimediaMessage(
        null,
        mmsLocationUrl,
        null,
        new Bundle(),
        ReflectionHelpers.callConstructor(PendingIntent.class));
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void clearLastDownloadedMultimediaMessageParams_shouldClearParameters() {
    smsManager.downloadMultimediaMessage(
        null,
        mmsLocationUrl,
        mmsContentUri,
        new Bundle(),
        ReflectionHelpers.callConstructor(PendingIntent.class));
    assertThat(shadowOf(smsManager).getLastDownloadedMultimediaMessageParams()).isNotNull();

    shadowOf(smsManager).clearLastDownloadedMultimediaMessageParams();
    assertThat(shadowOf(smsManager).getLastDownloadedMultimediaMessageParams()).isNull();
  }

  @Test
  @Config(minSdk = R)
  public void sendTextMessage_withMessageId_shouldStoreLastSentTextParameters() {
    smsManager.sendTextMessage(destAddress, scAddress, "Body Text", null, null, 1231L);
    TextSmsParams params = shadowOf(smsManager).getLastSentTextMessageParams();

    assertThat(params.getDestinationAddress()).isEqualTo(destAddress);
    assertThat(params.getScAddress()).isEqualTo(scAddress);
    assertThat(params.getText()).isEqualTo("Body Text");
    assertThat(params.getSentIntent()).isNull();
    assertThat(params.getMessageId()).isEqualTo(1231L);
  }

  @Test
  @Config(minSdk = R)
  public void sendMultipartMessage_withMessageId_shouldStoreLastSendMultimediaParameters() {
    smsManager.sendMultipartTextMessage(
        destAddress, scAddress, Lists.newArrayList("Foo", "Bar", "Baz"), null, null, 12312L);
    TextMultipartParams params = shadowOf(smsManager).getLastSentMultipartTextMessageParams();

    assertThat(params.getDestinationAddress()).isEqualTo(destAddress);
    assertThat(params.getScAddress()).isEqualTo(scAddress);
    assertThat(params.getParts()).containsExactly("Foo", "Bar", "Baz");
    assertThat(params.getSentIntents()).isNull();
    assertThat(params.getDeliveryIntents()).isNull();
    assertThat(params.getMessageId()).isEqualTo(12312L);
  }

  @Test
  @Config(minSdk = R)
  public void shouldGiveSmscAddress() {
    shadowOf(smsManager).setSmscAddress("123-244-2222");
    assertThat(smsManager.getSmscAddress()).isEqualTo("123-244-2222");
  }

  @Test
  @Config(minSdk = R)
  public void getSmscAddress_shouldThrowSecurityExceptionWhenReadPhoneStatePermissionNotGranted()
      throws Exception {
    shadowOf(smsManager).setSmscAddressPermission(false);
    assertThrows(SecurityException.class, () -> smsManager.getSmscAddress());
  }

  @Test
  @Config(minSdk = R)
  public void shouldGiveDefaultSmsSubscriptionId() {
    ShadowSmsManager.setDefaultSmsSubscriptionId(3);
    assertThat(ShadowSmsManager.getDefaultSmsSubscriptionId()).isEqualTo(3);
  }
}
