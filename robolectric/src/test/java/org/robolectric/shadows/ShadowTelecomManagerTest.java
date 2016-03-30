package org.robolectric.shadows;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.LOLLIPOP,
    Build.VERSION_CODES.LOLLIPOP_MR1,
    Build.VERSION_CODES.M})
public class ShadowTelecomManagerTest {

  private TelecomManager telecomService;

  @Before
  public void setUp() {
    telecomService = (TelecomManager) RuntimeEnvironment.application.getSystemService(Context.TELECOM_SERVICE);
  }

  @Test
  public void getSimCallManager() {
    PhoneAccountHandle handle =
        new PhoneAccountHandle(new ComponentName(RuntimeEnvironment.application, "component_class_name"), "id");

    shadowOf(telecomService).setSimCallManager(handle);

    assertThat(telecomService.getConnectionManager().getId()).isEqualTo("id");
  }
}
