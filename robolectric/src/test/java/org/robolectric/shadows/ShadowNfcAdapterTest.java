package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.nfc.NfcAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class ShadowNfcAdapterTest {

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void setNdefPushMesageCallback_shouldUseCallback() {
    final NfcAdapter.CreateNdefMessageCallback callback = mock(NfcAdapter.CreateNdefMessageCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    adapter.setNdefPushMessageCallback(callback, activity);
    assertThat(shadowOf(adapter).getNdefPushMessageCallback()).isSameAs(callback);
  }

  @Test
  public void setOnNdefPushCompleteCallback_shouldUseCallback() {
    final NfcAdapter.OnNdefPushCompleteCallback callback =
        mock(NfcAdapter.OnNdefPushCompleteCallback.class);
    final Activity activity = Robolectric.setupActivity(Activity.class);
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);

    adapter.setOnNdefPushCompleteCallback(callback, activity);
    assertThat(shadowOf(adapter).getOnNdefPushCompleteCallback()).isSameAs(callback);
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
    final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(RuntimeEnvironment.application);
    assertThat(adapter.isEnabled()).isFalse();

    shadowOf(adapter).setEnabled(true);
    assertThat(adapter.isEnabled()).isTrue();

    shadowOf(adapter).setEnabled(false);
    assertThat(adapter.isEnabled()).isFalse();
  }
}
