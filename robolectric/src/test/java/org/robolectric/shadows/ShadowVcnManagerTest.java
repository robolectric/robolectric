package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadow.api.Shadow.extract;

import android.net.vcn.VcnConfig;
import android.net.vcn.VcnManager;
import android.net.vcn.VcnManager.VcnStatusCallback;
import android.os.ParcelUuid;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowVcnManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = S)
public final class ShadowVcnManagerTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();
  private ShadowVcnManager instance;
  private final ParcelUuid subGroup = ParcelUuid.fromString("00000000-0000-0000-0000-000000000000");
  private final Executor executor = MoreExecutors.directExecutor();
  @Mock private VcnStatusCallback callback;
  private VcnConfig vcnConfig;

  @Before
  public void setUp() {
    instance =
        extract(ApplicationProvider.getApplicationContext().getSystemService(VcnManager.class));
  }

  @Test
  public void registerVcnStatusCallback_callbackRegistered() {
    instance.registerVcnStatusCallback(subGroup, executor, callback);

    assertThat(instance.getRegisteredVcnStatusCallbacks()).contains(callback);
    assertThat(instance.getRegisteredSubscriptionGroup(callback)).isEqualTo(subGroup);
  }

  @Test
  public void setStatus_callbackOnStatusChanged() {
    instance.registerVcnStatusCallback(subGroup, executor, callback);
    instance.setStatus(VcnManager.VCN_STATUS_CODE_ACTIVE);

    verify(callback).onStatusChanged(VcnManager.VCN_STATUS_CODE_ACTIVE);
  }

  @Test
  public void unregisterVcnStatusCallback_callbackNotInSet() {
    instance.registerVcnStatusCallback(subGroup, executor, callback);
    instance.unregisterVcnStatusCallback(callback);

    assertThat(instance.getRegisteredVcnStatusCallbacks()).doesNotContain(callback);
  }

  @Test
  public void setGatewayConnectionError_firesCallback() {
    String gatewayConnectionName = "gateway_connection";
    int errorCode = VcnManager.VCN_ERROR_CODE_INTERNAL_ERROR;

    instance.registerVcnStatusCallback(subGroup, executor, callback);
    instance.setGatewayConnectionError(gatewayConnectionName, errorCode, null);

    verify(callback).onGatewayConnectionError(gatewayConnectionName, errorCode, null);
  }

  @Test
  public void setVcnConfig_configInSet() {
    instance.setVcnConfig(subGroup, vcnConfig);

    assertThat(instance.getConfiguredSubscriptionGroups()).containsExactly(subGroup);
  }

  @Test
  public void clearVcnConfig_configNotInSet() {
    instance.setVcnConfig(subGroup, vcnConfig);
    instance.clearVcnConfig(subGroup);

    assertThat(instance.getConfiguredSubscriptionGroups()).isEmpty();
  }
}
