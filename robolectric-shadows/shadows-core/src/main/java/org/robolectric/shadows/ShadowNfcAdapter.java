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

/**
 * Shadow for {@link android.nfc.NfcAdapter}.
 */
@Implements(NfcAdapter.class)
public class ShadowNfcAdapter {
  @RealObject NfcAdapter nfcAdapter;
  private boolean enabled;
  private Activity enabledActivity;
  private PendingIntent intent;
  private IntentFilter[] filters;
  private String[][] techLists;
  private Activity disabledActivity;
  private NfcAdapter.CreateNdefMessageCallback callback;

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
    this.callback = callback;
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

  public NfcAdapter.CreateNdefMessageCallback getNdefPushMessageCallback() {
    return callback;
  }
}
