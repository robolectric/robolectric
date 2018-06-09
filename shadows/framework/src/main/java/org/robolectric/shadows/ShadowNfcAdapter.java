package org.robolectric.shadows;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(NfcAdapter.class)
public class ShadowNfcAdapter {
  @RealObject NfcAdapter nfcAdapter;
  private boolean enabled;
  private Activity enabledActivity;
  private PendingIntent intent;
  private IntentFilter[] filters;
  private String[][] techLists;
  private Activity disabledActivity;
  private NfcAdapter.CreateNdefMessageCallback ndefPushMessageCallback;
  private NfcAdapter.OnNdefPushCompleteCallback onNdefPushCompleteCallback;

  @Implementation
  public static NfcAdapter getNfcAdapter(Context context) {
    return ReflectionHelpers.callConstructor(NfcAdapter.class);
  }

  @Implementation
  public void enableForegroundDispatch(Activity activity, PendingIntent intent, IntentFilter[] filters, String[][] techLists) {
    this.enabledActivity = activity;
    this.intent = intent;
    this.filters = filters;
    this.techLists = techLists;
  }

  @Implementation
  public void disableForegroundDispatch(Activity activity) {
    disabledActivity = activity;
  }

  @Implementation
  public void setNdefPushMessageCallback(NfcAdapter.CreateNdefMessageCallback callback, Activity activity, Activity... activities) {
    this.ndefPushMessageCallback = callback;
  }

  /**
   * Sets callback that should be used on successful Android Beam (TM).
   *
   * <p>The last registered callback is recalled and can be fetched using {@link
   * #getOnNdefPushCompleteCallback}.
   */
  @Implementation
  public void setOnNdefPushCompleteCallback(
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
  public boolean isEnabled() {
    return enabled;
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

}
