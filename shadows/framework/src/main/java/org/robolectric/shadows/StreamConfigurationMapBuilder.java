package org.robolectric.shadows;

import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build.VERSION_CODES;
import android.util.Size;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

/** Builder for StreamConfigurationMap */
public final class StreamConfigurationMapBuilder {
  // from system/core/include/system/graphics.h
  private static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 0x22;

  private final Collection<Size> outputSizes = new ArrayList<>();

  /** Create a new {@link StreamConfigurationMapBuilder}. */
  public static StreamConfigurationMapBuilder newBuilder() {
    return new StreamConfigurationMapBuilder();
  }

  /** Adds an output size to be returned by {@link StreamConfigurationMap#getOutputSizes}. */
  public StreamConfigurationMapBuilder addOutputSize(Size outputSize) {
    outputSizes.add(outputSize);
    return this;
  }

  /** Builds a StreamConfigurationMap based on data previously added to this builder. */
  public StreamConfigurationMap build() {
    StreamConfiguration[] configs = new StreamConfiguration[outputSizes.size()];
    int i = 0;
    for (Size size : outputSizes) {
      configs[i] =
          new StreamConfiguration(
              HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED,
              size.getWidth(),
              size.getHeight(),
              /*input=*/ false);
      i++;
    }

    StreamConfigurationMap map = ReflectionHelpers.callConstructor(StreamConfigurationMap.class);
    ReflectionHelpers.setField(StreamConfigurationMap.class, map, "mConfigurations", configs);

    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.M) {
      HashMap<Integer, Integer> outputFormats = new HashMap<>();

      outputFormats.put(HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED, outputSizes.size());
      ReflectionHelpers.setField(
          StreamConfigurationMap.class, map, "mOutputFormats", outputFormats);
    } else {
      SparseIntArray outputFormats = new SparseIntArray();
      outputFormats.put(HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED, outputSizes.size());
      ReflectionHelpers.setField(
          StreamConfigurationMap.class, map, "mOutputFormats", outputFormats);
      ReflectionHelpers.setField(
          StreamConfigurationMap.class, map, "mAllOutputFormats", outputFormats);
    }

    return map;
  }

  private StreamConfigurationMapBuilder() {}
}
