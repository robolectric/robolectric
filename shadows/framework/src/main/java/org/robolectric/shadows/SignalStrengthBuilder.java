package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellSignalStrengthTdscdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.SignalStrength;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.SignalStrength} */
public class SignalStrengthBuilder {

  private ImmutableList<CellSignalStrength> cellSignalStrengths = ImmutableList.of();

  private SignalStrengthBuilder() {}

  public static SignalStrengthBuilder newBuilder() {
    return new SignalStrengthBuilder();
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  public SignalStrengthBuilder setCellSignalStrengths(
      List<CellSignalStrength> cellSignalStrengths) {
    this.cellSignalStrengths = ImmutableList.copyOf(cellSignalStrengths);
    return this;
  }

  public SignalStrength build() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    SignalStrength signalStrength = reflector(SignalStrengthReflector.class).newSignalStrength();
    SignalStrengthReflector signalStrengthReflector =
        reflector(SignalStrengthReflector.class, signalStrength);

    if (apiLevel > Build.VERSION_CODES.P) {
      for (CellSignalStrength cellSignalStrength : cellSignalStrengths) {
        if (cellSignalStrength instanceof CellSignalStrengthCdma) {
          signalStrengthReflector.setCellSignalStrengthCdma(
              (CellSignalStrengthCdma) cellSignalStrength);
        } else if (cellSignalStrength instanceof CellSignalStrengthGsm) {
          signalStrengthReflector.setCellSignalStrengthGsm(
              (CellSignalStrengthGsm) cellSignalStrength);
        } else if (cellSignalStrength instanceof CellSignalStrengthWcdma) {
          signalStrengthReflector.setCellSignalStrengthWcdma(
              (CellSignalStrengthWcdma) cellSignalStrength);
        } else if (cellSignalStrength instanceof CellSignalStrengthTdscdma) {
          signalStrengthReflector.setCellSignalStrengthTdscdma(
              (CellSignalStrengthTdscdma) cellSignalStrength);
        } else if (cellSignalStrength instanceof CellSignalStrengthLte) {
          signalStrengthReflector.setCellSignalStrengthLte(
              (CellSignalStrengthLte) cellSignalStrength);
        } else if (cellSignalStrength instanceof CellSignalStrengthNr) {
          signalStrengthReflector.setCellSignalStrengthNr(
              (CellSignalStrengthNr) cellSignalStrength);
        } else {
          throw new IllegalArgumentException(
              "Unsupported CellSignalStrength type: " + cellSignalStrength.getClass().getName());
        }
      }
    }

    return signalStrength;
  }

  @ForType(SignalStrength.class)
  private interface SignalStrengthReflector {
    @Constructor
    SignalStrength newSignalStrength();

    @Accessor("mCdma")
    void setCellSignalStrengthCdma(CellSignalStrengthCdma cellSignalStrength);

    @Accessor("mGsm")
    void setCellSignalStrengthGsm(CellSignalStrengthGsm cellSignalStrength);

    @Accessor("mWcdma")
    void setCellSignalStrengthWcdma(CellSignalStrengthWcdma cellSignalStrength);

    @Accessor("mTdscdma")
    void setCellSignalStrengthTdscdma(CellSignalStrengthTdscdma cellSignalStrength);

    @Accessor("mLte")
    void setCellSignalStrengthLte(CellSignalStrengthLte cellSignalStrength);

    @Accessor("mNr")
    void setCellSignalStrengthNr(CellSignalStrengthNr cellSignalStrength);
  }
}
