package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.os.PersistableBundle;
import android.uwb.RangingSession;
import android.uwb.UwbManager;
import android.uwb.UwbManager.AdapterStateCallback;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for {@link ShadowUwbManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = S)
public class ShadowUwbManagerTest {
  private /* RangingSession.Callback */ Object callbackObject;
  private /* AdapterStateCallback */ Object adapterStateCallbackObject;
  private /* UwbManager */ Object uwbManagerObject;
  private ShadowRangingSession.Adapter adapter;

  @Before
  public void setUp() {
    callbackObject = mock(RangingSession.Callback.class);
    adapterStateCallbackObject = mock(AdapterStateCallback.class);
    adapter = mock(ShadowRangingSession.Adapter.class);
    uwbManagerObject = getApplicationContext().getSystemService(UwbManager.class);
    ((UwbManager) uwbManagerObject)
        .unregisterAdapterStateCallback((AdapterStateCallback) adapterStateCallbackObject);
  }

  @Test
  public void registerAdapterStateCallback_invokesCallbackOnceInitially() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    AdapterStateCallback adapterStateCallback = (AdapterStateCallback) adapterStateCallbackObject;

    Shadow.<ShadowUwbManager>extract(manager)
        .registerAdapterStateCallback(directExecutor(), adapterStateCallback);

    verify(adapterStateCallback).onStateChanged(anyInt(), anyInt());
  }

  @Test
  public void simulateAdapterStateChange_invokesCallbackWithGivenStateAndReason() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    AdapterStateCallback adapterStateCallback = (AdapterStateCallback) adapterStateCallbackObject;
    manager.registerAdapterStateCallback(directExecutor(), adapterStateCallback);

    Shadow.<ShadowUwbManager>extract(manager)
        .simulateAdapterStateChange(
            AdapterStateCallback.STATE_DISABLED,
            AdapterStateCallback.STATE_CHANGED_REASON_SYSTEM_REGULATION);

    verify(adapterStateCallback)
        .onStateChanged(
            AdapterStateCallback.STATE_DISABLED,
            AdapterStateCallback.STATE_CHANGED_REASON_SYSTEM_REGULATION);
  }

  @Test
  public void getAdapterState_returnsStateFromSimulateAdapterStateChange() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    AdapterStateCallback adapterStateCallback = (AdapterStateCallback) adapterStateCallbackObject;
    manager.registerAdapterStateCallback(directExecutor(), adapterStateCallback);

    Shadow.<ShadowUwbManager>extract(manager)
        .simulateAdapterStateChange(
            AdapterStateCallback.STATE_ENABLED_ACTIVE,
            AdapterStateCallback.STATE_CHANGED_REASON_SYSTEM_REGULATION);

    assertThat(manager.getAdapterState()).isEqualTo(AdapterStateCallback.STATE_ENABLED_ACTIVE);

    Shadow.<ShadowUwbManager>extract(manager)
        .simulateAdapterStateChange(
            AdapterStateCallback.STATE_DISABLED,
            AdapterStateCallback.STATE_CHANGED_REASON_SYSTEM_REGULATION);

    assertThat(manager.getAdapterState()).isEqualTo(AdapterStateCallback.STATE_DISABLED);
  }

  @Test
  public void openRangingSession_openAdapter() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    RangingSession.Callback callback = (RangingSession.Callback) callbackObject;
    Shadow.<ShadowUwbManager>extract(manager).setUwbAdapter(adapter);
    manager.openRangingSession(genParams("openRangingSession"), directExecutor(), callback);
    verify(adapter)
        .onOpen(
            any(RangingSession.class), eq(callback), argThat(checkParams("openRangingSession")));
  }

  @Test
  public void getSpecificationInfo_expectedValue() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    Shadow.<ShadowUwbManager>extract(manager)
        .setSpecificationInfo(genParams("getSpecificationInfo"));
    assertThat(getName(manager.getSpecificationInfo())).isEqualTo("getSpecificationInfo");
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void openRangingSessionWithChipId_openAdapter() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    RangingSession.Callback callback = (RangingSession.Callback) callbackObject;
    Shadow.<ShadowUwbManager>extract(manager).setUwbAdapter(adapter);
    manager.openRangingSession(
        genParams("openRangingSession"), directExecutor(), callback, "chipId");
    verify(adapter)
        .onOpen(
            any(RangingSession.class), eq(callback), argThat(checkParams("openRangingSession")));
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setUwbEnabled_setToTrue_enablesUwbAndInvokesCallback() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    AdapterStateCallback adapterStateCallback = (AdapterStateCallback) adapterStateCallbackObject;
    manager.registerAdapterStateCallback(directExecutor(), adapterStateCallback);

    Shadow.<ShadowUwbManager>extract(manager).setUwbEnabled(false);
    Shadow.<ShadowUwbManager>extract(manager).setUwbEnabled(true);

    assertThat(manager.isUwbEnabled()).isTrue();
    // Invoked once when the callback is initially registered
    verify(adapterStateCallback, times(2))
        .onStateChanged(
            AdapterStateCallback.STATE_ENABLED_INACTIVE,
            AdapterStateCallback.STATE_CHANGED_REASON_SYSTEM_POLICY);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setUwbEnabled_setToFalse_disablesUwbAndInvokesCallback() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    AdapterStateCallback adapterStateCallback = (AdapterStateCallback) adapterStateCallbackObject;
    manager.registerAdapterStateCallback(directExecutor(), adapterStateCallback);

    Shadow.<ShadowUwbManager>extract(manager).setUwbEnabled(false);

    assertThat(manager.isUwbEnabled()).isFalse();
    verify(adapterStateCallback)
        .onStateChanged(
            AdapterStateCallback.STATE_DISABLED,
            AdapterStateCallback.STATE_CHANGED_REASON_SYSTEM_POLICY);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setUwbEnabled_enabledStateNotChanged_doesNothing() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    AdapterStateCallback adapterStateCallback = (AdapterStateCallback) adapterStateCallbackObject;
    manager.registerAdapterStateCallback(directExecutor(), adapterStateCallback);

    Shadow.<ShadowUwbManager>extract(manager).setUwbEnabled(true);

    assertThat(manager.isUwbEnabled()).isTrue();
    // Invoked once when the callback is initially registered
    verify(adapterStateCallback).onStateChanged(anyInt(), anyInt());
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setUwbEnabled_disabledStateNotChanged_doesNothing() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    AdapterStateCallback adapterStateCallback = (AdapterStateCallback) adapterStateCallbackObject;
    manager.registerAdapterStateCallback(directExecutor(), adapterStateCallback);

    Shadow.<ShadowUwbManager>extract(manager).setUwbEnabled(false);
    Shadow.<ShadowUwbManager>extract(manager).setUwbEnabled(false);

    assertThat(manager.isUwbEnabled()).isFalse();
    // Invoked only once when UWB is initially disabled
    verify(adapterStateCallback)
        .onStateChanged(
            AdapterStateCallback.STATE_DISABLED,
            AdapterStateCallback.STATE_CHANGED_REASON_SYSTEM_POLICY);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void getChipInfos_expectedValue() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    Shadow.<ShadowUwbManager>extract(manager).setChipInfos(ImmutableList.of(genParams("chipInfo")));

    List<PersistableBundle> chipInfos = manager.getChipInfos();
    assertThat(chipInfos).hasSize(1);
    assertThat(getName(chipInfos.get(0))).isEqualTo("chipInfo");
  }

  private static PersistableBundle genParams(String name) {
    PersistableBundle params = new PersistableBundle();
    params.putString("test", name);
    return params;
  }

  private static String getName(PersistableBundle params) {
    return params.getString("test");
  }

  private static ArgumentMatcher<PersistableBundle> checkParams(String name) {
    return params -> getName(params).equals(name);
  }
}
