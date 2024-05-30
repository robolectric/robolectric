package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

/** Builder for {@link android.telephony.CellInfoLte}. */
public class CellInfoLteBuilder {

  private boolean isRegistered = false;
  private long timeStamp = 0L;
  private int cellConnectionStatus = 0;
  private CellIdentityLte cellIdentity;
  private CellSignalStrengthLte cellSignalStrength;

  private CellInfoLteBuilder() {}

  public static CellInfoLteBuilder newBuilder() {
    return new CellInfoLteBuilder();
  }

  public CellInfoLteBuilder setRegistered(boolean isRegistered) {
    this.isRegistered = isRegistered;
    return this;
  }

  public CellInfoLteBuilder setTimeStampNanos(long timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  public CellInfoLteBuilder setCellConnectionStatus(int cellConnectionStatus) {
    this.cellConnectionStatus = cellConnectionStatus;
    return this;
  }

  public CellInfoLteBuilder setCellIdentity(CellIdentityLte cellIdentity) {
    this.cellIdentity = cellIdentity;
    return this;
  }

  public CellInfoLteBuilder setCellSignalStrength(CellSignalStrengthLte cellSignalStrength) {
    this.cellSignalStrength = cellSignalStrength;
    return this;
  }

  public CellInfoLte build() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (cellIdentity == null) {
      if (apiLevel > Build.VERSION_CODES.Q) {
        cellIdentity = CellIdentityLteBuilder.getDefaultInstance();
      } else {
        cellIdentity = CellIdentityLteBuilder.newBuilder().build();
      }
    }
    if (cellSignalStrength == null) {
      cellSignalStrength = CellSignalStrengthLteBuilder.getDefaultInstance();
    }
    CellInfoLteReflector cellInfoLteReflector = reflector(CellInfoLteReflector.class);
    if (apiLevel < Build.VERSION_CODES.TIRAMISU) {
      CellInfoLte cellInfo = cellInfoLteReflector.newCellInfoLte();
      cellInfoLteReflector = reflector(CellInfoLteReflector.class, cellInfo);
      cellInfoLteReflector.setCellIdentity(cellIdentity);
      cellInfoLteReflector.setCellSignalStrength(cellSignalStrength);
      CellInfoReflector cellInfoReflector = reflector(CellInfoReflector.class, cellInfo);
      cellInfoReflector.setTimeStamp(timeStamp);
      cellInfoReflector.setRegistered(isRegistered);
      if (apiLevel > Build.VERSION_CODES.O_MR1) {
        cellInfoReflector.setCellConnectionStatus(cellConnectionStatus);
      }
      return cellInfo;
    } else {
      try {
        // This reflection is highly brittle but there is currently no choice as CellConfigLte is
        // entirely @hide.
        Class cellConfigLteClass = Class.forName("android.telephony.CellConfigLte");
        return cellInfoLteReflector.newCellInfoLte(
            cellConnectionStatus,
            isRegistered,
            timeStamp,
            cellIdentity,
            cellSignalStrength,
            ReflectionHelpers.callConstructor(cellConfigLteClass));
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @ForType(CellInfoLte.class)
  private interface CellInfoLteReflector {
    @Constructor
    CellInfoLte newCellInfoLte();

    @Constructor
    CellInfoLte newCellInfoLte(
        int cellConnectionStatus,
        boolean isRegistered,
        long timeStamp,
        CellIdentityLte cellIdentity,
        CellSignalStrengthLte cellSignalStrength,
        @WithType("android.telephony.CellConfigLte") Object cellConfigLte);

    @Accessor("mCellIdentityLte")
    void setCellIdentity(CellIdentityLte cellIdentity);

    @Accessor("mCellSignalStrengthLte")
    void setCellSignalStrength(CellSignalStrengthLte cellSignalStrength);
  }

  @ForType(CellInfo.class)
  private interface CellInfoReflector {

    @Accessor("mRegistered")
    void setRegistered(boolean registered);

    @Accessor("mTimeStamp")
    void setTimeStamp(long registered);

    @Accessor("mCellConnectionStatus")
    void setCellConnectionStatus(int cellConnectionStatus);
  }
}
