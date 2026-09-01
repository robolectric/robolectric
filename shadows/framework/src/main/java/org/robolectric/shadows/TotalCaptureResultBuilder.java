package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link TotalCaptureResult}. */
public class TotalCaptureResultBuilder {
  private int sequenceId = 0;
  private final Map<CaptureResult.Key<?>, Object> entries = new HashMap<>();
  private final Map<String, TotalCaptureResult> physicalCameraResults = new HashMap<>();

  private TotalCaptureResultBuilder() {}

  public static TotalCaptureResultBuilder newBuilder() {
    return new TotalCaptureResultBuilder();
  }

  @CanIgnoreReturnValue
  public <T> TotalCaptureResultBuilder set(CaptureResult.Key<T> key, T value) {
    entries.put(key, value);
    return this;
  }

  @CanIgnoreReturnValue
  public TotalCaptureResultBuilder setSequenceId(int sequenceId) {
    this.sequenceId = sequenceId;
    return this;
  }

  @CanIgnoreReturnValue
  public TotalCaptureResultBuilder setPhysicalCameraResults(
      Map<String, TotalCaptureResult> physicalCameraResults) {
    this.physicalCameraResults.clear();
    this.physicalCameraResults.putAll(physicalCameraResults);
    return this;
  }

  @CanIgnoreReturnValue
  public TotalCaptureResultBuilder addPhysicalCameraResult(
      String physicalCameraId, TotalCaptureResult physicalResult) {
    this.physicalCameraResults.put(physicalCameraId, physicalResult);
    return this;
  }

  public TotalCaptureResult build() {
    TotalCaptureResult result = new TotalCaptureResult(new CameraMetadataNative(), sequenceId);
    CameraMetadataNative results = reflector(CaptureResultReflector.class, result).getResults();
    for (Map.Entry<CaptureResult.Key<?>, Object> entry : entries.entrySet()) {
      setEntry(results, entry.getKey(), entry.getValue());
    }
    if (!physicalCameraResults.isEmpty()) {
      reflector(TotalCaptureResultReflector.class, result)
          .setPhysicalCaptureResults(new HashMap<>(physicalCameraResults));
    }
    return result;
  }

  // Type safety is guaranteed by set(CaptureResult.Key<T>, T).
  @SuppressWarnings("unchecked")
  private static <T> void setEntry(
      CameraMetadataNative results, CaptureResult.Key<?> key, Object value) {
    results.set((CaptureResult.Key<T>) key, (T) value);
  }

  @ForType(CaptureResult.class)
  interface CaptureResultReflector {
    @Accessor("mResults")
    CameraMetadataNative getResults();
  }

  @ForType(TotalCaptureResult.class)
  interface TotalCaptureResultReflector {
    @Accessor("mPhysicalCaptureResults")
    // TotalCaptureResult uses HashMap for its internal field mPhysicalCaptureResults.
    @SuppressWarnings("NonApiType")
    void setPhysicalCaptureResults(HashMap<String, TotalCaptureResult> physicalCaptureResults);
  }
}
