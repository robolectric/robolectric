package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.telephony.UiccCardInfo;
import android.telephony.UiccPortInfo;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Builder for {@link UiccCardInfo} which includes modifications made in Android T to support MEP.
 */
@RequiresApi(VERSION_CODES.Q)
public class UiccCardInfoBuilder {

  private int cardId;
  private String eid;
  private String iccId;
  private int slotIndex;
  private int physicalSlotIndex;
  private List<UiccPortInfo> portList = new ArrayList<>();
  private boolean isEuicc;
  private boolean isMultipleEnabledProfilesSupported;
  private boolean isRemovable;

  private UiccCardInfoBuilder() {}

  public static UiccCardInfoBuilder newBuilder() {
    return new UiccCardInfoBuilder();
  }

  @CanIgnoreReturnValue
  public UiccCardInfoBuilder setCardId(int cardId) {
    this.cardId = cardId;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccCardInfoBuilder setEid(String eid) {
    this.eid = eid;
    return this;
  }

  /**
   * @deprecated This is no longer set on T+ due to MEP as a single eUICC can have more than one
   *     ICCID tied to it. It is instead set via {@code UiccPortInfo}.
   */
  @CanIgnoreReturnValue
  @Deprecated
  public UiccCardInfoBuilder setIccId(String iccId) {
    this.iccId = iccId;
    return this;
  }

  /**
   * @deprecated Use {@link setPhysicalSlotIndex} for Android T+ instead.
   */
  @CanIgnoreReturnValue
  @Deprecated
  public UiccCardInfoBuilder setSlotIndex(int slotIndex) {
    this.slotIndex = slotIndex;
    return this;
  }

  @CanIgnoreReturnValue
  @RequiresApi(VERSION_CODES.TIRAMISU)
  public UiccCardInfoBuilder setPhysicalSlotIndex(int physicalSlotIndex) {
    this.physicalSlotIndex = physicalSlotIndex;
    return this;
  }

  @CanIgnoreReturnValue
  @RequiresApi(VERSION_CODES.TIRAMISU)
  public UiccCardInfoBuilder setPorts(@NonNull List<UiccPortInfo> portList) {
    this.portList = portList;
    return this;
  }

  @CanIgnoreReturnValue
  @RequiresApi(VERSION_CODES.TIRAMISU)
  public UiccCardInfoBuilder setIsMultipleEnabledProfilesSupported(
      boolean isMultipleEnabledProfilesSupported) {
    this.isMultipleEnabledProfilesSupported = isMultipleEnabledProfilesSupported;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccCardInfoBuilder setIsEuicc(boolean isEuicc) {
    this.isEuicc = isEuicc;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccCardInfoBuilder setIsRemovable(boolean isRemovable) {
    this.isRemovable = isRemovable;
    return this;
  }

  public UiccCardInfo build() {
    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.TIRAMISU) {
      return ReflectionHelpers.callConstructor(
          UiccCardInfo.class,
          ClassParameter.from(boolean.class, isEuicc),
          ClassParameter.from(int.class, cardId),
          ClassParameter.from(String.class, eid),
          ClassParameter.from(String.class, iccId),
          ClassParameter.from(int.class, slotIndex),
          ClassParameter.from(boolean.class, isRemovable));
    }
    // T added the UiccPortInfo list and deprecated some top-level fields.
    return ReflectionHelpers.callConstructor(
        UiccCardInfo.class,
        ClassParameter.from(boolean.class, isEuicc),
        ClassParameter.from(int.class, cardId),
        ClassParameter.from(String.class, eid),
        ClassParameter.from(int.class, physicalSlotIndex),
        ClassParameter.from(boolean.class, isRemovable),
        ClassParameter.from(boolean.class, isMultipleEnabledProfilesSupported),
        ClassParameter.from(List.class, portList));
  }
}
