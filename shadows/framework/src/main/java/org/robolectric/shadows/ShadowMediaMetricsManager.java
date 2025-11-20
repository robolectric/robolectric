package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.media.metrics.BundleSession;
import android.media.metrics.EditingSession;
import android.media.metrics.MediaMetricsManager;
import android.media.metrics.PlaybackSession;
import android.media.metrics.RecordingSession;
import android.media.metrics.TranscodingSession;
import android.os.Build;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link MediaMetricsManager}. */
@Implements(
    value = MediaMetricsManager.class,
    minSdk = Build.VERSION_CODES.S,
    isInAndroidSdk = false)
public class ShadowMediaMetricsManager {

  private static final AtomicInteger sessionIdSource = new AtomicInteger();

  @RealObject private MediaMetricsManager realMediaMetricsManager;

  @Resetter
  public static void reset() {
    sessionIdSource.set(0);
  }

  @Implementation
  protected PlaybackSession createPlaybackSession() {
    return reflector(PlaybackSessionReflector.class)
        .newInstance(getNextSessionId(), realMediaMetricsManager);
  }

  @Implementation
  protected RecordingSession createRecordingSession() {
    return reflector(RecordingSessionReflector.class)
        .newInstance(getNextSessionId(), realMediaMetricsManager);
  }

  @Implementation(minSdk = Build.VERSION_CODES.TIRAMISU)
  protected TranscodingSession createTranscodingSession() {
    return reflector(TranscodingSessionReflector.class)
        .newInstance(getNextSessionId(), realMediaMetricsManager);
  }

  @Implementation(minSdk = Build.VERSION_CODES.TIRAMISU)
  protected EditingSession createEditingSession() {
    return reflector(EditingSessionReflector.class)
        .newInstance(getNextSessionId(), realMediaMetricsManager);
  }

  @Implementation(minSdk = Build.VERSION_CODES.TIRAMISU)
  protected BundleSession createBundleSession() {
    return reflector(BundleSessionReflector.class)
        .newInstance(getNextSessionId(), realMediaMetricsManager);
  }

  private static String getNextSessionId() {
    return Integer.toString(sessionIdSource.incrementAndGet());
  }

  @ForType(PlaybackSession.class)
  private interface PlaybackSessionReflector {
    // For the default constructor
    @Constructor
    PlaybackSession newInstance(String id, MediaMetricsManager mediaMetricsManager);
  }

  @ForType(RecordingSession.class)
  private interface RecordingSessionReflector {
    // For the default constructor
    @Constructor
    RecordingSession newInstance(String id, MediaMetricsManager mediaMetricsManager);
  }

  @ForType(TranscodingSession.class)
  private interface TranscodingSessionReflector {
    // For the default constructor
    @Constructor
    TranscodingSession newInstance(String id, MediaMetricsManager mediaMetricsManager);
  }

  @ForType(EditingSession.class)
  private interface EditingSessionReflector {
    // For the default constructor
    @Constructor
    EditingSession newInstance(String id, MediaMetricsManager mediaMetricsManager);
  }

  @ForType(BundleSession.class)
  private interface BundleSessionReflector {
    // For the default constructor
    @Constructor
    BundleSession newInstance(String id, MediaMetricsManager mediaMetricsManager);
  }
}
