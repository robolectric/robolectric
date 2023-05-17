package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthNr;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link CellSignalStrengthNrBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.Q)
public class CellSignalStrengthNrBuilderTest {

  // The platform enforces that some of these values are within a certain range - otherwise, it will
  // default to {@link android.telephony.CellInfo.UNAVAILABLE}.
  private static final int CSI_RSRP = -100;
  private static final int CSI_RSRQ = -10;
  private static final int CSI_SINR = -20;
  private static final int CSI_CQI_TABLE_INDEX = 1;
  private static final ImmutableList<Byte> CSI_CQI_REPORT = ImmutableList.of((byte) 7);
  private static final int SS_RSRP = -140;
  private static final int SS_RSRQ = -15;
  private static final int SS_SINR = -20;
  private static final int TIMING_ADVANCE = 10;

  @Test
  public void build_noArguments() {
    // The intent is to primarily verify that there are no issues setting default values i.e., no
    // exceptions thrown or invalid inputs.
    CellSignalStrengthNr cellSignalStrength = CellSignalStrengthNrBuilder.newBuilder().build();

    assertThat(cellSignalStrength.getCsiRsrp()).isEqualTo(CellInfo.UNAVAILABLE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q, maxSdk = Build.VERSION_CODES.S_V2)
  public void build_sdkQtoS() {
    CellSignalStrengthNr cellSignalStrength = getCellSignalStrength();

    assertCellSignalStrengthFieldsForAllSdks(cellSignalStrength);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.TIRAMISU)
  public void build_fromSdkT() {
    CellSignalStrengthNr cellSignalStrength = getCellSignalStrength();

    assertCellSignalStrengthFieldsForAllSdks(cellSignalStrength);
    assertThat(cellSignalStrength.getCsiCqiTableIndex()).isEqualTo(CSI_CQI_TABLE_INDEX);
    assertThat(cellSignalStrength.getCsiCqiReport()).containsExactly(7);
  }

  /**
   * Assertions on {@link android.telephony.CellSignalStrengthNr} values that are common across all
   * tested SDKs.
   */
  private void assertCellSignalStrengthFieldsForAllSdks(CellSignalStrengthNr cellSignalStrength) {
    assertThat(cellSignalStrength.getCsiRsrp()).isEqualTo(CSI_RSRP);
    assertThat(cellSignalStrength.getCsiRsrq()).isEqualTo(CSI_RSRQ);
    assertThat(cellSignalStrength.getCsiSinr()).isEqualTo(CSI_SINR);
    assertThat(cellSignalStrength.getSsRsrp()).isEqualTo(SS_RSRP);
    assertThat(cellSignalStrength.getSsRsrq()).isEqualTo(SS_RSRQ);
    assertThat(cellSignalStrength.getSsSinr()).isEqualTo(SS_SINR);
  }

  private CellSignalStrengthNr getCellSignalStrength() {
    return CellSignalStrengthNrBuilder.newBuilder()
        .setCsiRsrp(CSI_RSRP)
        .setCsiRsrq(CSI_RSRQ)
        .setCsiSinr(CSI_SINR)
        .setCsiCqiTableIndex(CSI_CQI_TABLE_INDEX)
        .setCsiCqiReport(CSI_CQI_REPORT)
        .setSsRsrp(SS_RSRP)
        .setSsRsrq(SS_RSRQ)
        .setSsSinr(SS_SINR)
        .setTimingAdvance(TIMING_ADVANCE)
        .build();
  }
}
