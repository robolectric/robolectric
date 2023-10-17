package org.robolectric.shadows;

import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_1;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_10;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_11;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_12;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_13;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_14;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_15;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_16;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_17;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_18;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_19;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_2;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_20;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_21;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_22;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_23;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_24;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_25;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_26;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_27;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_28;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_29;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_3;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_30;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_31;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_32;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_4;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_5;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_6;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_7;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_8;
import static android.hardware.radio.network.BarringInfo.SERVICE_TYPE_OPERATOR_9;
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
        && barringServiceType != BARRING_SERVICE_TYPE_SMS
        // OPERATOR_* values are not in the public SDK, but they are defined in the HAL and will be
        // returned on real devices, so we still let them through.
        && barringServiceType != SERVICE_TYPE_OPERATOR_1
        && barringServiceType != SERVICE_TYPE_OPERATOR_2
        && barringServiceType != SERVICE_TYPE_OPERATOR_3
        && barringServiceType != SERVICE_TYPE_OPERATOR_4
        && barringServiceType != SERVICE_TYPE_OPERATOR_5
        && barringServiceType != SERVICE_TYPE_OPERATOR_6
        && barringServiceType != SERVICE_TYPE_OPERATOR_7
        && barringServiceType != SERVICE_TYPE_OPERATOR_8
        && barringServiceType != SERVICE_TYPE_OPERATOR_9
        && barringServiceType != SERVICE_TYPE_OPERATOR_10
        && barringServiceType != SERVICE_TYPE_OPERATOR_11
        && barringServiceType != SERVICE_TYPE_OPERATOR_12
        && barringServiceType != SERVICE_TYPE_OPERATOR_13
        && barringServiceType != SERVICE_TYPE_OPERATOR_14
        && barringServiceType != SERVICE_TYPE_OPERATOR_15
        && barringServiceType != SERVICE_TYPE_OPERATOR_16
        && barringServiceType != SERVICE_TYPE_OPERATOR_17
        && barringServiceType != SERVICE_TYPE_OPERATOR_18
        && barringServiceType != SERVICE_TYPE_OPERATOR_19
        && barringServiceType != SERVICE_TYPE_OPERATOR_20
        && barringServiceType != SERVICE_TYPE_OPERATOR_21
        && barringServiceType != SERVICE_TYPE_OPERATOR_22
        && barringServiceType != SERVICE_TYPE_OPERATOR_23
        && barringServiceType != SERVICE_TYPE_OPERATOR_24
        && barringServiceType != SERVICE_TYPE_OPERATOR_25
        && barringServiceType != SERVICE_TYPE_OPERATOR_26
        && barringServiceType != SERVICE_TYPE_OPERATOR_27
        && barringServiceType != SERVICE_TYPE_OPERATOR_28
        && barringServiceType != SERVICE_TYPE_OPERATOR_29
        && barringServiceType != SERVICE_TYPE_OPERATOR_30
        && barringServiceType != SERVICE_TYPE_OPERATOR_31
        && barringServiceType != SERVICE_TYPE_OPERATOR_32) {
      throw new IllegalArgumentException("Unknown barringServiceType: " + barringServiceType);
    }
  }
}
