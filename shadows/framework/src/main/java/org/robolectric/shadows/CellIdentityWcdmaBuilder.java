package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.ClosedSubscriberGroupInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellIdentityWcdma}. */
@RequiresApi(Build.VERSION_CODES.Q)
public class CellIdentityWcdmaBuilder {

  private int lac = CellInfo.UNAVAILABLE;
  private int cid = CellInfo.UNAVAILABLE;
  private int psc = CellInfo.UNAVAILABLE;
  private int uarfcn = CellInfo.UNAVAILABLE;
  private List<String> additionalPlmns = new ArrayList<>();
  @Nullable private String mccStr = null;
  @Nullable private String mncStr = null;
  @Nullable private String alphal = null;
  @Nullable private String alphas = null;
  @Nullable private ClosedSubscriberGroupInfo csgInfo = null;

  private CellIdentityWcdmaBuilder() {}

  public static CellIdentityWcdmaBuilder newBuilder() {
    return new CellIdentityWcdmaBuilder();
  }

  // An empty constructor is not available on Q.
  @RequiresApi(Build.VERSION_CODES.R)
  protected static CellIdentityWcdma getDefaultInstance() {
    return reflector(CellIdentityWcdmaReflector.class).newCellIdentityWcdma();
  }

  public CellIdentityWcdmaBuilder setLac(int lac) {
    this.lac = lac;
    return this;
  }

  public CellIdentityWcdmaBuilder setCid(int cid) {
    this.cid = cid;
    return this;
  }

  public CellIdentityWcdmaBuilder setPsc(int psc) {
    this.psc = psc;
    return this;
  }

  public CellIdentityWcdmaBuilder setUarfcn(int uarfcn) {
    this.uarfcn = uarfcn;
    return this;
  }

  public CellIdentityWcdmaBuilder setMcc(String mcc) {
    this.mccStr = mcc;
    return this;
  }

  public CellIdentityWcdmaBuilder setMnc(String mnc) {
    this.mncStr = mnc;
    return this;
  }

  public CellIdentityWcdmaBuilder setOperatorAlphaLong(String operatorAlphaLong) {
    this.alphal = operatorAlphaLong;
    return this;
  }

  public CellIdentityWcdmaBuilder setOperatorAlphaShort(String operatorAlphaShort) {
    this.alphas = operatorAlphaShort;
    return this;
  }

  public CellIdentityWcdmaBuilder setAdditionalPlmns(List<String> additionalPlmns) {
    this.additionalPlmns = additionalPlmns;
    return this;
  }

  public CellIdentityWcdmaBuilder setCsgInfo(ClosedSubscriberGroupInfo csgInfo) {
    this.csgInfo = csgInfo;
    return this;
  }

  public CellIdentityWcdma build() {
    CellIdentityWcdmaReflector cellIdentityReflector = reflector(CellIdentityWcdmaReflector.class);
    if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.Q) {
      throw new UnsupportedOperationException("Not supported on API level < Q");
    } else if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.R) {
      return cellIdentityReflector.newCellIdentityWcdma(
          lac, cid, psc, uarfcn, mccStr, mncStr, alphal, alphas);
    } else {
      return cellIdentityReflector.newCellIdentityWcdma(
          lac, cid, psc, uarfcn, mccStr, mncStr, alphal, alphas, additionalPlmns, csgInfo);
    }
  }

  @ForType(CellIdentityWcdma.class)
  private interface CellIdentityWcdmaReflector {

    @Constructor
    CellIdentityWcdma newCellIdentityWcdma();

    @Constructor
    CellIdentityWcdma newCellIdentityWcdma(
        int lac,
        int cid,
        int psc,
        int uarfcn,
        String mcc,
        String mnc,
        String alphal,
        String alphas);

    @Constructor
    CellIdentityWcdma newCellIdentityWcdma(
        int lac,
        int cid,
        int psc,
        int uarfcn,
        String mcc,
        String mnc,
        String alphal,
        String alphas,
        Collection<String> additionalPlmns,
        ClosedSubscriberGroupInfo csgInfo);
  }
}
