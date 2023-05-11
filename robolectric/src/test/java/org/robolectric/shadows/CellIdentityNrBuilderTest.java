package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telephony.AccessNetworkConstants;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link CellIdentityNrBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.Q)
public class CellIdentityNrBuilderTest {

  private static final int PCI = 1;
  private static final int TAC = 2;
  private static final int NRARFCN = 4;
  private static final int[] BANDS =
      new int[] {
        AccessNetworkConstants.NgranBands.BAND_1, AccessNetworkConstants.NgranBands.BAND_2
      };
  private static final String MCC = "310";
  private static final String MNC = "260";
  private static final int NCI = 0;
  private static final String LONG_OPERATOR_NAME = "long operator name";
  private static final String SHORT_OPERATOR_NAME = "short operator name";
  private static final ImmutableList<String> ADDITIONAL_PLMNS = ImmutableList.of("310240");

  @Test
  public void build_noArguments() {
    // The intent is to primarily verify that there are no issues setting default values i.e., no
    // exceptions thrown or invalid inputs.
    CellIdentityNr cellIdentity = CellIdentityNrBuilder.newBuilder().build();

    assertThat(cellIdentity.getPci()).isEqualTo(CellInfo.UNAVAILABLE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q, maxSdk = Build.VERSION_CODES.R)
  public void build_sdkQtoR() {
    CellIdentityNr cellIdentity = getCellIdentityNr();

    assertCellIdentityFieldsForAllSdks(cellIdentity);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.S)
  public void build_fromSdkS() {
    CellIdentityNr cellIdentity = getCellIdentityNr();

    assertCellIdentityFieldsForAllSdks(cellIdentity);
    assertThat(cellIdentity.getBands()).isEqualTo(BANDS);
    assertThat(cellIdentity.getAdditionalPlmns()).containsExactlyElementsIn(ADDITIONAL_PLMNS);
  }

  /**
   * Assertions on {@link android.telephony.CellIdentityNr} values that are common across all tested
   * SDKs.
   */
  private void assertCellIdentityFieldsForAllSdks(CellIdentityNr cellIdentity) {
    assertThat(cellIdentity.getPci()).isEqualTo(PCI);
    assertThat(cellIdentity.getTac()).isEqualTo(TAC);
    assertThat(cellIdentity.getNrarfcn()).isEqualTo(NRARFCN);
    assertThat(cellIdentity.getMccString()).isEqualTo(MCC);
    assertThat(cellIdentity.getMncString()).isEqualTo(MNC);
    assertThat(cellIdentity.getNci()).isEqualTo(NCI);
    assertThat(cellIdentity.getOperatorAlphaLong().toString()).isEqualTo(LONG_OPERATOR_NAME);
    assertThat(cellIdentity.getOperatorAlphaShort().toString()).isEqualTo(SHORT_OPERATOR_NAME);
  }

  private CellIdentityNr getCellIdentityNr() {
    return CellIdentityNrBuilder.newBuilder()
        .setPci(PCI)
        .setTac(TAC)
        .setNrarfcn(NRARFCN)
        .setBands(BANDS)
        .setMcc(MCC)
        .setMnc(MNC)
        .setNci(NCI)
        .setLongOperatorName(LONG_OPERATOR_NAME)
        .setShortOperatorName(SHORT_OPERATOR_NAME)
        .setAdditionalPlmns(ADDITIONAL_PLMNS)
        .build();
  }
}
