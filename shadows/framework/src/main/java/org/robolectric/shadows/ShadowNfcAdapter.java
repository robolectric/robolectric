package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
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
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow implementation of {@link NfcAdapter}. */
@Implements(NfcAdapter.class)
public class ShadowNfcAdapter {
  @RealObject NfcAdapter nfcAdapter;

  @GuardedBy("ShadowNfcAdapter.class")
  private static boolean hardwareExists = true;

  @GuardedBy("this")
  private boolean enabled;

  @GuardedBy("this")
  private boolean secureNfcSupported;

  @GuardedBy("this")
  private boolean secureNfcEnabled;

  @GuardedBy("this")
  private Activity enabledActivity;

  @GuardedBy("this")
  private PendingIntent intent;

  @GuardedBy("this")
  private IntentFilter[] filters;

  @GuardedBy("this")
  private String[][] techLists;

  @GuardedBy("this")
  private Activity disabledActivity;

  @GuardedBy("this")
  private NdefMessage ndefPushMessage;

  @GuardedBy("this")
  private boolean ndefPushMessageSet;

  @GuardedBy("this")
  private NfcAdapter.CreateNdefMessageCallback ndefPushMessageCallback;

  @GuardedBy("this")
  private NfcAdapter.OnNdefPushCompleteCallback onNdefPushCompleteCallback;

  @GuardedBy("this")
  private NfcAdapter.ReaderCallback readerCallback;

  @Implementation
  protected static NfcAdapter getDefaultAdapter(Context context) {
    // The result of `getNfcAdapter` is cached, so need to check `hardwareExists` again here in case
    // its value was set after the value returned by `getNfcAdapter` got cached.
    synchronized (ShadowNfcAdapter.class) {
      if (!hardwareExists) {
        return null;
      }
    }
    return reflector(NfcAdapterReflector.class).getDefaultAdapter(context);
  }

  @Implementation
  protected static NfcAdapter getNfcAdapter(Context context) {
    synchronized (ShadowNfcAdapter.class) {
      if (!hardwareExists) {
        return null;
      }
    }
    return reflector(NfcAdapterReflector.class).getNfcAdapter(context);
  }

  /** Factory method for creating a mock NfcAdapter.Tag */
  public static Tag createMockTag() {
    if (RuntimeEnvironment.getApiLevel() <= TIRAMISU) {
      return ReflectionHelpers.callStaticMethod(
          Tag.class,
          "createMockTag",
          ClassParameter.from(byte[].class, new byte[0]),
          ClassParameter.from(int[].class, new int[0]),
          ClassParameter.from(Bundle[].class, new Bundle[0]));

    } else {
      return ReflectionHelpers.callStaticMethod(
          Tag.class,
          "createMockTag",
          ClassParameter.from(byte[].class, new byte[0]),
          ClassParameter.from(int[].class, new int[0]),
          ClassParameter.from(Bundle[].class, new Bundle[0]),
          ClassParameter.from(long.class, 0));
    }
  }

  @Implementation
  protected void enableForegroundDispatch(
      Activity activity, PendingIntent intent, IntentFilter[] filters, String[][] techLists) {
    synchronized (this) {
      this.enabledActivity = activity;
      this.intent = intent;
      this.filters = filters;
      this.techLists = techLists;
    }
  }

  @Implementation
  protected void disableForegroundDispatch(Activity activity) {
    synchronized (this) {
      disabledActivity = activity;
    }
  }

  @Implementation
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

    synchronized (this) {
      readerCallback = callback;
    }
  }

  @Implementation
  protected void disableReaderMode(Activity activity) {
    if (!RuntimeEnvironment.getApplication()
        .getPackageManager()
        .hasSystemFeature(PackageManager.FEATURE_NFC)) {
      throw new UnsupportedOperationException();
    }
    synchronized (this) {
      readerCallback = null;
    }
  }

  /** Returns true if NFC is in reader mode. */
  public synchronized boolean isInReaderMode() {
    return readerCallback != null;
  }

  /** Dispatches the tag onto any registered readers. */
  public void dispatchTagDiscovered(Tag tag) {
    NfcAdapter.ReaderCallback callback;
    synchronized (this) {
      callback = readerCallback;
    }
    if (callback != null) {
      callback.onTagDiscovered(tag);
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
    synchronized (this) {
      this.ndefPushMessage = message;
      this.ndefPushMessageSet = true;
    }
  }

  @Implementation
  protected void setNdefPushMessageCallback(
      NfcAdapter.CreateNdefMessageCallback callback, Activity activity, Activity... activities) {
    synchronized (this) {
      this.ndefPushMessageCallback = callback;
    }
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
    synchronized (this) {
      this.onNdefPushCompleteCallback = callback;
    }
  }

  @Implementation
  protected boolean isEnabled() {
    synchronized (this) {
      return enabled;
    }
  }

  @Implementation
  protected boolean enable() {
    synchronized (this) {
      enabled = true;
    }
    return true;
  }

  @Implementation
  protected boolean disable() {
    synchronized (this) {
      enabled = false;
    }
    return true;
  }

  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected boolean isSecureNfcSupported() {
    synchronized (this) {
      return secureNfcSupported;
    }
  }

  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected boolean isSecureNfcEnabled() {
    synchronized (this) {
      return secureNfcEnabled;
    }
  }

  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected boolean enableSecureNfc(boolean enableSecureNfc) {
    synchronized (this) {
      this.secureNfcEnabled = enableSecureNfc;
    }
    return true;
  }

  /**
   * Modifies the behavior of {@link #getNfcAdapter(Context)} to return {@code null}, to simulate
   * absence of NFC hardware.
   */
  public static synchronized void setNfcHardwareExists(boolean hardwareExists) {
    ShadowNfcAdapter.hardwareExists = hardwareExists;
  }

  public synchronized void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public synchronized void setSecureNfcSupported(boolean secureNfcSupported) {
    this.secureNfcSupported = secureNfcSupported;
  }

  public synchronized Activity getEnabledActivity() {
    return enabledActivity;
  }

  public synchronized PendingIntent getIntent() {
    return intent;
  }

  public synchronized IntentFilter[] getFilters() {
    return filters;
  }

  public synchronized String[][] getTechLists() {
    return techLists;
  }

  public synchronized Activity getDisabledActivity() {
    return disabledActivity;
  }

  /** Returns last registered callback, or {@code null} if none was set. */
  public synchronized NfcAdapter.CreateNdefMessageCallback getNdefPushMessageCallback() {
    return ndefPushMessageCallback;
  }

  public synchronized NfcAdapter.OnNdefPushCompleteCallback getOnNdefPushCompleteCallback() {
    return onNdefPushCompleteCallback;
  }

  /** Returns last set NDEF message, or throws {@code IllegalStateException} if it was never set. */
  public synchronized NdefMessage getNdefPushMessage() {
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
      if (RuntimeEnvironment.getApiLevel() <= TIRAMISU) {
        nfcAdapterReflector.setHasBeamFeature(false);
      }
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

    @Direct
    @Static
    NfcAdapter getDefaultAdapter(Context context);
  }

  @ForType(Tag.class)
  interface TagReflector {
    Tag createMockTag(byte[] id, int[] techList, Bundle[] techListExtras, long cookie);
  }
}
