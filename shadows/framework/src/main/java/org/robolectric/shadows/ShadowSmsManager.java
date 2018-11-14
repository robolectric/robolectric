package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = SmsManager.class, minSdk = JELLY_BEAN_MR2)
public class ShadowSmsManager {

  @Resetter
  public static void reset() {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP_MR1) {
      Map<String, Object> sSubInstances =
          ReflectionHelpers.getStaticField(SmsManager.class, "sSubInstances");
      sSubInstances.clear();
    }
  }

  private TextSmsParams lastTextSmsParams;
  private TextMultipartParams lastTextMultipartParams;
  private DataMessageParams lastDataParams;

  @Implementation
  protected void sendDataMessage(
      String destinationAddress,
      String scAddress,
      short destinationPort,
      byte[] data,
      PendingIntent sentIntent,
      PendingIntent deliveryIntent) {
    if (TextUtils.isEmpty(destinationAddress)) {
      throw new IllegalArgumentException("Invalid destinationAddress");
    }

    lastDataParams = new DataMessageParams(destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent);
  }

  @Implementation
  protected void sendTextMessage(
      String destinationAddress,
      String scAddress,
      String text,
      PendingIntent sentIntent,
      PendingIntent deliveryIntent) {
    if (TextUtils.isEmpty(destinationAddress)) {
      throw new IllegalArgumentException("Invalid destinationAddress");
    }

    if (TextUtils.isEmpty(text)) {
      throw new IllegalArgumentException("Invalid message body");
    }

    lastTextSmsParams = new TextSmsParams(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
  }

  @Implementation
  protected void sendMultipartTextMessage(
      String destinationAddress,
      String scAddress,
      ArrayList<String> parts,
      ArrayList<PendingIntent> sentIntents,
      ArrayList<PendingIntent> deliveryIntents) {
    if (TextUtils.isEmpty(destinationAddress)) {
      throw new IllegalArgumentException("Invalid destinationAddress");
    }

    if (parts == null) {
      throw new IllegalArgumentException("Invalid message parts");
    }

    lastTextMultipartParams = new TextMultipartParams(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
  }

  /**
   * @return Parameters for last call to {@code sendDataMessage}.
   */
  public DataMessageParams getLastSentDataMessageParams() {
    return lastDataParams;
  }

  /**
   * Clear last recorded parameters for {@code sendDataMessage}.
   */
  public void clearLastSentDataMessageParams() {
    lastDataParams = null;
  }

  /**
   * @return Parameters for last call to {@code sendTextMessage}.
   */
  public TextSmsParams getLastSentTextMessageParams() {
    return lastTextSmsParams;
  }

  /**
   * Clear last recorded parameters for {@code sendTextMessage}.
   */
  public void clearLastSentTextMessageParams() {
    lastTextSmsParams = null;
  }

  /**
   * @return Parameters for last call to {@code sendMultipartTextMessage}.
   */
  public TextMultipartParams getLastSentMultipartTextMessageParams() {
    return lastTextMultipartParams;
  }

  /**
   * Clear last recorded parameters for {@code sendMultipartTextMessage}.
   */
  public void clearLastSentMultipartTextMessageParams() {
    lastTextMultipartParams = null;
  }

  public static class DataMessageParams {
    private final String destinationAddress;
    private final String scAddress;
    private final short destinationPort;
    private final byte[] data;
    private final PendingIntent sentIntent;
    private final PendingIntent deliveryIntent;

    public DataMessageParams(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
      this.destinationAddress = destinationAddress;
      this.scAddress = scAddress;
      this.destinationPort = destinationPort;
      this.data = data;
      this.sentIntent = sentIntent;
      this.deliveryIntent = deliveryIntent;
    }

    public String getDestinationAddress() {
      return destinationAddress;
    }

    public String getScAddress() {
      return scAddress;
    }

    public short getDestinationPort() {
      return destinationPort;
    }

    public byte[] getData() {
      return data;
    }

    public PendingIntent getSentIntent() {
      return sentIntent;
    }

    public PendingIntent getDeliveryIntent() {
      return deliveryIntent;
    }
  }

  public static class TextSmsParams {
    private final String destinationAddress;
    private final String scAddress;
    private final String text;
    private final PendingIntent sentIntent;
    private final PendingIntent deliveryIntent;

    public TextSmsParams(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
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

  public static class TextMultipartParams {
    private final String destinationAddress;
    private final String scAddress;
    private final ArrayList<String> parts;
    private final ArrayList<PendingIntent> sentIntents;
    private final ArrayList<PendingIntent> deliveryIntents;

    public TextMultipartParams(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
      this.destinationAddress = destinationAddress;
      this.scAddress = scAddress;
      this.parts = parts;
      this.sentIntents = sentIntents;
      this.deliveryIntents = deliveryIntents;
    }

    public String getDestinationAddress() {
      return destinationAddress;
    }

    public String getScAddress() {
      return scAddress;
    }

    public List<String> getParts() {
      return parts;
    }

    public List<android.app.PendingIntent> getSentIntents() {
      return sentIntents;
    }

    public List<android.app.PendingIntent> getDeliveryIntents() {
      return deliveryIntents;
    }
  }
}
