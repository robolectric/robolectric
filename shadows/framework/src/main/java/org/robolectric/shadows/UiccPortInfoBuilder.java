package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.telephony.UiccPortInfo;
import androidx.annotation.RequiresApi;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link UiccPortInfo} which was introduced in Android T. */
@RequiresApi(VERSION_CODES.TIRAMISU)
public class UiccPortInfoBuilder {

  private String iccId;
  private int portIndex;
  private int logicalSlotIndex;
  private boolean isActive;

  private UiccPortInfoBuilder() {}

  public static UiccPortInfoBuilder newBuilder() {
    return new UiccPortInfoBuilder();
  }

  @CanIgnoreReturnValue
  public UiccPortInfoBuilder setIccId(String iccId) {
    this.iccId = iccId;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccPortInfoBuilder setPortIndex(int portIndex) {
    this.portIndex = portIndex;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccPortInfoBuilder setLogicalSlotIndex(int logicalSlotIndex) {
    this.logicalSlotIndex = logicalSlotIndex;
    return this;
  }

  @CanIgnoreReturnValue
  public UiccPortInfoBuilder setIsActive(boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  public UiccPortInfo build() {
    return ReflectionHelpers.callConstructor(
        UiccPortInfo.class,
        ClassParameter.from(String.class, iccId),
        ClassParameter.from(int.class, portIndex),
        ClassParameter.from(int.class, logicalSlotIndex),
        ClassParameter.from(boolean.class, isActive));
  }
}
