package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthGsm;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellSignalStrengthGsm} */
@RequiresApi(Build.VERSION_CODES.Q)
public class CellSignalStrengthGsmBuilder {

  private int rssi = CellInfo.UNAVAILABLE;
  private int ber = CellInfo.UNAVAILABLE;
  private int timingAdvance = CellInfo.UNAVAILABLE;

  private CellSignalStrengthGsmBuilder() {}

  public static CellSignalStrengthGsmBuilder newBuilder() {
    return new CellSignalStrengthGsmBuilder();
  }

  protected static CellSignalStrengthGsm getDefaultInstance() {
    return reflector(CellSignalStrengthGsmReflector.class).newCellSignalStrength();
  }

  /** This is equivalent to {@code signalStrength} pre SDK Q. */
  public CellSignalStrengthGsmBuilder setRssi(int rssi) {
    this.rssi = rssi;
    return this;
  }

  public CellSignalStrengthGsmBuilder setBer(int ber) {
    this.ber = ber;
    return this;
  }

  public CellSignalStrengthGsmBuilder setTimingAdvance(int timingAdvance) {
    this.timingAdvance = timingAdvance;
    return this;
  }

  public CellSignalStrengthGsm build() {
    return reflector(CellSignalStrengthGsmReflector.class)
        .newCellSignalStrength(rssi, ber, timingAdvance);
  }

  @ForType(CellSignalStrengthGsm.class)
  private interface CellSignalStrengthGsmReflector {
    @Constructor
    CellSignalStrengthGsm newCellSignalStrength();

    @Constructor
    CellSignalStrengthGsm newCellSignalStrength(int rssi, int ber, int timingAdvance);
  }
}
