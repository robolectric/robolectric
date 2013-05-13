package org.robolectric.shadows;

import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.text.TextUtils;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(SmsManager.class)
public class ShadowSmsManager {

  @RealObject
  private static SmsManager realManager = Robolectric.newInstanceOf(SmsManager.class);

  private TextSmsParams lastTextSmsParams = null;

  @Implementation
  public static SmsManager getDefault() {
    return realManager;
  }

  @Implementation
  public void sendTextMessage(
      String destinationAddress, String scAddress, String text,
      PendingIntent sentIntent, PendingIntent deliveryIntent) {

    if (TextUtils.isEmpty(destinationAddress))
      throw new IllegalArgumentException("Invalid destinationAddress");

    if (TextUtils.isEmpty(text))
      throw new IllegalArgumentException("Invalid message body");

    lastTextSmsParams = new TextSmsParams(
      destinationAddress,
      scAddress,
      text,
      sentIntent,
      deliveryIntent );
  }

  public TextSmsParams getLastSentTextMessageParams() {
    return lastTextSmsParams;
  }

  public void clearLastSentTextMessageParams() {
    lastTextSmsParams = null;
  }

  public class TextSmsParams {
    private String destinationAddress;
    private String scAddress;
    private String text;
    private PendingIntent sentIntent;
    private PendingIntent deliveryIntent;

    public TextSmsParams(
      String destinationAddress, String scAddress, String text,
      PendingIntent sentIntent, PendingIntent deliveryIntent) {
      this.destinationAddress = destinationAddress;
      this.scAddress = scAddress;
      this.text = text;
      this.sentIntent = sentIntent;
      this.deliveryIntent = deliveryIntent;
    }

    public String getDestinationAddress() {
      return destinationAddress;
    }

    public String getScAddress() {
      return scAddress;
    }

    public String getText() {
      return text;
    }

    public PendingIntent getSentIntent() {
      return sentIntent;
    }

    public PendingIntent getDeliveryIntent() {
      return deliveryIntent;
    }
  }
}
