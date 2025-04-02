package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.os.Parcel;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellInfoWdcma}. */
@RequiresApi(Build.VERSION_CODES.Q)
public class CellInfoGsmBuilder {

  private boolean isRegistered = false;
  private long timeStamp = 0L;
  private int cellConnectionStatus = 0;
  private CellIdentityGsm cellIdentity;
  private CellSignalStrengthGsm cellSignalStrength;

  private CellInfoGsmBuilder() {}

  public static CellInfoGsmBuilder newBuilder() {
    return new CellInfoGsmBuilder();
  }

  public CellInfoGsmBuilder setRegistered(boolean isRegistered) {
    this.isRegistered = isRegistered;
    return this;
  }

  public CellInfoGsmBuilder setTimeStampNanos(long timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  public CellInfoGsmBuilder setCellConnectionStatus(int cellConnectionStatus) {
    this.cellConnectionStatus = cellConnectionStatus;
    return this;
  }

  public CellInfoGsmBuilder setCellIdentity(CellIdentityGsm cellIdentity) {
    this.cellIdentity = cellIdentity;
    return this;
  }

  public CellInfoGsmBuilder setCellSignalStrength(CellSignalStrengthGsm cellSignalStrength) {
    this.cellSignalStrength = cellSignalStrength;
    return this;
  }

  public CellInfoGsm build() {
    if (cellIdentity == null) {
      cellIdentity = CellIdentityGsmBuilder.getDefaultInstance();
    }
    if (cellSignalStrength == null) {
      cellSignalStrength = CellSignalStrengthGsmBuilder.getDefaultInstance();
    }
    // CellInfoGsm has no default constructor below T so we write it to a Parcel.
    if (RuntimeEnvironment.getApiLevel() <= Build.VERSION_CODES.TIRAMISU) {
      Parcel p = Parcel.obtain();
      p.writeInt(CellInfo.TYPE_GSM);
      p.writeInt(isRegistered ? 1 : 0);
      p.writeLong(timeStamp);
      p.writeInt(cellConnectionStatus);
      cellIdentity.writeToParcel(p, 0);
      cellSignalStrength.writeToParcel(p, 0);
      p.setDataPosition(0);
      CellInfoGsm cellInfoGsm = CellInfoGsm.CREATOR.createFromParcel(p);
      p.recycle();
      return cellInfoGsm;
    } else {
      return reflector(CellInfoGsmReflector.class)
          .newCellInfoGsm(
              cellConnectionStatus, isRegistered, timeStamp, cellIdentity, cellSignalStrength);
    }
  }

  @ForType(CellInfoGsm.class)
  private interface CellInfoGsmReflector {
    @Constructor
    CellInfoGsm newCellInfoGsm(
        int cellConnectionStatus,
        boolean isRegistered,
        long timeStamp,
        CellIdentityGsm cellIdentity,
        CellSignalStrengthGsm cellSignalStrength);
  }
}
