package org.robolectric.shadows;

import android.media.CamcorderProfile;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow of the CamcorderProfile that allows the caller to add custom profile settings. */
@Implements(CamcorderProfile.class)
public class ShadowCamcorderProfile {

  private static final Table<Integer, Integer, CamcorderProfile> profiles = HashBasedTable.create();

  public static void addProfile(int cameraId, int quality, CamcorderProfile profile) {
    profiles.put(cameraId, quality, profile);
  }

  @Resetter
  public static void reset() {
    profiles.clear();
  }

  public static CamcorderProfile createProfile(
      int duration,
      int quality,
      int fileFormat,
      int videoCodec,
      int videoBitRate,
      int videoFrameRate,
      int videoWidth,
      int videoHeight,
      int audioCodec,
      int audioBitRate,
      int audioSampleRate,
      int audioChannels) {
    // CamcorderProfile doesn't have a public constructor. To construct we need to use reflection.
    return ReflectionHelpers.callConstructor(
        CamcorderProfile.class,
        ClassParameter.from(int.class, duration),
        ClassParameter.from(int.class, quality),
        ClassParameter.from(int.class, fileFormat),
        ClassParameter.from(int.class, videoCodec),
        ClassParameter.from(int.class, videoBitRate),
        ClassParameter.from(int.class, videoFrameRate),
        ClassParameter.from(int.class, videoWidth),
        ClassParameter.from(int.class, videoHeight),
        ClassParameter.from(int.class, audioCodec),
        ClassParameter.from(int.class, audioBitRate),
        ClassParameter.from(int.class, audioSampleRate),
        ClassParameter.from(int.class, audioChannels));
  }

  @Implementation
  protected static boolean hasProfile(int cameraId, int quality) {
    return profiles.contains(cameraId, quality);
  }

  @Implementation
  protected static CamcorderProfile get(int cameraId, int quality) {
    return profiles.get(cameraId, quality);
  }
}
