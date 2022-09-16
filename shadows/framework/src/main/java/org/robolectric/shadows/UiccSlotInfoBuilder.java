package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.telephony.UiccPortInfo;
import android.telephony.UiccSlotInfo;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link UiccSlotInfo} which was introduced in Android P. */
public class UiccSlotInfoBuilder {

  private boolean isEuicc = true;
  private String cardId = "";
  private int cardStateInfo;
  private boolean isExtendedApduSupported;
  private boolean isRemovable; // For API > 28
  private boolean isActive; // For API < 33
  private int logicalSlotIdx; // For API < 33
  private final List<UiccPortInfo> portList = new ArrayList<>();

  private UiccSlotInfoBuilder() {}

  public static UiccSlotInfoBuilder newBuilder() {
    return new UiccSlotInfoBuilder();
  }

  @CanIgnoreReturnValue
  public UiccSlotInfoBuilder setIsEuicc(boolean isEuicc) {
    this.isEuicc = isEuicc;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccSlotInfoBuilder setCardId(String cardId) {
    this.cardId = cardId;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccSlotInfoBuilder setCardStateInfo(int cardStateInfo) {
    this.cardStateInfo = cardStateInfo;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccSlotInfoBuilder setIsExtendedApduSupported(boolean isExtendedApduSupported) {
    this.isExtendedApduSupported = isExtendedApduSupported;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccSlotInfoBuilder setIsRemovable(boolean isRemovable) {
    this.isRemovable = isRemovable;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccSlotInfoBuilder addPort(
      String iccId, int portIndex, int logicSlotIndex, boolean isActive) {
    if (RuntimeEnvironment.getApiLevel() >= TIRAMISU) {
      this.portList.add(new UiccPortInfo(iccId, portIndex, logicSlotIndex, isActive));
    } else {
      this.isActive = isActive;
      this.logicalSlotIdx = logicSlotIndex;
    }
    return this;
  }

  public UiccSlotInfo build() {
    if (RuntimeEnvironment.getApiLevel() >= TIRAMISU) {
      return new UiccSlotInfo(
          isEuicc,
          cardId,
          cardStateInfo,
          isExtendedApduSupported,
          isRemovable,
          ImmutableList.copyOf(portList));
    } else if (RuntimeEnvironment.getApiLevel() >= Q) {
      return ReflectionHelpers.callConstructor(
          UiccSlotInfo.class,
          ClassParameter.from(boolean.class, isActive),
          ClassParameter.from(boolean.class, isEuicc),
          ClassParameter.from(String.class, cardId),
          ClassParameter.from(int.class, cardStateInfo),
          ClassParameter.from(int.class, logicalSlotIdx),
          ClassParameter.from(boolean.class, isExtendedApduSupported),
          ClassParameter.from(boolean.class, isRemovable));
    } else {
      return new UiccSlotInfo(
          isActive, isActive, cardId, cardStateInfo, logicalSlotIdx, isExtendedApduSupported);
    }
  }
}
