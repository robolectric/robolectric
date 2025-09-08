package org.robolectric.shadows;

import android.ranging.RangingData;
import android.ranging.RangingMeasurement;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** Factory for {@link RangingData} */
public class RangingDataBuilder {

  int rangingTechnology;
  double distance;
  long timestampMillis;
  int confidence = RangingMeasurement.CONFIDENCE_HIGH;

  /** Sets the ranging technology. */
  @CanIgnoreReturnValue
  public RangingDataBuilder setRangingTechnology(int rangingTechnology) {
    this.rangingTechnology = rangingTechnology;
    return this;
  }

  /** Sets the distance value. */
  @CanIgnoreReturnValue
  public RangingDataBuilder setDistance(double distance) {
    this.distance = distance;
    return this;
  }

  /** Sets the timestamp value. */
  @CanIgnoreReturnValue
  public RangingDataBuilder setTimestampMillis(long timestampMillis) {
    this.timestampMillis = timestampMillis;
    return this;
  }

  @CanIgnoreReturnValue
  public RangingDataBuilder setConfidence(int confidence) {
    this.confidence = confidence;
    return this;
  }

  public RangingData build() {
    return new RangingData.Builder()
        .setRangingTechnology(rangingTechnology)
        .setTimestampMillis(timestampMillis)
        .setDistance(
            new RangingMeasurement.Builder()
                .setMeasurement(distance)
                .setConfidence(confidence)
                .build())
        .build();
  }
}
