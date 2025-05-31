package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.SignalStrength;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link SignalStrengthBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Config.OLDEST_SDK)
public class SignalStrengthBuilderTest {

  @Test
  @Config(maxSdk = Build.VERSION_CODES.P)
  public void build_noArguments_toSdkP() {
    SignalStrength signalStrength = SignalStrengthBuilder.newBuilder().build();

    assertThat(signalStrength.getCdmaDbm()).isEqualTo(-1);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void build_noArguments_fromSdkQ() {
    SignalStrength signalStrength = SignalStrengthBuilder.newBuilder().build();

    assertThat(signalStrength.getCdmaDbm()).isEqualTo(SignalStrength.INVALID);
  }

  @Test
  @Config(sdk = {Build.VERSION_CODES.Q, Config.NEWEST_SDK})
  public void build_setCellSignalStrengths() {
    CellSignalStrengthGsm cellSignalStrengthGsm =
        CellSignalStrengthGsmBuilder.newBuilder().setRssi(-100).build();
    CellSignalStrengthWcdma cellSignalStrengthWcdma =
        CellSignalStrengthWcdmaBuilder.newBuilder().setRssi(-100).build();
    CellSignalStrengthLte cellSignalStrengthLte =
        CellSignalStrengthLteBuilder.newBuilder().setRsrp(-100).build();
    CellSignalStrengthNr cellSignalStrengthNr =
        CellSignalStrengthNrBuilder.newBuilder().setSsRsrp(-100).build();
    ImmutableList<CellSignalStrength> cellSignalStrengths =
        ImmutableList.of(
            cellSignalStrengthGsm,
            cellSignalStrengthWcdma,
            cellSignalStrengthLte,
            cellSignalStrengthNr);

    // CellSignalStrengthCdma & CellSignalStrengthTdscdma will use the default instance which will
    // be considered invalid and thus won't be returned by the method.
    SignalStrength signalStrength =
        SignalStrengthBuilder.newBuilder().setCellSignalStrengths(cellSignalStrengths).build();

    assertThat(signalStrength.getCellSignalStrengths())
        .containsExactlyElementsIn(cellSignalStrengths);
  }
}
