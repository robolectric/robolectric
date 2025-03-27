package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.os.Build;
import android.os.Parcel;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthWcdma;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telephony.CellInfoWdcma}. */
@RequiresApi(Build.VERSION_CODES.Q)
public class CellInfoWcdmaBuilder {

  private boolean isRegistered = false;
  private long timeStamp = 0L;
  private int cellConnectionStatus = 0;
  private CellIdentityWcdma cellIdentity;
  private CellSignalStrengthWcdma cellSignalStrength;

  private CellInfoWcdmaBuilder() {}

  public static CellInfoWcdmaBuilder newBuilder() {
    return new CellInfoWcdmaBuilder();
  }

  public CellInfoWcdmaBuilder setRegistered(boolean isRegistered) {
    this.isRegistered = isRegistered;
    return this;
  }

  public CellInfoWcdmaBuilder setTimeStampNanos(long timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  public CellInfoWcdmaBuilder setCellConnectionStatus(int cellConnectionStatus) {
    this.cellConnectionStatus = cellConnectionStatus;
    return this;
  }

  public CellInfoWcdmaBuilder setCellIdentity(CellIdentityWcdma cellIdentity) {
    this.cellIdentity = cellIdentity;
    return this;
  }

  public CellInfoWcdmaBuilder setCellSignalStrength(CellSignalStrengthWcdma cellSignalStrength) {
    this.cellSignalStrength = cellSignalStrength;
    return this;
  }

  public CellInfoWcdma build() {
    if (cellIdentity == null) {
      cellIdentity = CellIdentityWcdmaBuilder.getDefaultInstance();
    }
    if (cellSignalStrength == null) {
      cellSignalStrength = CellSignalStrengthWcdmaBuilder.getDefaultInstance();
    }
    // CellInfoWcdma has no default constructor below T so we write it to a Parcel.
    if (RuntimeEnvironment.getApiLevel() <= Build.VERSION_CODES.TIRAMISU) {
      Parcel p = Parcel.obtain();
      p.writeInt(/* CellInfo#TYPE_WCDMA */ 4);
      p.writeInt(isRegistered ? 1 : 0);
      p.writeLong(timeStamp);
      p.writeInt(cellConnectionStatus);
      cellIdentity.writeToParcel(p, 0);
      cellSignalStrength.writeToParcel(p, 0);
      p.setDataPosition(0);
      CellInfoWcdma cellInfoWcdma = CellInfoWcdma.CREATOR.createFromParcel(p);
      p.recycle();
      return cellInfoWcdma;
    } else {
      return reflector(CellInfoWcdmaReflector.class)
          .newCellInfoWcdma(
              cellConnectionStatus, isRegistered, timeStamp, cellIdentity, cellSignalStrength);
    }
  }

  @ForType(CellInfoWcdma.class)
  private interface CellInfoWcdmaReflector {
    @Constructor
    CellInfoWcdma newCellInfoWcdma(
        int cellConnectionStatus,
        boolean isRegistered,
        long timeStamp,
        CellIdentityWcdma cellIdentity,
        CellSignalStrengthWcdma cellSignalStrength);
  }
}
