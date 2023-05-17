package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthNr;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link CellInfoNrBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.Q)
public class CellInfoNrBuilderTest {

  private static final boolean REGISTERED = false;
  private static final long TIMESTAMP_NANOS = 123L;
  private static final long TIMESTAMP_MILLIS = Duration.ofNanos(TIMESTAMP_NANOS).toMillis();
  private static final int CELL_CONNECTION_STATUS = 1;

  private static final CellIdentityNr cellIdentity =
      CellIdentityNrBuilder.newBuilder().setMcc("310").build();
  private static final CellSignalStrengthNr cellSignalStrength =
      CellSignalStrengthNrBuilder.newBuilder().setCsiRsrp(-100).build();

  @Test
  public void build_noArguments() {
    // The intent is to primarily verify that there are no issues setting default values i.e., no
    // exceptions thrown or invalid inputs.
    CellInfoNr cellInfo = CellInfoNrBuilder.newBuilder().build();

    assertThat(cellInfo.getTimeStamp()).isEqualTo(0);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.Q)
  public void build_sdkQ() {
    CellInfoNr cellInfo = getCellInfoNr();

    assertCellInfoFieldsForAllSdks(cellInfo);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.R)
  public void build_fromSdkR() {
    CellInfoNr cellInfo = getCellInfoNr();

    assertCellInfoFieldsForAllSdks(cellInfo);
    assertThat(cellInfo.getTimestampMillis()).isEqualTo(TIMESTAMP_MILLIS);
  }

  /**
   * Assertions on {@link android.telephony.CellInfo} values that are common across all tested SDKs.
   */
  private void assertCellInfoFieldsForAllSdks(CellInfoNr cellInfo) {
    assertThat(cellInfo.isRegistered()).isFalse();
    assertThat(cellInfo.getTimeStamp()).isEqualTo(TIMESTAMP_NANOS);
    assertThat(cellInfo.getCellConnectionStatus()).isEqualTo(CELL_CONNECTION_STATUS);
    assertThat(cellInfo.getCellIdentity()).isEqualTo(cellIdentity);
    assertThat(cellInfo.getCellSignalStrength()).isEqualTo(cellSignalStrength);
  }

  private CellInfoNr getCellInfoNr() {
    return CellInfoNrBuilder.newBuilder()
        .setRegistered(REGISTERED)
        .setTimeStampNanos(TIMESTAMP_NANOS)
        .setCellConnectionStatus(CELL_CONNECTION_STATUS)
        .setCellIdentity(cellIdentity)
        .setCellSignalStrength(cellSignalStrength)
        .build();
  }
}
