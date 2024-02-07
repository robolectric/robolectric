package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthLte;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link CellSignalStrengthLteBuilder} */
@RunWith(AndroidJUnit4.class)
public class CellSignalStrengthLteBuilderTest {

  // The platform enforces that some of these values are within a certain range - otherwise, it will
  // default to {@link android.telephony.CellInfo.UNAVAILABLE}.
  private static final int RSSI = -100;
  private static final int RSRP = -120;
  private static final int RSRQ = -10;
  private static final int RSSNR = 30;
  private static final int CQI_TABLE_INDEX = 4;
  private static final int CQI = 5;
  private static final int TIMING_ADVANCE = 6;

  @Test
  public void build_noArguments() {
    // The intent is to primarily verify that there are no issues setting default values i.e., no
    // exceptions thrown or invalid inputs.
    CellSignalStrengthLte cellSignalStrength = CellSignalStrengthLteBuilder.newBuilder().build();

    assertThat(cellSignalStrength.getTimingAdvance()).isEqualTo(CellInfo.UNAVAILABLE);
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.N_MR1)
  public void build_sdkJtoN() {
    CellSignalStrengthLte cellSignalStrength = getCellSignalStrength();

    assertThat(cellSignalStrength.getTimingAdvance()).isEqualTo(TIMING_ADVANCE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O, maxSdk = Build.VERSION_CODES.P)
  public void build_sdkOToP() {
    CellSignalStrengthLte cellSignalStrength = getCellSignalStrength();

    assertThat(cellSignalStrength.getRsrp()).isEqualTo(RSRP);
    assertThat(cellSignalStrength.getRssnr()).isEqualTo(RSSNR);
    assertThat(cellSignalStrength.getRsrq()).isEqualTo(RSRQ);
    assertThat(cellSignalStrength.getCqi()).isEqualTo(CQI);
    assertThat(cellSignalStrength.getTimingAdvance()).isEqualTo(TIMING_ADVANCE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q, maxSdk = Build.VERSION_CODES.R)
  public void build_sdkQtoR() {
    CellSignalStrengthLte cellSignalStrength = getCellSignalStrength();

    assertThat(cellSignalStrength.getRssi()).isEqualTo(RSSI);
    assertThat(cellSignalStrength.getRsrp()).isEqualTo(RSRP);
    assertThat(cellSignalStrength.getRsrq()).isEqualTo(RSRQ);
    assertThat(cellSignalStrength.getRssnr()).isEqualTo(RSSNR);
    assertThat(cellSignalStrength.getCqi()).isEqualTo(CQI);
    assertThat(cellSignalStrength.getTimingAdvance()).isEqualTo(TIMING_ADVANCE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.S)
  public void build_fromSdkS() {
    CellSignalStrengthLte cellSignalStrength = getCellSignalStrength();

    assertThat(cellSignalStrength.getRssi()).isEqualTo(RSSI);
    assertThat(cellSignalStrength.getRsrp()).isEqualTo(RSRP);
    assertThat(cellSignalStrength.getRsrq()).isEqualTo(RSRQ);
    assertThat(cellSignalStrength.getRssnr()).isEqualTo(RSSNR);
    assertThat(cellSignalStrength.getCqiTableIndex()).isEqualTo(CQI_TABLE_INDEX);
    assertThat(cellSignalStrength.getCqi()).isEqualTo(CQI);
    assertThat(cellSignalStrength.getTimingAdvance()).isEqualTo(TIMING_ADVANCE);
  }

  private CellSignalStrengthLte getCellSignalStrength() {
    return CellSignalStrengthLteBuilder.newBuilder()
        .setRssi(RSSI)
        .setRsrp(RSRP)
        .setRsrq(RSRQ)
        .setRssnr(RSSNR)
        .setCqi(CQI)
        .setCqiTableIndex(CQI_TABLE_INDEX)
        .setTimingAdvance(TIMING_ADVANCE)
        .build();
  }
}
