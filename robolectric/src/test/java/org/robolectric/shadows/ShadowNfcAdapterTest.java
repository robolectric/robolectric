package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowNfcAdapterTest {

  @Rule public ExpectedException expectedException = ExpectedException.none();
  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
    shadowOf(context.getPackageManager())
        .setSystemFeature(PackageManager.FEATURE_NFC, /* supported= */ true);
  }

  @Test
  public void setNdefPushMesageCallback_shouldUseCallback() {
    final NfcAdapter.CreateNdefMessageCallback callback = mock(NfcAdapter.CreateNdefMessageCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    adapter.setNdefPushMessageCallback(callback, activity);
    assertThat(shadowOf(adapter).getNdefPushMessageCallback()).isSameInstanceAs(callback);
  }

  @Test
  public void setOnNdefPushCompleteCallback_shouldUseCallback() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    adapter.setOnNdefPushCompleteCallback(callback, activity);
    assertThat(shadowOf(adapter).getOnNdefPushCompleteCallback()).isSameInstanceAs(callback);
  }

  @Test
  public void setOnNdefPushCompleteCallback_throwsOnNullActivity() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final Activity nullActivity = null;
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("activity cannot be null");
    adapter.setOnNdefPushCompleteCallback(callback, nullActivity);
  }

  @Test
  public void setOnNdefPushCompleteCallback_throwsOnNullInActivities() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final Activity nullActivity = null;
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("activities cannot contain null");

    adapter.setOnNdefPushCompleteCallback(callback, activity, nullActivity);
  }

  @Test
  public void isEnabled_shouldReturnEnabledState() {
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
    assertThat(adapter.isEnabled()).isFalse();

    shadowOf(adapter).setEnabled(true);
    assertThat(adapter.isEnabled()).isTrue();

    shadowOf(adapter).setEnabled(false);
    assertThat(adapter.isEnabled()).isFalse();
  }

  @Test
  public void getNfcAdapter_returnsNonNull() {
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
    assertThat(adapter).isNotNull();
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
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    adapter.setNdefPushMessage(null, activity);

    assertThat(shadowOf(adapter).getNdefPushMessage()).isNull();
  }

  @Test
  public void setNdefPushMessage_setsNonNullMessage() {
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
    final NdefMessage message =
        new NdefMessage(new NdefRecord[] {new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)});

    adapter.setNdefPushMessage(message, activity);

    assertThat(shadowOf(adapter).getNdefPushMessage()).isSameInstanceAs(message);
  }

  @Test
  public void getNdefPushMessage_messageNotSet_throwsIllegalStateException() {
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    expectedException.expect(IllegalStateException.class);

    shadowOf(adapter).getNdefPushMessage();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.KITKAT)
  public void isInReaderMode_beforeEnableReaderMode_shouldReturnFalse() {
    createActivity(
        activity -> {
          NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
          assertThat(shadowOf(adapter).isInReaderMode()).isFalse();
        });
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.KITKAT)
  public void isInReaderMode_afterEnableReaderMode_shouldReturnTrue() {
    createActivity(
        activity -> {
          NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
          NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
          adapter.enableReaderMode(
              activity,
              callback,
              NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
              /* extras= */ null);

          assertThat(shadowOf(adapter).isInReaderMode()).isTrue();
        });
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.KITKAT)
  public void isInReaderMode_afterDisableReaderMode_shouldReturnFalse() {
    createActivity(
        activity -> {
          NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
          NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
          adapter.enableReaderMode(
              activity,
              callback,
              NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
              /* extras= */ null);
          adapter.disableReaderMode(activity);

          assertThat(shadowOf(adapter).isInReaderMode()).isFalse();
        });
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.KITKAT)
  public void dispatchTagDiscovered_shouldDispatchTagToCallback() {
    createActivity(
        activity -> {
          NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
          NfcAdapter.ReaderCallback callback = mock(NfcAdapter.ReaderCallback.class);
          adapter.enableReaderMode(
              activity,
              callback,
              NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
              /* extras= */ null);
          Tag tag = createMockTag();
          shadowOf(adapter).dispatchTagDiscovered(tag);

          verify(callback).onTagDiscovered(same(tag));
        });
  }

  private static Tag createMockTag() {
    return Tag.createMockTag(new byte[0], new int[0], new Bundle[0]);
  }

  private void createActivity(ActivityScenario.ActivityAction<Activity> activityAction) {
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.packageName = context.getPackageName();
    activityInfo.name = "android.app.Activity";
    shadowOf(context.getPackageManager()).addOrUpdateActivity(activityInfo);
    try (ActivityScenario<Activity> scenario = ActivityScenario.launch(Activity.class)) {
      scenario.onActivity(activityAction);
    }
  }
}
