package org.robolectric.shadows;

import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_CS_FALLBACK;
import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_CS_SERVICE;
import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_CS_VOICE;
import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_EMERGENCY;
import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_MMTEL_VIDEO;
import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_MMTEL_VOICE;
import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_MO_DATA;
import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_MO_SIGNALLING;
import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_PS_SERVICE;
import static android.telephony.BarringInfo.BARRING_SERVICE_TYPE_SMS;
import static android.telephony.BarringInfo.BarringServiceInfo.BARRING_TYPE_CONDITIONAL;
import static android.telephony.BarringInfo.BarringServiceInfo.BARRING_TYPE_NONE;
import static android.telephony.BarringInfo.BarringServiceInfo.BARRING_TYPE_UNCONDITIONAL;
import static android.telephony.BarringInfo.BarringServiceInfo.BARRING_TYPE_UNKNOWN;

import android.os.Build.VERSION_CODES;
import android.telephony.BarringInfo;
import android.telephony.BarringInfo.BarringServiceInfo;
import android.telephony.CellIdentity;
import android.util.SparseArray;
import androidx.annotation.RequiresApi;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link BarringInfo}. */
@RequiresApi(VERSION_CODES.R)
public class BarringInfoBuilder {

  private CellIdentity barringCellIdentity;
  private final SparseArray<BarringServiceInfo> barringServiceInfos = new SparseArray<>();

  private BarringInfoBuilder() {}

  public static BarringInfoBuilder newBuilder() {
    return new BarringInfoBuilder();
  }

  @CanIgnoreReturnValue
  public BarringInfoBuilder setCellIdentity(CellIdentity cellIdentity) {
    this.barringCellIdentity = cellIdentity;
    return this;
  }

  @CanIgnoreReturnValue
  public BarringInfoBuilder addBarringServiceInfo(
      int barringServiceType, BarringServiceInfo barringServiceInfo) {
    validateBarringServiceType(barringServiceType);
    barringServiceInfos.put(barringServiceType, barringServiceInfo);
    return this;
  }

  public BarringInfo build() {
    return ReflectionHelpers.callConstructor(
        BarringInfo.class,
        ClassParameter.from(CellIdentity.class, barringCellIdentity),
        ClassParameter.from(SparseArray.class, barringServiceInfos));
  }

  /** Builder for {@link BarringServiceInfo}. */
  public static class BarringServiceInfoBuilder {
    private int barringType = BARRING_TYPE_NONE;
    private boolean isConditionallyBarred;
    private int conditionalBarringFactor;
    private int conditionalBarringTimeSeconds;

    private BarringServiceInfoBuilder() {}

    public static BarringServiceInfoBuilder newBuilder() {
      return new BarringServiceInfoBuilder();
    }

    @CanIgnoreReturnValue
    public BarringServiceInfoBuilder setBarringType(int barringType) {
      validateBarringType(barringType);
      this.barringType = barringType;
      return this;
    }

    @CanIgnoreReturnValue
    public BarringServiceInfoBuilder setIsConditionallyBarred(boolean isConditionallyBarred) {
      this.isConditionallyBarred = isConditionallyBarred;
      return this;
    }

    @CanIgnoreReturnValue
    public BarringServiceInfoBuilder setConditionalBarringFactor(int conditionalBarringFactor) {
      this.conditionalBarringFactor = conditionalBarringFactor;
      return this;
    }

    @CanIgnoreReturnValue
    public BarringServiceInfoBuilder setConditionalBarringTimeSeconds(
        int conditionalBarringTimeSeconds) {
      this.conditionalBarringTimeSeconds = conditionalBarringTimeSeconds;
      return this;
    }

    public BarringServiceInfo build() {
      return ReflectionHelpers.callConstructor(
          BarringServiceInfo.class,
          ClassParameter.from(int.class, barringType),
          ClassParameter.from(boolean.class, isConditionallyBarred),
          ClassParameter.from(int.class, conditionalBarringFactor),
          ClassParameter.from(int.class, conditionalBarringTimeSeconds));
    }

    private void validateBarringType(int barringType) {
      if (barringType != BARRING_TYPE_NONE
          && barringType != BARRING_TYPE_UNCONDITIONAL
          && barringType != BARRING_TYPE_CONDITIONAL
          && barringType != BARRING_TYPE_UNKNOWN) {
        throw new IllegalArgumentException("Unknown barringType: " + barringType);
      }
    }
  }

  private void validateBarringServiceType(int barringServiceType) {
    if (barringServiceType != BARRING_SERVICE_TYPE_CS_SERVICE
        && barringServiceType != BARRING_SERVICE_TYPE_PS_SERVICE
        && barringServiceType != BARRING_SERVICE_TYPE_CS_VOICE
        && barringServiceType != BARRING_SERVICE_TYPE_MO_SIGNALLING
        && barringServiceType != BARRING_SERVICE_TYPE_MO_DATA
        && barringServiceType != BARRING_SERVICE_TYPE_CS_FALLBACK
        && barringServiceType != BARRING_SERVICE_TYPE_MMTEL_VOICE
        && barringServiceType != BARRING_SERVICE_TYPE_MMTEL_VIDEO
        && barringServiceType != BARRING_SERVICE_TYPE_EMERGENCY
        && barringServiceType != BARRING_SERVICE_TYPE_SMS) {
      throw new IllegalArgumentException("Unknown barringServiceType: " + barringServiceType);
    }
  }
}
