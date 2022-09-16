package org.robolectric.shadows;

import static android.content.Context.DEVICE_POLICY_SERVICE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyResourcesManager;
import android.os.Build.VERSION_CODES;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for {@link ShadowDevicePolicyManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.TIRAMISU)
public class ShadowDevicePolicyResourcesManagerTest {

  private DevicePolicyResourcesManager devicePolicyResourcesManager;
  private ShadowDevicePolicyResourcesManager shadowDevicePolicyResourcesManager;

  @Before
  public void setUp() {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) getApplicationContext().getSystemService(DEVICE_POLICY_SERVICE);
    devicePolicyResourcesManager = devicePolicyManager.getResources();
    shadowDevicePolicyResourcesManager = Shadow.extract(devicePolicyResourcesManager);
  }

  @Test
  public void getString_returnsUserSetString() {
    shadowDevicePolicyResourcesManager.setString("stringId", "value");
    assertThat(devicePolicyResourcesManager.getString("stringId", () -> "default"))
        .isEqualTo("value");
  }

  @Test
  public void getString_whenStringSetThenUnset_doesntReturnIt() {
    shadowDevicePolicyResourcesManager.setString("stringId", "value");
    shadowDevicePolicyResourcesManager.setString("stringId", null);
    assertThat(devicePolicyResourcesManager.getString("stringId", () -> "default"))
        .isNotEqualTo("value");
  }
}
