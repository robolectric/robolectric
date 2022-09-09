package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.os.Build.VERSION_CODES;
import android.uwb.AdapterStateListener;
import android.uwb.IUwbAdapter;
import android.uwb.UwbManager.AdapterStateCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.util.concurrent.PausedExecutorService;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowUwbAdapterStateListener}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.S)
public class ShadowUwbAdapterStateListenerTest {
  private Object /* AdapterStateListener */ adapterStateListenerObject;

  @Before
  public void setUp() {
    IUwbAdapter mockUwbAdapter = mock(IUwbAdapter.class);
    adapterStateListenerObject = new AdapterStateListener(mockUwbAdapter);
  }

  @Test
  public void testSetEnabledTrue_isEnabled() {
    AdapterStateListener adapterStateListener = (AdapterStateListener) adapterStateListenerObject;

    adapterStateListener.setEnabled(true);
    assertThat(adapterStateListener.getAdapterState())
        .isEqualTo(AdapterStateCallback.STATE_ENABLED_INACTIVE);
  }

  @Test
  public void testSetEnabledFalse_isDisabled() {
    AdapterStateListener adapterStateListener = (AdapterStateListener) adapterStateListenerObject;

    adapterStateListener.setEnabled(false);
    assertThat(adapterStateListener.getAdapterState())
        .isEqualTo(AdapterStateCallback.STATE_DISABLED);
  }

  @Test
  public void testOnAdapterStateChanged_stateIsUpdated() {
    AdapterStateListener adapterStateListener = (AdapterStateListener) adapterStateListenerObject;

    adapterStateListener.onAdapterStateChanged(
        AdapterStateCallback.STATE_ENABLED_ACTIVE,
        AdapterStateCallback.STATE_CHANGED_REASON_SESSION_STARTED);
    assertThat(adapterStateListener.getAdapterState())
        .isEqualTo(AdapterStateCallback.STATE_ENABLED_ACTIVE);

    adapterStateListener.onAdapterStateChanged(
        AdapterStateCallback.STATE_ENABLED_INACTIVE,
        AdapterStateCallback.STATE_CHANGED_REASON_ALL_SESSIONS_CLOSED);
    assertThat(adapterStateListener.getAdapterState())
        .isEqualTo(AdapterStateCallback.STATE_ENABLED_INACTIVE);

    adapterStateListener.onAdapterStateChanged(
        AdapterStateCallback.STATE_DISABLED,
        AdapterStateCallback.STATE_CHANGED_REASON_ERROR_UNKNOWN);
    assertThat(adapterStateListener.getAdapterState())
        .isEqualTo(AdapterStateCallback.STATE_DISABLED);
  }

  @Test
  public void testRegisterCallback() {
    AdapterStateListener adapterStateListener = (AdapterStateListener) adapterStateListenerObject;
    AdapterStateCallback mockAdapterStateCallback = mock(AdapterStateCallback.class);
    PausedExecutorService executorService = new PausedExecutorService();

    adapterStateListener.register(executorService, mockAdapterStateCallback);
    executorService.runAll();

    verify(mockAdapterStateCallback)
        .onStateChanged(
            adapterStateListener.getAdapterState(),
            AdapterStateCallback.STATE_CHANGED_REASON_ERROR_UNKNOWN);

    adapterStateListener.onAdapterStateChanged(
        AdapterStateCallback.STATE_ENABLED_ACTIVE,
        AdapterStateCallback.STATE_CHANGED_REASON_SESSION_STARTED);
    executorService.runAll();

    verify(mockAdapterStateCallback)
        .onStateChanged(
            AdapterStateCallback.STATE_ENABLED_ACTIVE,
            AdapterStateCallback.STATE_CHANGED_REASON_SESSION_STARTED);

    adapterStateListener.unregister(mockAdapterStateCallback);
    adapterStateListener.onAdapterStateChanged(
        AdapterStateCallback.STATE_ENABLED_INACTIVE,
        AdapterStateCallback.STATE_CHANGED_REASON_ALL_SESSIONS_CLOSED);
    executorService.runAll();

    verifyNoMoreInteractions(mockAdapterStateCallback);
  }
}
