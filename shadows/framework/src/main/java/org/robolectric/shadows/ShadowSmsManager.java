package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.R;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import androidx.annotation.Nullable;
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

  private String smscAddress;
  private boolean hasSmscAddressPermission = true;
  private static int defaultSmsSubscriptionId = -1;

  @Resetter
  public static void reset() {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP_MR1) {
      Map<String, Object> sSubInstances =
          ReflectionHelpers.getStaticField(SmsManager.class, "sSubInstances");
      sSubInstances.clear();
      defaultSmsSubscriptionId = -1;
    }
  }

  // SMS functionality

  protected TextSmsParams lastTextSmsParams;
  protected TextMultipartParams lastTextMultipartParams;
  protected DataMessageParams lastDataParams;

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

    lastTextSmsParams =
        new TextSmsParams(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
  }

  @Implementation(minSdk = R)
  protected void sendTextMessage(
      String destinationAddress,
      String scAddress,
      String text,
      PendingIntent sentIntent,
      PendingIntent deliveryIntent,
      long messageId) {
    if (TextUtils.isEmpty(destinationAddress)) {
      throw new IllegalArgumentException("Invalid destinationAddress");
    }

    if (TextUtils.isEmpty(text)) {
      throw new IllegalArgumentException("Invalid message body");
    }

    lastTextSmsParams =
        new TextSmsParams(
            destinationAddress, scAddress, text, sentIntent, deliveryIntent, messageId);
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

  /** @return Parameters for last call to {@link #sendDataMessage}. */
  public DataMessageParams getLastSentDataMessageParams() {
    return lastDataParams;
  }

  /** Clear last recorded parameters for {@link #sendDataMessage}. */
  public void clearLastSentDataMessageParams() {
    lastDataParams = null;
  }

  /** @return Parameters for last call to {@link #sendTextMessage}. */
  public TextSmsParams getLastSentTextMessageParams() {
    return lastTextSmsParams;
  }

  /** Clear last recorded parameters for {@link #sendTextMessage}. */
  public void clearLastSentTextMessageParams() {
    lastTextSmsParams = null;
  }

  /** @return Parameters for last call to {@link #sendMultipartTextMessage}. */
  public TextMultipartParams getLastSentMultipartTextMessageParams() {
    return lastTextMultipartParams;
  }

  /** Clear last recorded parameters for {@link #sendMultipartTextMessage}. */
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
    private final long messageId;

    public TextSmsParams(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent) {
      this(destinationAddress, scAddress, text, sentIntent, deliveryIntent, 0L);
    }

    public TextSmsParams(
        String destinationAddress,
        String scAddress,
        String text,
        PendingIntent sentIntent,
        PendingIntent deliveryIntent,
        long messageId) {
      this.destinationAddress = destinationAddress;
      this.scAddress = scAddress;
      this.text = text;
      this.sentIntent = sentIntent;
      this.deliveryIntent = deliveryIntent;
      this.messageId = messageId;
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

    public long getMessageId() {
      return messageId;
    }
  }

  public static class TextMultipartParams {
    private final String destinationAddress;
    private final String scAddress;
    private final List<String> parts;
    private final List<PendingIntent> sentIntents;
    private final List<PendingIntent> deliveryIntents;
    private final long messageId;

    public TextMultipartParams(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
      this(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, 0L);
    }

    public TextMultipartParams(
        String destinationAddress,
        String scAddress,
        List<String> parts,
        List<PendingIntent> sentIntents,
        List<PendingIntent> deliveryIntents,
        long messageId) {
      this.destinationAddress = destinationAddress;
      this.scAddress = scAddress;
      this.parts = parts;
      this.sentIntents = sentIntents;
      this.deliveryIntents = deliveryIntents;
      this.messageId = messageId;
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

    public long getMessageId() {
      return messageId;
    }
  }

  // MMS functionality

  private SendMultimediaMessageParams lastSentMultimediaMessageParams;
  private DownloadMultimediaMessageParams lastDownloadedMultimediaMessageParams;

  @Implementation(minSdk = LOLLIPOP)
  protected void sendMultimediaMessage(
      Context context,
      Uri contentUri,
      @Nullable String locationUrl,
      @Nullable Bundle configOverrides,
      @Nullable PendingIntent sentIntent) {
    if (contentUri == null || TextUtils.isEmpty(contentUri.getHost())) {
      throw new IllegalArgumentException("Invalid contentUri");
    }

    lastSentMultimediaMessageParams =
        new SendMultimediaMessageParams(contentUri, locationUrl, configOverrides, sentIntent);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void downloadMultimediaMessage(
      Context context,
      String locationUrl,
      Uri contentUri,
      @Nullable Bundle configOverrides,
      @Nullable PendingIntent sentIntent) {
    if (contentUri == null || TextUtils.isEmpty(contentUri.getHost())) {
      throw new IllegalArgumentException("Invalid contentUri");
    }

    if (TextUtils.isEmpty(locationUrl)) {
      throw new IllegalArgumentException("Invalid locationUrl");
    }

    lastDownloadedMultimediaMessageParams =
        new DownloadMultimediaMessageParams(contentUri, locationUrl, configOverrides, sentIntent);
  }

  /** @return Parameters for last call to {@link #sendMultimediaMessage}. */
  public SendMultimediaMessageParams getLastSentMultimediaMessageParams() {
    return lastSentMultimediaMessageParams;
  }

  /** Clear last recorded parameters for {@link #sendMultimediaMessage}. */
  public void clearLastSentMultimediaMessageParams() {
    lastSentMultimediaMessageParams = null;
  }

  /** @return Parameters for last call to {@link #downloadMultimediaMessage}. */
  public DownloadMultimediaMessageParams getLastDownloadedMultimediaMessageParams() {
    return lastDownloadedMultimediaMessageParams;
  }

  /** Clear last recorded parameters for {@link #downloadMultimediaMessage}. */
  public void clearLastDownloadedMultimediaMessageParams() {
    lastDownloadedMultimediaMessageParams = null;
  }

  @Implementation(minSdk = R)
  protected void sendMultipartTextMessage(
      String destinationAddress,
      String scAddress,
      List<String> parts,
      List<PendingIntent> sentIntents,
      List<PendingIntent> deliveryIntents,
      long messageId) {
    if (TextUtils.isEmpty(destinationAddress)) {
      throw new IllegalArgumentException("Invalid destinationAddress");
    }

    if (parts == null) {
      throw new IllegalArgumentException("Invalid message parts");
    }

    lastTextMultipartParams =
        new TextMultipartParams(
            destinationAddress, scAddress, parts, sentIntents, deliveryIntents, messageId);
  }

  /**
   * Base class for testable parameters from calls to either {@link #downloadMultimediaMessage} or
   * {@link #downloadMultimediaMessage}.
   */
  public abstract static class MultimediaMessageParams {
    private final Uri contentUri;
    protected final String locationUrl;
    @Nullable private final Bundle configOverrides;
    @Nullable protected final PendingIntent pendingIntent;

    protected MultimediaMessageParams(
        Uri contentUri,
        String locationUrl,
        @Nullable Bundle configOverrides,
        @Nullable PendingIntent pendingIntent) {
      this.contentUri = contentUri;
      this.locationUrl = locationUrl;
      this.configOverrides = configOverrides;
      this.pendingIntent = pendingIntent;
    }

    public Uri getContentUri() {
      return contentUri;
    }

    @Nullable
    public Bundle getConfigOverrides() {
      return configOverrides;
    }
  }

  /** Testable parameters from calls to {@link #sendMultimediaMessage}. */
  public static final class SendMultimediaMessageParams extends MultimediaMessageParams {
    protected SendMultimediaMessageParams(
        Uri contentUri,
        @Nullable String locationUrl,
        @Nullable Bundle configOverrides,
        @Nullable PendingIntent pendingIntent) {
      super(contentUri, locationUrl, configOverrides, pendingIntent);
    }

    @Nullable
    public String getLocationUrl() {
      return locationUrl;
    }

    @Nullable
    public PendingIntent getSentIntent() {
      return pendingIntent;
    }
  }

  /** Testable parameters from calls to {@link #downloadMultimediaMessage}. */
  public static final class DownloadMultimediaMessageParams extends MultimediaMessageParams {
    protected DownloadMultimediaMessageParams(
        Uri contentUri,
        String locationUrl,
        @Nullable Bundle configOverrides,
        @Nullable PendingIntent pendingIntent) {
      super(contentUri, locationUrl, configOverrides, pendingIntent);
    }

    public String getLocationUrl() {
      return locationUrl;
    }

    @Nullable
    public PendingIntent getDownloadedIntent() {
      return pendingIntent;
    }
  }

  /**
   * Sets a boolean value to simulate whether or not the required permissions to call {@link
   * #getSmscAddress()} have been granted.
   */
  public void setSmscAddressPermission(boolean smscAddressPermission) {
    this.hasSmscAddressPermission = smscAddressPermission;
  }

  /**
   * Returns {@code null} by default or the value specified via {@link #setSmscAddress(String)}.
   * Required permission is set by {@link #setSmscAddressPermission(boolean)}.
   */
  @Implementation(minSdk = R)
  protected String getSmscAddress() {
    if (!hasSmscAddressPermission) {
      throw new SecurityException();
    }
    return smscAddress;
  }

  /** Sets the value to be returned by {@link #getDefaultSmsSubscriptionId()}. */
  public static void setDefaultSmsSubscriptionId(int id) {
    defaultSmsSubscriptionId = id;
  }

  /**
   * Returns {@code -1} by default or the value specified in {@link
   * #setDefaultSmsSubscriptionId(int)}.
   */
  @Implementation(minSdk = R)
  protected static int getDefaultSmsSubscriptionId() {
    return defaultSmsSubscriptionId;
  }

  /** Sets the value returned by {@link SmsManager#getSmscAddress()}. */
  public void setSmscAddress(String smscAddress) {
    this.smscAddress = smscAddress;
  }
}
