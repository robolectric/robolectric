package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellIdentityNr}. */
@RequiresApi(Build.VERSION_CODES.Q)
public class CellIdentityNrBuilder {

  private int pci = CellInfo.UNAVAILABLE;
  private int tac = CellInfo.UNAVAILABLE;
  private int nrarfcn = CellInfo.UNAVAILABLE;
  private int[] bands = new int[0];
  @Nullable private String mcc = null;
  @Nullable private String mnc = null;
  private long nci = CellInfo.UNAVAILABLE;
  @Nullable private String alphal = null;
  @Nullable private String alphas = null;
  private List<String> additionalPlmns = new ArrayList<>();

  private CellIdentityNrBuilder() {}

  public static CellIdentityNrBuilder newBuilder() {
    return new CellIdentityNrBuilder();
  }

  // An empty constructor is not available on Q.
  @RequiresApi(Build.VERSION_CODES.R)
  protected static CellIdentityNr getDefaultInstance() {
    return reflector(CellIdentityNrReflector.class).newCellIdentityNr();
  }

  public CellIdentityNrBuilder setNci(long nci) {
    this.nci = nci;
    return this;
  }

  public CellIdentityNrBuilder setPci(int pci) {
    this.pci = pci;
    return this;
  }

  public CellIdentityNrBuilder setTac(int tac) {
    this.tac = tac;
    return this;
  }

  public CellIdentityNrBuilder setNrarfcn(int nrarfcn) {
    this.nrarfcn = nrarfcn;
    return this;
  }

  public CellIdentityNrBuilder setMcc(String mcc) {
    this.mcc = mcc;
    return this;
  }

  public CellIdentityNrBuilder setMnc(String mnc) {
    this.mnc = mnc;
    return this;
  }

  public CellIdentityNrBuilder setBands(int[] bands) {
    this.bands = bands;
    return this;
  }

  public CellIdentityNrBuilder setLongOperatorName(String longOperatorName) {
    this.alphal = longOperatorName;
    return this;
  }

  public CellIdentityNrBuilder setShortOperatorName(String shortOperatorName) {
    this.alphas = shortOperatorName;
    return this;
  }

  public CellIdentityNrBuilder setAdditionalPlmns(List<String> additionalPlmns) {
    this.additionalPlmns = additionalPlmns;
    return this;
  }

  public CellIdentityNr build() {
    CellIdentityNrReflector cellIdentityReflector = reflector(CellIdentityNrReflector.class);
    if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.R) {
      return cellIdentityReflector.newCellIdentityNr(
          pci, tac, nrarfcn, mcc, mnc, nci, alphal, alphas);
    } else {
      return cellIdentityReflector.newCellIdentityNr(
          pci, tac, nrarfcn, bands, mcc, mnc, nci, alphal, alphas, additionalPlmns);
    }
  }

  @ForType(CellIdentityNr.class)
  private interface CellIdentityNrReflector {

    @Constructor
    CellIdentityNr newCellIdentityNr();

    @Constructor
    CellIdentityNr newCellIdentityNr(
        int pci,
        int tac,
        int nrarfcn,
        String mcc,
        String mnc,
        long nci,
        String alphal,
        String alphas);

    @Constructor
    CellIdentityNr newCellIdentityNr(
        int pci,
        int tac,
        int nrarfcn,
        int[] bands,
        String mcc,
        String mnc,
        long nci,
        String alphal,
        String alphas,
        Collection<String> additionalPlmns);
  }
}
