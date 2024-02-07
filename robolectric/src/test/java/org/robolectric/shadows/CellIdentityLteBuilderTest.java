package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link CellIdentityLteBuilder} */
@RunWith(AndroidJUnit4.class)
public class CellIdentityLteBuilderTest {

  private static final String MCC = "310";
  private static final String MNC = "260";
  private static final int CI = 0;
  private static final int PCI = 1;
  private static final int TAC = 2;
  private static final int EARFCN = 4;
  private static final int[] BANDS = new int[] {2, 4};
  private static final int BANDWIDTH = 5;
  private static final String SHORT_OPERATOR_NAME = "short operator name";
  private static final String LONG_OPERATOR_NAME = "long operator name";
  private static final ImmutableList<String> ADDITIONAL_PLMNS = ImmutableList.of("310240");

  @Test
  public void build_noArguments() {
    // The intent is to primarily verify that there are no issues setting default values i.e., no
    // exceptions thrown or invalid inputs.
    CellIdentityLte cellIdentity = CellIdentityLteBuilder.newBuilder().build();

    assertThat(cellIdentity.getCi()).isEqualTo(CellInfo.UNAVAILABLE);
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.M)
  public void build_sdkJtoM() {
    CellIdentityLte cellIdentity = getCellIdentityLte();

    assertCellIdentityFieldsForAllSdks(cellIdentity);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N, maxSdk = Build.VERSION_CODES.O_MR1)
  public void build_sdkNtoO() {
    CellIdentityLte cellIdentity = getCellIdentityLte();

    assertCellIdentityFieldsForAllSdks(cellIdentity);
    assertThat(cellIdentity.getEarfcn()).isEqualTo(EARFCN);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q, maxSdk = Build.VERSION_CODES.R)
  public void build_sdkPtoQ() {
    CellIdentityLte cellIdentity = getCellIdentityLte();

    assertCellIdentityFieldsForAllSdks(cellIdentity);
    assertThat(cellIdentity.getMccString()).isEqualTo(MCC);
    assertThat(cellIdentity.getMncString()).isEqualTo(MNC);
    assertThat(cellIdentity.getEarfcn()).isEqualTo(EARFCN);
    assertThat(cellIdentity.getBandwidth()).isEqualTo(BANDWIDTH);
    assertThat(cellIdentity.getOperatorAlphaLong().toString()).isEqualTo(LONG_OPERATOR_NAME);
    assertThat(cellIdentity.getOperatorAlphaShort().toString()).isEqualTo(SHORT_OPERATOR_NAME);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.S)
  public void build_fromSdkS() {
    CellIdentityLte cellIdentity = getCellIdentityLte();

    assertCellIdentityFieldsForAllSdks(cellIdentity);
    assertThat(cellIdentity.getMccString()).isEqualTo(MCC);
    assertThat(cellIdentity.getMncString()).isEqualTo(MNC);
    assertThat(cellIdentity.getEarfcn()).isEqualTo(EARFCN);
    assertThat(cellIdentity.getBandwidth()).isEqualTo(BANDWIDTH);
    assertThat(cellIdentity.getBands()).isEqualTo(BANDS);
    assertThat(cellIdentity.getOperatorAlphaLong().toString()).isEqualTo(LONG_OPERATOR_NAME);
    assertThat(cellIdentity.getOperatorAlphaShort().toString()).isEqualTo(SHORT_OPERATOR_NAME);
    assertThat(cellIdentity.getAdditionalPlmns()).containsExactlyElementsIn(ADDITIONAL_PLMNS);
  }

  /**
   * Assertions on {@link android.telephony.CellIdentityLte} values that are common across all
   * tested SDKs.
   */
  private void assertCellIdentityFieldsForAllSdks(CellIdentityLte cellIdentity) {
    assertThat(cellIdentity.getMcc()).isEqualTo(Integer.parseInt(MCC));
    assertThat(cellIdentity.getMnc()).isEqualTo(Integer.parseInt(MNC));
    assertThat(cellIdentity.getCi()).isEqualTo(CI);
    assertThat(cellIdentity.getPci()).isEqualTo(PCI);
    assertThat(cellIdentity.getTac()).isEqualTo(TAC);
  }

  private CellIdentityLte getCellIdentityLte() {
    return CellIdentityLteBuilder.newBuilder()
        .setMcc(MCC)
        .setMnc(MNC)
        .setCi(CI)
        .setPci(PCI)
        .setTac(TAC)
        .setEarfcn(EARFCN)
        .setBands(BANDS)
        .setBandwidth(BANDWIDTH)
        .setLongOperatorName(LONG_OPERATOR_NAME)
        .setShortOperatorName(SHORT_OPERATOR_NAME)
        .setAdditionalPlmns(ADDITIONAL_PLMNS)
        .build();
  }
}
