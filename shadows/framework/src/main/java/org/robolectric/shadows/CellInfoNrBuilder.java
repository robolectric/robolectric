package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.os.Parcel;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthNr;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellInfoNr}. */
@RequiresApi(Build.VERSION_CODES.Q)
public class CellInfoNrBuilder {

  private boolean isRegistered = false;
  private long timeStamp = 0L;
  private int cellConnectionStatus = 0;
  private CellIdentityNr cellIdentity;
  private CellSignalStrengthNr cellSignalStrength;

  private CellInfoNrBuilder() {}

  public static CellInfoNrBuilder newBuilder() {
    return new CellInfoNrBuilder();
  }

  public CellInfoNrBuilder setRegistered(boolean isRegistered) {
    this.isRegistered = isRegistered;
    return this;
  }

  public CellInfoNrBuilder setTimeStampNanos(long timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  public CellInfoNrBuilder setCellConnectionStatus(int cellConnectionStatus) {
    this.cellConnectionStatus = cellConnectionStatus;
    return this;
  }

  public CellInfoNrBuilder setCellIdentity(CellIdentityNr cellIdentity) {
    this.cellIdentity = cellIdentity;
    return this;
  }

  public CellInfoNrBuilder setCellSignalStrength(CellSignalStrengthNr cellSignalStrength) {
    this.cellSignalStrength = cellSignalStrength;
    return this;
  }

  public CellInfoNr build() {
    if (cellIdentity == null) {
      cellIdentity = CellIdentityNrBuilder.getDefaultInstance();
    }
    if (cellSignalStrength == null) {
      cellSignalStrength = CellSignalStrengthNrBuilder.getDefaultInstance();
    }
    // CellInfoNr has no default constructor below T so we write it to a Parcel.
    if (RuntimeEnvironment.getApiLevel() <= Build.VERSION_CODES.TIRAMISU) {
      Parcel p = Parcel.obtain();
      p.writeInt(/* CellInfo#TYPE_NR */ 6);
      p.writeInt(isRegistered ? 1 : 0);
      p.writeLong(timeStamp);
      p.writeInt(cellConnectionStatus);
      cellIdentity.writeToParcel(p, 0);
      cellSignalStrength.writeToParcel(p, 0);
      p.setDataPosition(0);
      CellInfoNr cellInfoNr = CellInfoNr.CREATOR.createFromParcel(p);
      p.recycle();
      return cellInfoNr;
    } else {
      return reflector(CellInfoNrReflector.class)
          .newCellInfoNr(
              cellConnectionStatus, isRegistered, timeStamp, cellIdentity, cellSignalStrength);
    }
  }

  @ForType(CellInfoNr.class)
  private interface CellInfoNrReflector {
    @Constructor
    CellInfoNr newCellInfoNr(
        int cellConnectionStatus,
        boolean isRegistered,
        long timeStamp,
        CellIdentityNr cellIdentity,
        CellSignalStrengthNr cellSignalStrength);
  }
}
