package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellIdentityGsm}. */
@RequiresApi(Build.VERSION_CODES.Q)
public class CellIdentityGsmBuilder {

  private int lac = CellInfo.UNAVAILABLE;
  private int cid = CellInfo.UNAVAILABLE;
  private int bsic = CellInfo.UNAVAILABLE;
  private int arfcn = CellInfo.UNAVAILABLE;
  private List<String> additionalPlmns = new ArrayList<>();
  @Nullable private String mccStr = null;
  @Nullable private String mncStr = null;
  @Nullable private String alphal = null;
  @Nullable private String alphas = null;

  private CellIdentityGsmBuilder() {}

  public static CellIdentityGsmBuilder newBuilder() {
    return new CellIdentityGsmBuilder();
  }

  // An empty constructor is not available on Q.
  @RequiresApi(Build.VERSION_CODES.R)
  protected static CellIdentityGsm getDefaultInstance() {
    return reflector(CellIdentityGsmReflector.class).newCellIdentityGsm();
  }

  public CellIdentityGsmBuilder setLac(int lac) {
    this.lac = lac;
    return this;
  }

  public CellIdentityGsmBuilder setCid(int cid) {
    this.cid = cid;
    return this;
  }

  public CellIdentityGsmBuilder setBsic(int bsic) {
    this.bsic = bsic;
    return this;
  }

  public CellIdentityGsmBuilder setArfcn(int arfcn) {
    this.arfcn = arfcn;
    return this;
  }

  public CellIdentityGsmBuilder setMcc(String mcc) {
    this.mccStr = mcc;
    return this;
  }

  public CellIdentityGsmBuilder setMnc(String mnc) {
    this.mncStr = mnc;
    return this;
  }

  public CellIdentityGsmBuilder setOperatorAlphaLong(String operatorAlphaLong) {
    this.alphal = operatorAlphaLong;
    return this;
  }

  public CellIdentityGsmBuilder setOperatorAlphaShort(String operatorAlphaShort) {
    this.alphas = operatorAlphaShort;
    return this;
  }

  public CellIdentityGsmBuilder setAdditionalPlmns(List<String> additionalPlmns) {
    this.additionalPlmns = additionalPlmns;
    return this;
  }

  public CellIdentityGsm build() {
    CellIdentityGsmReflector cellIdentityReflector = reflector(CellIdentityGsmReflector.class);
    if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.Q) {
      throw new UnsupportedOperationException("Not supported on API level < Q");
    } else if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.R) {
      return cellIdentityReflector.newCellIdentityGsm(
          lac, cid, arfcn, bsic, mccStr, mncStr, alphal, alphas);
    } else {

      return cellIdentityReflector.newCellIdentityGsm(
          lac, cid, arfcn, bsic, mccStr, mncStr, alphal, alphas, additionalPlmns);
    }
  }

  @ForType(CellIdentityGsm.class)
  private interface CellIdentityGsmReflector {

    @Constructor
    CellIdentityGsm newCellIdentityGsm();

    @Constructor
    CellIdentityGsm newCellIdentityGsm(
        int lac,
        int cid,
        int arfcn,
        int bsic,
        String mcc,
        String mnc,
        String alphal,
        String alphas);

    @Constructor
    CellIdentityGsm newCellIdentityGsm(
        int lac,
        int cid,
        int arfcn,
        int bsic,
        String mcc,
        String mnc,
        String alphal,
        String alphas,
        Collection<String> additionalPlmns);
  }
}
