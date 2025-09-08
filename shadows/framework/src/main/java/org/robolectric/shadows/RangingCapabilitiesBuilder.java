package org.robolectric.shadows;

import android.ranging.RangingCapabilities;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashMap;
import java.util.Map;

/** Factory for {@link RangingData} */
public class RangingCapabilitiesBuilder {
  public RangingCapabilitiesBuilder() {
    this.availabilities = new HashMap<>();
  }

  Map<Integer, Integer> availabilities;

  /** Adds ranging technology availability to the builder */
  @CanIgnoreReturnValue
  public RangingCapabilitiesBuilder addAvailability(int rangingTechnology, int availability) {
    this.availabilities.put(rangingTechnology, availability);
    return this;
  }

  /** Builds an instance of {@link RangingCapabilities} */
  public RangingCapabilities build() {
    RangingCapabilities.Builder rangingCapabilitiesBuilder = new RangingCapabilities.Builder();
    for (Map.Entry<Integer, Integer> entry : availabilities.entrySet()) {
      rangingCapabilitiesBuilder.addAvailability(entry.getKey(), entry.getValue());
    }
    return rangingCapabilitiesBuilder.build();
  }
}
