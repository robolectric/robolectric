package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.nfc.cardemulation.PollingFrame;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import javax.annotation.Nullable;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link PollingFrame}. */
public class PollingFrameBuilder {
  private int type;
  private byte[] data;
  private int vendorSpecificGain;
  private long timestamp;
  private boolean triggeredAutoTransact;

  private PollingFrameBuilder() {}

  public static PollingFrameBuilder newBuilder() {
    return new PollingFrameBuilder();
  }

  @CanIgnoreReturnValue
  public PollingFrameBuilder setType(int type) {
    this.type = type;
    return this;
  }

  @CanIgnoreReturnValue
  public PollingFrameBuilder setData(@Nullable byte[] data) {
    this.data = data;
    return this;
  }

  @CanIgnoreReturnValue
  public PollingFrameBuilder setVendorSpecificGain(int vendorSpecificGain) {
    this.vendorSpecificGain = vendorSpecificGain;
    return this;
  }

  @CanIgnoreReturnValue
  public PollingFrameBuilder setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  @CanIgnoreReturnValue
  public PollingFrameBuilder setTriggeredAutoTransact(boolean triggeredAutoTransact) {
    this.triggeredAutoTransact = triggeredAutoTransact;
    return this;
  }

  public PollingFrame build() {
    return reflector(PollingFrameReflector.class)
        .newPollingFrame(type, data, vendorSpecificGain, timestamp, triggeredAutoTransact);
  }

  @ForType(PollingFrame.class)
  interface PollingFrameReflector {
    @Constructor
    PollingFrame newPollingFrame(
        int type, byte[] data, int gain, long timestamp, boolean triggeredAutoTransact);
  }
}
