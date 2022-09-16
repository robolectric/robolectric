package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.os.Build.VERSION_CODES;
import android.telephony.UiccCardInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.Q)
public final class UiccCardInfoBuilderTest {

  @Test
  @Config(maxSdk = VERSION_CODES.S_V2)
  public void buildUiccCardInfo_sdkQtoT() {
    UiccCardInfo cardInfo =
        UiccCardInfoBuilder.newBuilder()
            .setIsEuicc(true)
            .setCardId(5)
            .setEid("sample_eid")
            .setIccId("sample_iccid")
            .setSlotIndex(1)
            .setIsRemovable(true)
            .build();

    assertThat(cardInfo).isNotNull();
    assertThat(cardInfo.isEuicc()).isTrue();
    assertThat(cardInfo.getCardId()).isEqualTo(5);
    assertThat(cardInfo.getEid()).isEqualTo("sample_eid");
    assertThat(cardInfo.getIccId()).isEqualTo("sample_iccid");
    assertThat(cardInfo.getSlotIndex()).isEqualTo(1);
    assertThat(cardInfo.isRemovable()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void buildUiccCardInfo_fromSdkT() {
    UiccCardInfo cardInfo =
        UiccCardInfoBuilder.newBuilder()
            .setCardId(5)
            .setIsEuicc(true)
            .setEid("sample_eid")
            .setPhysicalSlotIndex(1)
            .setIsRemovable(true)
            .setIsMultipleEnabledProfilesSupported(true)
            .setPorts(new ArrayList<>())
            .build();

    assertThat(cardInfo).isNotNull();
    assertThat(cardInfo.isEuicc()).isTrue();
    assertThat(cardInfo.getCardId()).isEqualTo(5);
    assertThat(cardInfo.getEid()).isEqualTo("sample_eid");
    assertThat(cardInfo.getPhysicalSlotIndex()).isEqualTo(1);
    assertThat(cardInfo.isRemovable()).isTrue();
    assertThat(cardInfo.isMultipleEnabledProfilesSupported()).isTrue();
    assertThat(cardInfo.getPorts()).isEmpty();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void buildUiccCardInfo_nullPorts_fromSdkT() {
    UiccCardInfo cardInfo =
        UiccCardInfoBuilder.newBuilder()
            .setCardId(5)
            .setIsEuicc(true)
            .setEid("sample_eid")
            .setPhysicalSlotIndex(1)
            .setIsRemovable(true)
            .setIsMultipleEnabledProfilesSupported(true)
            .setPorts(null)
            .build();

    assertThrows(NullPointerException.class, cardInfo::getPorts);
  }
}
