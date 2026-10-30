package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.params.MultiResolutionStreamConfigurationMap;
import android.hardware.camera2.params.MultiResolutionStreamInfo;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build.VERSION_CODES;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link MultiResolutionStreamConfigurationMap}. */
@RequiresApi(VERSION_CODES.S)
public final class MultiResolutionStreamConfigurationMapBuilder {
  private final Map<String, List<StreamConfiguration>> configurationsByPhysicalCameraId =
      new LinkedHashMap<>();

  private MultiResolutionStreamConfigurationMapBuilder() {}

  /** Creates a new {@link MultiResolutionStreamConfigurationMapBuilder}. */
  public static MultiResolutionStreamConfigurationMapBuilder newBuilder() {
    return new MultiResolutionStreamConfigurationMapBuilder();
  }

  /**
   * Adds an output {@link MultiResolutionStreamInfo} to be returned by {@link
   * MultiResolutionStreamConfigurationMap#getOutputInfo} for the provided format.
   *
   * <p>The provided format must be one of the formats defined in {@link ImageFormat} or {@link
   * PixelFormat}.
   */
  @CanIgnoreReturnValue
  public MultiResolutionStreamConfigurationMapBuilder addOutputStreamConfig(
      int format, MultiResolutionStreamInfo streamInfo) {
    return addStreamConfig(format, streamInfo, /* input= */ false);
  }

  /**
   * Adds an input {@link MultiResolutionStreamInfo} to be returned by {@link
   * MultiResolutionStreamConfigurationMap#getInputInfo} for the provided format.
   *
   * <p>The provided format must be one of the formats defined in {@link ImageFormat} or {@link
   * PixelFormat}.
   */
  @CanIgnoreReturnValue
  public MultiResolutionStreamConfigurationMapBuilder addInputStreamConfig(
      int format, MultiResolutionStreamInfo streamInfo) {
    return addStreamConfig(format, streamInfo, /* input= */ true);
  }

  @CanIgnoreReturnValue
  private MultiResolutionStreamConfigurationMapBuilder addStreamConfig(
      int format, MultiResolutionStreamInfo streamInfo, boolean input) {
    Objects.requireNonNull(streamInfo, "streamInfo must not be null");
    int internalFormat = StreamConfigurationMap.imageFormatToInternal(new int[] {format})[0];
    List<StreamConfiguration> configs =
        configurationsByPhysicalCameraId.computeIfAbsent(
            streamInfo.getPhysicalCameraId(), (String k) -> new ArrayList<>());
    configs.add(
        new StreamConfiguration(
            internalFormat, streamInfo.getWidth(), streamInfo.getHeight(), input));
    return this;
  }

  /**
   * Builds a {@link MultiResolutionStreamConfigurationMap} based on data previously added to this
   * builder.
   */
  public MultiResolutionStreamConfigurationMap build() {
    Map<String, StreamConfiguration[]> configurations = new LinkedHashMap<>();
    for (Map.Entry<String, List<StreamConfiguration>> entry :
        configurationsByPhysicalCameraId.entrySet()) {
      configurations.put(entry.getKey(), entry.getValue().toArray(new StreamConfiguration[0]));
    }
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.BAKLAVA) {
      return reflector(MultiResolutionStreamConfigurationMapReflector.class)
          .newMultiResolutionStreamConfigurationMapS(configurations);
    }
    return reflector(MultiResolutionStreamConfigurationMapReflector.class)
        .newMultiResolutionStreamConfigurationMapC(
            configurations, /* concurrentReaderFormats= */ null);
  }

  @ForType(MultiResolutionStreamConfigurationMap.class)
  interface MultiResolutionStreamConfigurationMapReflector {
    @Constructor
    MultiResolutionStreamConfigurationMap newMultiResolutionStreamConfigurationMapS(
        Map<String, StreamConfiguration[]> configurations);

    @Constructor
    MultiResolutionStreamConfigurationMap newMultiResolutionStreamConfigurationMapC(
        Map<String, StreamConfiguration[]> configurations, int[] concurrentReaderFormats);
  }
}
