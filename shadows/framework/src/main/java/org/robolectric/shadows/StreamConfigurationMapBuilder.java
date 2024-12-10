package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.camera2.params.HighSpeedVideoConfiguration;
import android.hardware.camera2.params.ReprocessFormatsMap;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationDuration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build.VERSION_CODES;
import android.util.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

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
    boolean hasPrivateOutput = false;
    for (Map.Entry<Integer, Collection<Size>> entry : outputFormatWithSupportedSize.entrySet()) {
      hasPrivateOutput =
          hasPrivateOutput || (entry.getKey() == HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED);
      for (Size size : entry.getValue()) {
        configsList.add(
            new StreamConfiguration(
                entry.getKey(), size.getWidth(), size.getHeight(), /* input= */ false));
      }
    }
    // The constructor of StreamConfigurationMap asserts that a
    // HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED config exists.
    if (!hasPrivateOutput) {
      configsList.add(
          new StreamConfiguration(HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED, 1, 1, false));
    }

    for (Map.Entry<Integer, Collection<Size>> entry : inputFormatWithSupportedSize.entrySet()) {
      for (Size size : entry.getValue()) {
        configsList.add(
            new StreamConfiguration(
                entry.getKey(), size.getWidth(), size.getHeight(), /* input= */ true));
      }
    }

    StreamConfiguration[] configs = new StreamConfiguration[configsList.size()];
    configsList.toArray(configs);

    if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.M) {
      return reflector(StreamConfigurationMapReflector.class)
          .newStreamConfigurationMapL(
              configs,
              new StreamConfigurationDuration[0],
              new StreamConfigurationDuration[0],
              /* highSpeedVideoConfigurations= */ null);
    } else if (RuntimeEnvironment.getApiLevel() < VERSION_CODES.Q) {
      return reflector(StreamConfigurationMapReflector.class)
          .newStreamConfigurationMapM(
              configs,
              new StreamConfigurationDuration[0],
              new StreamConfigurationDuration[0],
              /* depthConfigurations= */ null,
              /* depthMinFrameDurations= */ null,
              /* depthStallDurations= */ null,
              /* highSpeedVideoConfigurations= */ null,
              /* inputOutputFormatsMap= */ null,
              /* listHighResolution= */ false);
    } else if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.TIRAMISU) {
      return reflector(StreamConfigurationMapReflector.class)
          .newStreamConfigurationMapQ(
              configs,
              new StreamConfigurationDuration[0],
              new StreamConfigurationDuration[0],
              /* depthConfigurations= */ null,
              /* depthMinFrameDurations= */ null,
              /* depthStallDurations= */ null,
              /* dynamicDepthConfigurations= */ null,
              /* dynamicDepthMinFrameDurations= */ null,
              /* dynamicDepthStallDurations= */ null,
              /* heicConfigurations= */ null,
              /* heicMinFrameDurations= */ null,
              /* heicStallDurations= */ null,
              /* highSpeedVideoConfigurations= */ null,
              /* inputOutputFormatsMap= */ null,
              /* listHighResolution= */ false);
    } else if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.VANILLA_ICE_CREAM) {
      return reflector(StreamConfigurationMapReflector.class)
          .newStreamConfigurationMapV(
              configs,
              new StreamConfigurationDuration[0],
              new StreamConfigurationDuration[0],
              /* depthConfigurations= */ null,
              /* depthMinFrameDurations= */ null,
              /* depthStallDurations= */ null,
              /* dynamicDepthConfigurations= */ null,
              /* dynamicDepthMinFrameDurations= */ null,
              /* dynamicDepthStallDurations= */ null,
              /* heicConfigurations= */ null,
              /* heicMinFrameDurations= */ null,
              /* heicStallDurations= */ null,
              /* jpegRConfigurations= */ null,
              /* jpegRMinFrameDurations= */ null,
              /* jpegRStallDurations= */ null,
              /* highSpeedVideoConfigurations= */ null,
              /* inputOutputFormatsMap= */ null,
              /* listHighResolution= */ false);
    } else {
      return reflector(StreamConfigurationMapReflector.class)
          .newStreamConfigurationMapW(
              configs,
              new StreamConfigurationDuration[0],
              new StreamConfigurationDuration[0],
              /* depthConfigurations= */ null,
              /* depthMinFrameDurations= */ null,
              /* depthStallDurations= */ null,
              /* dynamicDepthConfigurations= */ null,
              /* dynamicDepthMinFrameDurations= */ null,
              /* dynamicDepthStallDurations= */ null,
              /* heicConfigurations= */ null,
              /* heicMinFrameDurations= */ null,
              /* heicStallDurations= */ null,
              /* jpegRConfigurations= */ null,
              /* jpegRMinFrameDurations= */ null,
              /* jpegRStallDurations= */ null,
              /* heicUltraHDRConfigurations= */ null,
              /* heicUltraHDRMinFrameDurations= */ null,
              /* heicUltraHDRStallDurations= */ null,
              /* highSpeedVideoConfigurations= */ null,
              /* inputOutputFormatsMap= */ null,
              /* listHighResolution= */ false);
    }
  }

  // Constructors for StreamConfigurationMap per SDK version.
  @ForType(StreamConfigurationMap.class)
  interface StreamConfigurationMapReflector {
    @Constructor
    StreamConfigurationMap newStreamConfigurationMapL(
        StreamConfiguration[] configurations,
        StreamConfigurationDuration[] minFrameDurations,
        StreamConfigurationDuration[] stallDurations,
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurations);

    @Constructor
    StreamConfigurationMap newStreamConfigurationMapM(
        StreamConfiguration[] configurations,
        StreamConfigurationDuration[] minFrameDurations,
        StreamConfigurationDuration[] stallDurations,
        StreamConfiguration[] depthConfigurations,
        StreamConfigurationDuration[] depthMinFrameDurations,
        StreamConfigurationDuration[] depthStallDurations,
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurations,
        ReprocessFormatsMap inputOutputFormatsMap,
        boolean listHighResolution);

    @Constructor
    StreamConfigurationMap newStreamConfigurationMapQ(
        StreamConfiguration[] configurations,
        StreamConfigurationDuration[] minFrameDurations,
        StreamConfigurationDuration[] stallDurations,
        StreamConfiguration[] depthConfigurations,
        StreamConfigurationDuration[] depthMinFrameDurations,
        StreamConfigurationDuration[] depthStallDurations,
        StreamConfiguration[] dynamicDepthConfigurations,
        StreamConfigurationDuration[] dynamicDepthMinFrameDurations,
        StreamConfigurationDuration[] dynamicDepthStallDurations,
        StreamConfiguration[] heicConfigurations,
        StreamConfigurationDuration[] heicMinFrameDurations,
        StreamConfigurationDuration[] heicStallDurations,
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurations,
        ReprocessFormatsMap inputOutputFormatsMap,
        boolean listHighResolution);

    @Constructor
    StreamConfigurationMap newStreamConfigurationMapV(
        StreamConfiguration[] configurations,
        StreamConfigurationDuration[] minFrameDurations,
        StreamConfigurationDuration[] stallDurations,
        StreamConfiguration[] depthConfigurations,
        StreamConfigurationDuration[] depthMinFrameDurations,
        StreamConfigurationDuration[] depthStallDurations,
        StreamConfiguration[] dynamicDepthConfigurations,
        StreamConfigurationDuration[] dynamicDepthMinFrameDurations,
        StreamConfigurationDuration[] dynamicDepthStallDurations,
        StreamConfiguration[] heicConfigurations,
        StreamConfigurationDuration[] heicMinFrameDurations,
        StreamConfigurationDuration[] heicStallDurations,
        StreamConfiguration[] jpegRConfigurations,
        StreamConfigurationDuration[] jpegRMinFrameDurations,
        StreamConfigurationDuration[] jpegRStallDurations,
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurations,
        ReprocessFormatsMap inputOutputFormatsMap,
        boolean listHighResolution);

    @Constructor
    StreamConfigurationMap newStreamConfigurationMapW(
        StreamConfiguration[] configurations,
        StreamConfigurationDuration[] minFrameDurations,
        StreamConfigurationDuration[] stallDurations,
        StreamConfiguration[] depthConfigurations,
        StreamConfigurationDuration[] depthMinFrameDurations,
        StreamConfigurationDuration[] depthStallDurations,
        StreamConfiguration[] dynamicDepthConfigurations,
        StreamConfigurationDuration[] dynamicDepthMinFrameDurations,
        StreamConfigurationDuration[] dynamicDepthStallDurations,
        StreamConfiguration[] heicConfigurations,
        StreamConfigurationDuration[] heicMinFrameDurations,
        StreamConfigurationDuration[] heicStallDurations,
        StreamConfiguration[] jpegRConfigurations,
        StreamConfigurationDuration[] jpegRMinFrameDurations,
        StreamConfigurationDuration[] jpegRStallDurations,
        StreamConfiguration[] heicUltraHDRConfigurations,
        StreamConfigurationDuration[] heicUltraHDRMinFrameDurations,
        StreamConfigurationDuration[] heicUltraHDRStallDurations,
        HighSpeedVideoConfiguration[] highSpeedVideoConfigurations,
        ReprocessFormatsMap inputOutputFormatsMap,
        boolean listHighResolution);
  }

  private StreamConfigurationMapBuilder() {}
}
