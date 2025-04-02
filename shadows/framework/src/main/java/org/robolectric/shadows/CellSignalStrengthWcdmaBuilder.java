package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthWcdma;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellSignalStrengthWcdma} */
@RequiresApi(Build.VERSION_CODES.Q)
public class CellSignalStrengthWcdmaBuilder {

  private int rssi = CellInfo.UNAVAILABLE;
  private int ber = CellInfo.UNAVAILABLE;
  private int rscp = CellInfo.UNAVAILABLE;
  private int ecno = CellInfo.UNAVAILABLE;

  private CellSignalStrengthWcdmaBuilder() {}

  public static CellSignalStrengthWcdmaBuilder newBuilder() {
    return new CellSignalStrengthWcdmaBuilder();
  }

  protected static CellSignalStrengthWcdma getDefaultInstance() {
    return reflector(CellSignalStrengthWcdmaReflector.class).newCellSignalStrength();
  }

  /** This is equivalent to {@code signalStrength} pre SDK Q. */
  public CellSignalStrengthWcdmaBuilder setRssi(int rssi) {
    this.rssi = rssi;
    return this;
  }

  public CellSignalStrengthWcdmaBuilder setBer(int ber) {
    this.ber = ber;
    return this;
  }

  public CellSignalStrengthWcdmaBuilder setRscp(int rscp) {
    this.rscp = rscp;
    return this;
  }

  public CellSignalStrengthWcdmaBuilder setEcno(int ecno) {
    this.ecno = ecno;
    return this;
  }

  public CellSignalStrengthWcdma build() {
    return reflector(CellSignalStrengthWcdmaReflector.class)
        .newCellSignalStrength(rssi, ber, rscp, ecno);
  }

  @ForType(CellSignalStrengthWcdma.class)
  private interface CellSignalStrengthWcdmaReflector {
    @Constructor
    CellSignalStrengthWcdma newCellSignalStrength();

    @Constructor
    CellSignalStrengthWcdma newCellSignalStrength(int rssi, int ber, int rscp, int ecno);
  }
}
