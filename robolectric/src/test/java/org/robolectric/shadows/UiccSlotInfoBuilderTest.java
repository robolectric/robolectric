package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.telephony.UiccSlotInfo.CARD_STATE_INFO_PRESENT;
import static com.google.common.truth.Truth.assertThat;

import android.telephony.UiccPortInfo;
import android.telephony.UiccSlotInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public class UiccSlotInfoBuilderTest {
  @Test
  public void buildUiccSlotInfo() {
    UiccSlotInfo slotInfo =
        UiccSlotInfoBuilder.newBuilder()
            .setCardStateInfo(CARD_STATE_INFO_PRESENT)
            .setCardId("cardId")
            .setIsEuicc(true)
            .setIsRemovable(true)
            .setIsExtendedApduSupported(true)
            .addPort("iccId", 1, 1, true)
            .build();

    assertThat(slotInfo).isNotNull();
    assertThat(slotInfo.getCardId()).isEqualTo("cardId");
    assertThat(slotInfo.getIsEuicc()).isTrue();
    assertThat(slotInfo.getIsExtendedApduSupported()).isEqualTo(true);
    assertThat(slotInfo.getCardStateInfo()).isEqualTo(CARD_STATE_INFO_PRESENT);
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      assertThat(slotInfo.isRemovable()).isEqualTo(true);
    }
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void buildUiccSlotInfo_ports() {
    UiccSlotInfo slotInfo =
        UiccSlotInfoBuilder.newBuilder()
            .setCardStateInfo(CARD_STATE_INFO_PRESENT)
            .setCardId("cardId")
            .setIsEuicc(true)
            .setIsRemovable(true)
            .setIsExtendedApduSupported(true)
            .addPort("iccId", 1, 1, true)
            .build();
    assertThat(slotInfo).isNotNull();
    assertThat(slotInfo.getCardId()).isEqualTo("cardId");
    assertThat(slotInfo.getIsEuicc()).isTrue();
    assertThat(slotInfo.getIsExtendedApduSupported()).isEqualTo(true);
    assertThat(slotInfo.getCardStateInfo()).isEqualTo(CARD_STATE_INFO_PRESENT);
    assertThat(slotInfo.getPorts()).hasSize(1);
    UiccPortInfo portInfo = slotInfo.getPorts().stream().findFirst().get();
    assertThat(portInfo.getIccId()).isEqualTo("iccId");
    assertThat(portInfo.getPortIndex()).isEqualTo(1);
    assertThat(portInfo.getLogicalSlotIndex()).isEqualTo(1);
    assertThat(portInfo.isActive()).isEqualTo(true);
  }
}
