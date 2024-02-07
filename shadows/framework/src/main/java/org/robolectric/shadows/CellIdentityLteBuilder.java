package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.ClosedSubscriberGroupInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellIdentityLte}. */
public class CellIdentityLteBuilder {

  @Nullable private String mcc = null;
  @Nullable private String mnc = null;
  private int ci = CellInfo.UNAVAILABLE;
  private int pci = CellInfo.UNAVAILABLE;
  private int tac = CellInfo.UNAVAILABLE;
  private int earfcn = CellInfo.UNAVAILABLE;
  private int[] bands = new int[0];
  private int bandwidth = CellInfo.UNAVAILABLE;
  @Nullable private String alphal = null;
  @Nullable private String alphas = null;
  private List<String> additionalPlmns = new ArrayList<>();

  private CellIdentityLteBuilder() {}

  public static CellIdentityLteBuilder newBuilder() {
    return new CellIdentityLteBuilder();
  }

  protected static CellIdentityLte getDefaultInstance() {
    return reflector(CellIdentityLteReflector.class).newCellIdentityLte();
  }

  public CellIdentityLteBuilder setMcc(String mcc) {
    this.mcc = mcc;
    return this;
  }

  public CellIdentityLteBuilder setMnc(String mnc) {
    this.mnc = mnc;
    return this;
  }

  public CellIdentityLteBuilder setCi(int ci) {
    this.ci = ci;
    return this;
  }

  public CellIdentityLteBuilder setPci(int pci) {
    this.pci = pci;
    return this;
  }

  public CellIdentityLteBuilder setTac(int tac) {
    this.tac = tac;
    return this;
  }

  public CellIdentityLteBuilder setEarfcn(int earfcn) {
    this.earfcn = earfcn;
    return this;
  }

  public CellIdentityLteBuilder setBands(int[] bands) {
    this.bands = bands;
    return this;
  }

  public CellIdentityLteBuilder setBandwidth(int bandwidth) {
    this.bandwidth = bandwidth;
    return this;
  }

  public CellIdentityLteBuilder setLongOperatorName(String longOperatorName) {
    this.alphal = longOperatorName;
    return this;
  }

  public CellIdentityLteBuilder setShortOperatorName(String shortOperatorName) {
    this.alphas = shortOperatorName;
    return this;
  }

  public CellIdentityLteBuilder setAdditionalPlmns(List<String> additionalPlmns) {
    this.additionalPlmns = additionalPlmns;
    return this;
  }

  public CellIdentityLte build() {
    CellIdentityLteReflector cellIdentityLteReflector = reflector(CellIdentityLteReflector.class);
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel < Build.VERSION_CODES.N) {
      return cellIdentityLteReflector.newCellIdentityLte(
          mccOrMncToInt(mcc), mccOrMncToInt(mnc), ci, pci, tac);
    } else if (apiLevel < Build.VERSION_CODES.P) {
      return cellIdentityLteReflector.newCellIdentityLte(
          mccOrMncToInt(mcc), mccOrMncToInt(mnc), ci, pci, tac, earfcn);
    } else if (apiLevel < Build.VERSION_CODES.R) {
      return cellIdentityLteReflector.newCellIdentityLte(
          ci, pci, tac, earfcn, bandwidth, mcc, mnc, alphal, alphas);
    } else {
      return cellIdentityLteReflector.newCellIdentityLte(
          ci,
          pci,
          tac,
          earfcn,
          bands,
          bandwidth,
          mcc,
          mnc,
          alphal,
          alphas,
          additionalPlmns,
          /* csgInfo= */ null);
    }
  }

  private static int mccOrMncToInt(@Nullable String mccOrMnc) {
    return mccOrMnc == null ? CellInfo.UNAVAILABLE : Integer.parseInt(mccOrMnc);
  }

  @ForType(CellIdentityLte.class)
  private interface CellIdentityLteReflector {
    @Constructor
    CellIdentityLte newCellIdentityLte();

    @Constructor
    CellIdentityLte newCellIdentityLte(int mcc, int mnc, int ci, int pci, int tac);

    @Constructor
    CellIdentityLte newCellIdentityLte(int mcc, int mnc, int ci, int pci, int tac, int earfcn);

    @Constructor
    CellIdentityLte newCellIdentityLte(
        int ci,
        int pci,
        int tac,
        int earfcn,
        int bandwidth,
        String mcc,
        String mnc,
        String alphal,
        String alphas);

    @Constructor
    CellIdentityLte newCellIdentityLte(
        int ci,
        int pci,
        int tac,
        int earfcn,
        int[] bands,
        int bandwidth,
        String mcc,
        String mnc,
        String alphal,
        String alphas,
        Collection<String> additionalPlmns,
        ClosedSubscriberGroupInfo csgInfo);
  }
}
