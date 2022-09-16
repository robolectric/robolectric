package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION_CODES;
import android.telephony.UiccPortInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.TIRAMISU)
public final class UiccPortInfoBuilderTest {

  @Test
  public void buildUiccPortInfo() {
    UiccPortInfo portInfo =
        UiccPortInfoBuilder.newBuilder()
            .setIccId("sample_iccid")
            .setPortIndex(1)
            .setLogicalSlotIndex(1)
            .setIsActive(true)
            .build();

    assertThat(portInfo).isNotNull();
    assertThat(portInfo.getIccId()).isEqualTo("sample_iccid");
    assertThat(portInfo.getPortIndex()).isEqualTo(1);
    assertThat(portInfo.getLogicalSlotIndex()).isEqualTo(1);
    assertThat(portInfo.isActive()).isTrue();
  }
}
