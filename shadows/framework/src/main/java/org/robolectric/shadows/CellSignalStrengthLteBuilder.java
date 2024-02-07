package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthLte;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellSignalStrengthLte} */
public class CellSignalStrengthLteBuilder {

  private int rssi = CellInfo.UNAVAILABLE;
  private int rsrp = CellInfo.UNAVAILABLE;
  private int rsrq = CellInfo.UNAVAILABLE;
  private int rssnr = CellInfo.UNAVAILABLE;
  private int cqiTableIndex = CellInfo.UNAVAILABLE;
  private int cqi = CellInfo.UNAVAILABLE;
  private int timingAdvance = CellInfo.UNAVAILABLE;

  private CellSignalStrengthLteBuilder() {}

  public static CellSignalStrengthLteBuilder newBuilder() {
    return new CellSignalStrengthLteBuilder();
  }

  protected static CellSignalStrengthLte getDefaultInstance() {
    return reflector(CellSignalStrengthLteReflector.class).newCellSignalStrength();
  }

  /** This is equivalent to {@code signalStrength} pre SDK Q. */
  public CellSignalStrengthLteBuilder setRssi(int rssi) {
    this.rssi = rssi;
    return this;
  }

  public CellSignalStrengthLteBuilder setRsrp(int rsrp) {
    this.rsrp = rsrp;
    return this;
  }

  public CellSignalStrengthLteBuilder setRsrq(int rsrq) {
    this.rsrq = rsrq;
    return this;
  }

  public CellSignalStrengthLteBuilder setRssnr(int rssnr) {
    this.rssnr = rssnr;
    return this;
  }

  public CellSignalStrengthLteBuilder setCqiTableIndex(int cqiTableIndex) {
    this.cqiTableIndex = cqiTableIndex;
    return this;
  }

  public CellSignalStrengthLteBuilder setCqi(int cqi) {
    this.cqi = cqi;
    return this;
  }

  public CellSignalStrengthLteBuilder setTimingAdvance(int timingAdvance) {
    this.timingAdvance = timingAdvance;
    return this;
  }

  public CellSignalStrengthLte build() {
    CellSignalStrengthLteReflector cellSignalStrengthReflector =
        reflector(CellSignalStrengthLteReflector.class);
    if (RuntimeEnvironment.getApiLevel() < Build.VERSION_CODES.S) {
      return cellSignalStrengthReflector.newCellSignalStrength(
          rssi, rsrp, rsrq, rssnr, cqi, timingAdvance);
    } else {
      return cellSignalStrengthReflector.newCellSignalStrength(
          rssi, rsrp, rsrq, rssnr, cqiTableIndex, cqi, timingAdvance);
    }
  }

  @ForType(CellSignalStrengthLte.class)
  private interface CellSignalStrengthLteReflector {
    @Constructor
    CellSignalStrengthLte newCellSignalStrength();

    @Constructor
    CellSignalStrengthLte newCellSignalStrength(
        int rssi, int rsrp, int rsrq, int rssnr, int cqi, int timingAdvance);

    @Constructor
    CellSignalStrengthLte newCellSignalStrength(
        int rssi, int rsrp, int rsrq, int rssnr, int cqiTableIndex, int cqi, int timingAdvance);
  }
}
