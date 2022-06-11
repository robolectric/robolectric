package org.robolectric.shadows;

import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build.VERSION_CODES;
import android.util.Size;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

/** Builder for StreamConfigurationMap */
public final class StreamConfigurationMapBuilder {
  // from system/core/include/system/graphics.h
  private static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 0x22;

  private final HashMap<Integer, Collection<Size>> inputFormatWithSupportedSize = new HashMap<>();
  private final HashMap<Integer, Collection<Size>> outputFormatWithSupportedSize = new HashMap<>();

  /** Create a new {@link StreamConfigurationMapBuilder}. */
  public static StreamConfigurationMapBuilder newBuilder() {
    return new StreamConfigurationMapBuilder();
  }

  /**
   * Adds an output size to be returned by {@link StreamConfigurationMap#getOutputSizes} for the
   * provided format.
   *
   * <p>The provided format must be one of the formats defined in {@link ImageFormat} or {@link
   * PixelFormat}.
   */
  public StreamConfigurationMapBuilder addOutputSize(int format, Size outputSize) {
    if (!outputFormatWithSupportedSize.containsKey(format)) {
      Collection<Size> outputSizes = new ArrayList<>();
      outputFormatWithSupportedSize.put(format, outputSizes);
    }
    outputFormatWithSupportedSize.get(format).add(outputSize);
    return this;
  }

  /**
   * Adds an input size to be returned by {@link StreamConfigurationMap#getInputSizes} for the
   * provided format.
   *
   * <p>The provided format must be one of the formats defined in {@link ImageFormat} or {@link
   * PixelFormat}.
   */
  public StreamConfigurationMapBuilder addInputSize(int format, Size inputSize) {
    if (!inputFormatWithSupportedSize.containsKey(format)) {
      List<Size> inputSizes = new ArrayList<>();
      inputFormatWithSupportedSize.put(format, inputSizes);
    }
    inputFormatWithSupportedSize.get(format).add(inputSize);
    return this;
  }

  /**
   * Adds an output size to be returned by {@link StreamConfigurationMap#getOutputSizes}.
   *
   * <p>Calling this method is equivalent to calling {@link addOutputSize(int, Size)} with format
   * {@link ImageFormat#PRIVATE}.
   */
  public StreamConfigurationMapBuilder addOutputSize(Size outputSize) {
    addOutputSize(HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED, outputSize);
    return this;
  }

  /** Builds a StreamConfigurationMap based on data previously added to this builder. */
  public StreamConfigurationMap build() {
    Collection<StreamConfiguration> configsList = new ArrayList<>();

    for (Map.Entry<Integer, Collection<Size>> entry : outputFormatWithSupportedSize.entrySet()) {
      for (Size size : entry.getValue()) {
        configsList.add(
            new StreamConfiguration(
                entry.getKey(), size.getWidth(), size.getHeight(), /*input=*/ false));
      }
    }

    for (Map.Entry<Integer, Collection<Size>> entry : inputFormatWithSupportedSize.entrySet()) {
      for (Size size : entry.getValue()) {
        configsList.add(
            new StreamConfiguration(
                entry.getKey(), size.getWidth(), size.getHeight(), /*input=*/ true));
      }
    }

    StreamConfiguration[] configs = new StreamConfiguration[configsList.size()];
    configsList.toArray(configs);

    StreamConfigurationMap map = ReflectionHelpers.callConstructor(StreamConfigurationMap.class);
    ReflectionHelpers.setField(StreamConfigurationMap.class, map, "mConfigurations", configs);

    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.M) {
      HashMap<Integer, Integer> outputFormats = new HashMap<>();
      for (int format : outputFormatWithSupportedSize.keySet()) {
        outputFormats.put(format, outputFormatWithSupportedSize.get(format).size());
      }
      ReflectionHelpers.setField(
          StreamConfigurationMap.class, map, "mOutputFormats", outputFormats);
    } else {
      SparseIntArray outputFormats = new SparseIntArray();
      for (int format : outputFormatWithSupportedSize.keySet()) {
        outputFormats.put(format, outputFormatWithSupportedSize.get(format).size());
      }
      ReflectionHelpers.setField(
          StreamConfigurationMap.class, map, "mOutputFormats", outputFormats);
      ReflectionHelpers.setField(
          StreamConfigurationMap.class, map, "mAllOutputFormats", outputFormats);

      // Add input formats for reprocessing
      SparseIntArray inputFormats = new SparseIntArray();
      for (int format : inputFormatWithSupportedSize.keySet()) {
        inputFormats.put(format, inputFormatWithSupportedSize.get(format).size());
      }
      ReflectionHelpers.setField(StreamConfigurationMap.class, map, "mInputFormats", inputFormats);
    }
    return map;
  }

  private StreamConfigurationMapBuilder() {}
}
