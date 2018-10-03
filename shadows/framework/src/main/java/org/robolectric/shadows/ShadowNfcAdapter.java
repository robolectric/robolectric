package org.robolectric.shadows;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(NfcAdapter.class)
public class ShadowNfcAdapter {
  @RealObject NfcAdapter nfcAdapter;
  private static boolean hardwareExists = true;
  private boolean enabled;
  private Activity enabledActivity;
  private PendingIntent intent;
  private IntentFilter[] filters;
  private String[][] techLists;
  private Activity disabledActivity;
  private NdefMessage ndefPushMessage;
  private boolean ndefPushMessageSet;
  private NfcAdapter.CreateNdefMessageCallback ndefPushMessageCallback;
  private NfcAdapter.OnNdefPushCompleteCallback onNdefPushCompleteCallback;

  @Implementation
  protected static NfcAdapter getNfcAdapter(Context context) {
    if (!hardwareExists) {
      return null;
    }
    return ReflectionHelpers.callConstructor(NfcAdapter.class);
  }

  @Implementation
  protected void enableForegroundDispatch(
      Activity activity, PendingIntent intent, IntentFilter[] filters, String[][] techLists) {
    this.enabledActivity = activity;
    this.intent = intent;
    this.filters = filters;
    this.techLists = techLists;
  }

  @Implementation
  protected void disableForegroundDispatch(Activity activity) {
    disabledActivity = activity;
  }

  /**
   * Mocks setting NDEF push message so that it could be verified in the test. Use {@link
   * #getNdefPushMessage()} to verify that message was set.
   */
  @Implementation
  protected void setNdefPushMessage(
      NdefMessage message, Activity activity, Activity... activities) {
    if (activity == null) {
      throw new NullPointerException("activity cannot be null");
    }
    for (Activity a : activities) {
      if (a == null) {
        throw new NullPointerException("activities cannot contain null");
      }
    }
    this.ndefPushMessage = message;
    this.ndefPushMessageSet = true;
  }

  @Implementation
  protected void setNdefPushMessageCallback(
      NfcAdapter.CreateNdefMessageCallback callback, Activity activity, Activity... activities) {
    this.ndefPushMessageCallback = callback;
  }

  /**
   * Sets callback that should be used on successful Android Beam (TM).
   *
   * <p>The last registered callback is recalled and can be fetched using {@link
   * #getOnNdefPushCompleteCallback}.
   */
  @Implementation
  protected void setOnNdefPushCompleteCallback(
      NfcAdapter.OnNdefPushCompleteCallback callback, Activity activity, Activity... activities) {
    if (activity == null) {
      throw new NullPointerException("activity cannot be null");
    }
    for (Activity a : activities) {
      if (a == null) {
        throw new NullPointerException("activities cannot contain null");
      }
    }
    this.onNdefPushCompleteCallback = callback;
  }

  @Implementation
  protected boolean isEnabled() {
    return enabled;
  }

  /**
   * Modifies behavior of {@link #getNfcAdapter(Context)} to return {@code null}, to simulate
   * absence of NFC hardware.
   */
  public static void setNfcHardwareExists(boolean hardwareExists) {
    ShadowNfcAdapter.hardwareExists = hardwareExists;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Activity getEnabledActivity() {
    return enabledActivity;
  }

  public PendingIntent getIntent() {
    return intent;
  }

  public IntentFilter[] getFilters() {
    return filters;
  }

  public String[][] getTechLists() {
    return techLists;
  }

  public Activity getDisabledActivity() {
    return disabledActivity;
  }

  /** Returns last registered callback, or {@code null} if none was set. */
  public NfcAdapter.CreateNdefMessageCallback getNdefPushMessageCallback() {
    return ndefPushMessageCallback;
  }

  public NfcAdapter.OnNdefPushCompleteCallback getOnNdefPushCompleteCallback() {
    return onNdefPushCompleteCallback;
  }

  /** Returns last set NDEF message, or throws {@code IllegalStateException} if it was never set. */
  public NdefMessage getNdefPushMessage() {
    if (!ndefPushMessageSet) {
      throw new IllegalStateException();
    }
    return ndefPushMessage;
  }

  @Resetter
  public static synchronized void reset() {
    hardwareExists = true;
  }
}
