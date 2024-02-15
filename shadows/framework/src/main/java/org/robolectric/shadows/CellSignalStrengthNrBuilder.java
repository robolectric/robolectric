package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthNr;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellSignalStrengthNr} */
@RequiresApi(Build.VERSION_CODES.Q)
public class CellSignalStrengthNrBuilder {

  private int csiRrsp = CellInfo.UNAVAILABLE;
  private int csiRsrq = CellInfo.UNAVAILABLE;
  private int csiSinr = CellInfo.UNAVAILABLE;
  private int csiCqiTableIndex = CellInfo.UNAVAILABLE;
  private List<Byte> csiCqiReport = new ArrayList<>();
  private int ssRsrp = CellInfo.UNAVAILABLE;
  private int ssRsrq = CellInfo.UNAVAILABLE;
  private int ssSinr = CellInfo.UNAVAILABLE;
  private int timingAdvance = CellInfo.UNAVAILABLE;

  private CellSignalStrengthNrBuilder() {}

  public static CellSignalStrengthNrBuilder newBuilder() {
    return new CellSignalStrengthNrBuilder();
  }

  protected static CellSignalStrengthNr getDefaultInstance() {
    return reflector(CellSignalStrengthNrReflector.class).newCellSignalStrengthNr();
  }

  public CellSignalStrengthNrBuilder setCsiRsrp(int csiRrsp) {
    this.csiRrsp = csiRrsp;
    return this;
  }

  public CellSignalStrengthNrBuilder setCsiRsrq(int csiRsrq) {
    this.csiRsrq = csiRsrq;
    return this;
  }

  public CellSignalStrengthNrBuilder setCsiSinr(int csiSinr) {
    this.csiSinr = csiSinr;
    return this;
  }

  public CellSignalStrengthNrBuilder setCsiCqiTableIndex(int csiCqiTableIndex) {
    this.csiCqiTableIndex = csiCqiTableIndex;
    return this;
  }

  public CellSignalStrengthNrBuilder setCsiCqiReport(List<Byte> csiCqiReport) {
    this.csiCqiReport = csiCqiReport;
    return this;
  }

  public CellSignalStrengthNrBuilder setSsRsrp(int ssRsrp) {
    this.ssRsrp = ssRsrp;
    return this;
  }

  public CellSignalStrengthNrBuilder setSsRsrq(int ssRsrq) {
    this.ssRsrq = ssRsrq;
    return this;
  }

  public CellSignalStrengthNrBuilder setSsSinr(int ssSinr) {
    this.ssSinr = ssSinr;
    return this;
  }

  public CellSignalStrengthNrBuilder setTimingAdvance(int timingAdvance) {
    this.timingAdvance = timingAdvance;
    return this;
  }

  public CellSignalStrengthNr build() {
    CellSignalStrengthNrReflector cellSignalStrengthReflector =
        reflector(CellSignalStrengthNrReflector.class);
    if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.TIRAMISU) {
      return cellSignalStrengthReflector.newCellSignalStrengthNr(
          csiRrsp, csiRsrq, csiSinr, ssRsrp, ssRsrq, ssSinr);
    } else if (RuntimeEnvironment.getApiLevel() == Build.VERSION_CODES.TIRAMISU) {
      return cellSignalStrengthReflector.newCellSignalStrengthNr(
          csiRrsp, csiRsrq, csiSinr, csiCqiTableIndex, csiCqiReport, ssRsrp, ssRsrq, ssSinr);
    } else {
      return cellSignalStrengthReflector.newCellSignalStrengthNr(
          csiRrsp,
          csiRsrq,
          csiSinr,
          csiCqiTableIndex,
          csiCqiReport,
          ssRsrp,
          ssRsrq,
          ssSinr,
          timingAdvance);
    }
  }

  @ForType(CellSignalStrengthNr.class)
  private interface CellSignalStrengthNrReflector {

    @Constructor
    CellSignalStrengthNr newCellSignalStrengthNr();

    @Constructor
    CellSignalStrengthNr newCellSignalStrengthNr(
        int csRsrp, int csiRsrq, int csiSinr, int ssRsrp, int ssRsrq, int ssSinr);

    @Constructor
    CellSignalStrengthNr newCellSignalStrengthNr(
        int csRsrp,
        int csiRsrq,
        int csiSinr,
        int csiCqiTableIndex,
        List<Byte> csiCqiReport,
        int ssRsrp,
        int ssRsrq,
        int ssSinr);

    @Constructor
    CellSignalStrengthNr newCellSignalStrengthNr(
        int csRsrp,
        int csiRsrq,
        int csiSinr,
        int csiCqiTableIndex,
        List<Byte> csiCqiReport,
        int ssRsrp,
        int ssRsrq,
        int ssSinr,
        int timingAdvance);
  }
}
