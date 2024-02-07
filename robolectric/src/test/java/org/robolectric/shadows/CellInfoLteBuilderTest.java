package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link CellInfoLteBuilder} */
@RunWith(AndroidJUnit4.class)
public class CellInfoLteBuilderTest {

  private static final boolean REGISTERED = false;
  private static final long TIMESTAMP_NANOS = 123L;
  private static final long TIMESTAMP_MILLIS = Duration.ofNanos(TIMESTAMP_NANOS).toMillis();
  private static final int CELL_CONNECTION_STATUS = 1;

  private static final CellIdentityLte cellIdentity =
      CellIdentityLteBuilder.newBuilder().setMcc("310").build();
  private static final CellSignalStrengthLte cellSignalStrength =
      CellSignalStrengthLteBuilder.newBuilder().setRsrp(-120).build();

  @Test
  public void build_noArguments() {
    // The intent is to primarily verify that there are no issues setting default values i.e., no
    // exceptions thrown or invalid inputs.
    CellInfoLte cellInfo = CellInfoLteBuilder.newBuilder().build();

    assertThat(cellInfo.getTimeStamp()).isEqualTo(0);
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.N_MR1)
  public void build_sdkJtoN() {
    CellInfoLte cellInfo = getCellInfoLte();

    assertThat(cellInfo.isRegistered()).isFalse();
    assertThat(cellInfo.getTimeStamp()).isEqualTo(TIMESTAMP_NANOS);
    assertThat(cellInfo.getCellSignalStrength()).isEqualTo(cellSignalStrength);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P, maxSdk = Build.VERSION_CODES.Q)
  public void build_fromSdkPtoQ() {
    CellInfoLte cellInfo = getCellInfoLte();

    assertThat(cellInfo.isRegistered()).isFalse();
    assertThat(cellInfo.getTimeStamp()).isEqualTo(TIMESTAMP_NANOS);
    assertThat(cellInfo.getCellConnectionStatus()).isEqualTo(CELL_CONNECTION_STATUS);
    assertThat(cellInfo.getCellSignalStrength()).isEqualTo(cellSignalStrength);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.R, maxSdk = Config.NEWEST_SDK)
  public void build_fromSdkR() {
    CellInfoLte cellInfo = getCellInfoLte();

    assertThat(cellInfo.isRegistered()).isFalse();
    assertThat(cellInfo.getTimestampMillis()).isEqualTo(TIMESTAMP_MILLIS);
    assertThat(cellInfo.getCellConnectionStatus()).isEqualTo(CELL_CONNECTION_STATUS);
    assertThat(cellInfo.getCellSignalStrength()).isEqualTo(cellSignalStrength);
    assertThat(cellInfo.getCellIdentity()).isEqualTo(cellIdentity);
  }

  private CellInfoLte getCellInfoLte() {
    return CellInfoLteBuilder.newBuilder()
        .setRegistered(REGISTERED)
        .setTimeStampNanos(TIMESTAMP_NANOS)
        .setCellConnectionStatus(CELL_CONNECTION_STATUS)
        .setCellIdentity(cellIdentity)
        .setCellSignalStrength(cellSignalStrength)
        .build();
  }
}
