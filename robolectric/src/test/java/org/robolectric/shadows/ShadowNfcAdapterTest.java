package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAntennaInfo;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.ForType;

@RunWith(AndroidJUnit4.class)
public class ShadowNfcAdapterTest {

  private final Application context = RuntimeEnvironment.getApplication();

  @Before
  public void setUp() throws Exception {
    shadowOf(context.getPackageManager())
        .setSystemFeature(PackageManager.FEATURE_NFC, /* supported= */ true);
  }

  @Test
  public void setNdefPushMessageCallback_shouldUseCallback() {
    final NfcAdapter.CreateNdefMessageCallback callback =
        mock(NfcAdapter.CreateNdefMessageCallback.class);
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

            shadowOf(adapter).setNdefPushMessageCallback(callback, activity);
            assertThat(shadowOf(adapter).getNdefPushMessageCallback()).isSameInstanceAs(callback);
          });
    }
  }

  @Test
  public void setOnNdefPushCompleteCallback_shouldUseCallback() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            final ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));

            adapter.setOnNdefPushCompleteCallback(callback, activity);
            assertThat(adapter.getOnNdefPushCompleteCallback()).isSameInstanceAs(callback);
          });
    }
  }

  @Test
  public void setOnNdefPushCompleteCallback_throwsOnNullActivity() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            final Activity nullActivity = null;
            final ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));

            NullPointerException exception =
                assertThrows(
                    NullPointerException.class,
                    () -> adapter.setOnNdefPushCompleteCallback(callback, nullActivity));
            assertThat(exception).hasMessageThat().contains("activity cannot be null");
          });
    }
  }

  @Test
  public void setOnNdefPushCompleteCallback_throwsOnNullInActivities() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            final Activity nullActivity = null;
            final ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));

            NullPointerException exception =
                assertThrows(
                    NullPointerException.class,
                    () -> adapter.setOnNdefPushCompleteCallback(callback, activity, nullActivity));
            assertThat(exception).hasMessageThat().contains("activities cannot contain null");
          });
    }
  }

  @Test
  public void isEnabled_shouldReturnEnabledState() {
    final ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(context));
    assertThat(adapter.isEnabled()).isFalse();

    adapter.setEnabled(true);
    assertThat(adapter.isEnabled()).isTrue();

    adapter.setEnabled(false);
    assertThat(adapter.isEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void isSecureNfcSupported_shouldReturnSupportedState() {
    ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(context));
    assertThat(adapter.isSecureNfcSupported()).isFalse();

    adapter.setSecureNfcSupported(true);
    assertThat(adapter.isSecureNfcSupported()).isTrue();

    adapter.setSecureNfcSupported(false);
    assertThat(adapter.isSecureNfcSupported()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void isSecureNfcEnabled_shouldReturnEnabledState() {
    ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(context));
    assertThat(adapter.isSecureNfcEnabled()).isFalse();

    adapter.enableSecureNfc(true);
    assertThat(adapter.isSecureNfcEnabled()).isTrue();

    adapter.enableSecureNfc(false);
    assertThat(adapter.isSecureNfcEnabled()).isFalse();
  }

  @Test
  public void getNfcAdapter_returnsNonNull() {
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);

    assertThat(adapter).isNotNull();

    // This is checked twice to prevent a regression where attempting to acquire the
    // `getDefaultAdapter` twice in a test would cause a `null` value to be returned on UDC+.
    NfcAdapter adapterAgain = NfcAdapter.getDefaultAdapter(context);

    assertThat(adapterAgain).isNotNull();
  }

  @Test
  public void getNfcAdapter_hardwareExists_returnsNonNull() {
    ShadowNfcAdapter.setNfcHardwareExists(true);

    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);

    assertThat(adapter).isNotNull();
  }

  @Test
  public void getNfcAdapter_hardwareDoesNotExist_returnsNull() {
    ShadowNfcAdapter.setNfcHardwareExists(false);

    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);

    assertThat(adapter).isNull();
  }

  @Test
  public void setNdefPushMessage_setsNullMessage() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            final ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));

            adapter.setNdefPushMessage(null, activity);

            assertThat(adapter.getNdefPushMessage()).isNull();
          });
    }
  }

  @Test
  public void setNdefPushMessage_setsNonNullMessage() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            final ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));
            final NdefMessage message =
                new NdefMessage(
                    new NdefRecord[] {new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)});

            adapter.setNdefPushMessage(message, activity);

            assertThat(adapter.getNdefPushMessage()).isSameInstanceAs(message);
          });
    }
  }

  @Test
  public void getNdefPushMessage_messageNotSet_throwsIllegalStateException() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            final ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));

            assertThrows(IllegalStateException.class, () -> adapter.getNdefPushMessage());
          });
    }
  }

  @Test
  public void isInReaderMode_beforeEnableReaderMode_shouldReturnFalse() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));
            assertThat(adapter.isInReaderMode()).isFalse();
          });
    }
  }

  @Test
  public void isInReaderMode_afterEnableReaderMode_shouldReturnTrue() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));
            NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
            adapter.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                /* extras= */ null);

            assertThat(adapter.isInReaderMode()).isTrue();
          });
    }
  }

  @Test
  public void getReaderModeExtras_beforeEnableReaderMode_shouldReturnNull() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));

            assertThat(adapter.getReaderModeExtras()).isNull();
          });
    }
  }

  @Test
  public void isInReaderMode_afterDisableReaderMode_shouldReturnFalse() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));
            NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
            adapter.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                /* extras= */ null);
            adapter.disableReaderMode(activity);

            assertThat(adapter.isInReaderMode()).isFalse();
          });
    }
  }

  @Test
  public void getReaderModeExtras_afterEnableReaderMode_shouldReturnExtras() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));
            NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
            Bundle extras = new Bundle();
            extras.putByteArray("test", new byte[] {0x01, 0x02});
            adapter.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                extras);

            assertThat(adapter.getReaderModeExtras()).isEqualTo(extras);
          });
    }
  }

  @Test
  public void getReaderModeExtras_afterDisableReaderMode_shouldReturnNull() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));
            NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
            Bundle extras = new Bundle();
            extras.putByteArray("test", new byte[] {0x01, 0x02});
            adapter.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                extras);
            adapter.disableReaderMode(activity);

            assertThat(adapter.getReaderModeExtras()).isNull();
          });
    }
  }

  @Test
  public void dispatchTagDiscovered_shouldDispatchTagToCallback() {
    try (ActivityScenario<Activity> scenario =
        ActivityScenario.launch(new Intent(context, Activity.class))) {
      scenario.onActivity(
          activity -> {
            ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(activity));
            NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
            adapter.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                /* extras= */ null);
            Tag tag = ShadowNfcAdapter.createMockTag();
            adapter.dispatchTagDiscovered(tag);

            verify(callback).onTagDiscovered(same(tag));
          });
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void getNfcAntennaInfo_noneSet_returnsNull() {
    ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(context));

    assertThat(adapter.getNfcAntennaInfo()).isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void getNfcAntennaInfo_returnsSetInfo() {
    ShadowNfcAdapter adapter = shadowOf(NfcAdapter.getDefaultAdapter(context));
    NfcAntennaInfo info = new NfcAntennaInfo(0, 0, false, Collections.emptyList());
    adapter.setNfcAntennaInfo(info);

    assertThat(adapter.getNfcAntennaInfo()).isEqualTo(info);
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void isObserveModeSupported_shouldReturnSupportedState() {
    final NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
    final ShadowNfcAdapter adapter = shadowOf(nfcAdapter);
    final NfcAdapterVReflector adapterReflector = reflector(NfcAdapterVReflector.class, nfcAdapter);
    assertThat(adapterReflector.isObserveModeSupported()).isFalse();

    adapter.setObserveModeSupported(true);
    assertThat(adapterReflector.isObserveModeSupported()).isTrue();

    adapter.setObserveModeSupported(false);
    assertThat(adapterReflector.isObserveModeSupported()).isFalse();
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void isObserveModeEnabled_shouldReturnEnabledState() {
    final NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
    final ShadowNfcAdapter adapter = shadowOf(nfcAdapter);
    final NfcAdapterVReflector adapterReflector = reflector(NfcAdapterVReflector.class, nfcAdapter);
    adapter.setObserveModeSupported(true);
    assertThat(adapterReflector.isObserveModeEnabled()).isFalse();

    adapterReflector.setObserveModeEnabled(true);
    assertThat(adapterReflector.isObserveModeEnabled()).isTrue();

    adapterReflector.setObserveModeEnabled(false);
    assertThat(adapterReflector.isObserveModeEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void setObserveModeEnabled_notSupported_doesNothing() {
    final NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
    final ShadowNfcAdapter adapter = shadowOf(nfcAdapter);
    final NfcAdapterVReflector adapterReflector = reflector(NfcAdapterVReflector.class, nfcAdapter);
    adapter.setObserveModeSupported(false);
    assertThat(adapterReflector.isObserveModeEnabled()).isFalse();

    adapterReflector.setObserveModeEnabled(true);
    assertThat(adapterReflector.isObserveModeEnabled()).isFalse();
  }

  // TODO: delete when this test compiles against V sdk
  @ForType(NfcAdapter.class)
  interface NfcAdapterVReflector {
    boolean isObserveModeSupported();

    boolean isObserveModeEnabled();

    void setObserveModeEnabled(boolean enabled);
  }
}
