package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow implementation of {@link NfcAdapter}. */
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
  private NfcAdapter.ReaderCallback readerCallback;

  @Implementation
  protected static NfcAdapter getNfcAdapter(Context context) {
    if (!hardwareExists) {
      return null;
    }
    return reflector(NfcAdapterReflector.class).getNfcAdapter(context);
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

  @Implementation(minSdk = Build.VERSION_CODES.KITKAT)
  protected void enableReaderMode(
      Activity activity, NfcAdapter.ReaderCallback callback, int flags, Bundle extras) {
    if (!RuntimeEnvironment.getApplication()
        .getPackageManager()
        .hasSystemFeature(PackageManager.FEATURE_NFC)) {
      throw new UnsupportedOperationException();
    }
    if (callback == null) {
      throw new NullPointerException("ReaderCallback is null");
    }
    readerCallback = callback;
  }

  @Implementation(minSdk = Build.VERSION_CODES.KITKAT)
  protected void disableReaderMode(Activity activity) {
    if (!RuntimeEnvironment.getApplication()
        .getPackageManager()
        .hasSystemFeature(PackageManager.FEATURE_NFC)) {
      throw new UnsupportedOperationException();
    }
    readerCallback = null;
  }

  /** Returns true if NFC is in reader mode. */
  public boolean isInReaderMode() {
    return readerCallback != null;
  }

  /** Dispatches the tag onto any registered readers. */
  public void dispatchTagDiscovered(Tag tag) {
    if (readerCallback != null) {
      readerCallback.onTagDiscovered(tag);
    }
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

  @Implementation
  protected boolean enable() {
    enabled = true;
    return true;
  }

  @Implementation
  protected boolean disable() {
    enabled = false;
    return true;
  }

  /**
   * Modifies the behavior of {@link #getNfcAdapter(Context)} to return {@code null}, to simulate
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
    NfcAdapterReflector nfcAdapterReflector = reflector(NfcAdapterReflector.class);
    nfcAdapterReflector.setIsInitialized(false);
    Map<Context, NfcAdapter> adapters = nfcAdapterReflector.getNfcAdapters();
    if (adapters != null) {
      adapters.clear();
    }
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.Q) {
      nfcAdapterReflector.setHasNfcFeature(false);
      nfcAdapterReflector.setHasBeamFeature(false);
    }
  }

  @ForType(NfcAdapter.class)
  interface NfcAdapterReflector {
    @Static
    @Accessor("sIsInitialized")
    void setIsInitialized(boolean isInitialized);

    @Static
    @Accessor("sHasNfcFeature")
    void setHasNfcFeature(boolean hasNfcFeature);

    @Static
    @Accessor("sHasBeamFeature")
    void setHasBeamFeature(boolean hasBeamFeature);

    @Static
    @Accessor("sNfcAdapters")
    Map<Context, NfcAdapter> getNfcAdapters();

    @Direct
    @Static
    NfcAdapter getNfcAdapter(Context context);
  }
}
