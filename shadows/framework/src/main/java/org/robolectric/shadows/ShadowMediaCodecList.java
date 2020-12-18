package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/**
 * Implementation of {@link MediaCodecList}.
 *
 * <p>Custom {@link MediaCodecInfo} can be created using {@link MediaCodecInfoBuilder} and added to
 * the list of codecs via {@link #addCodec}.
 */
@Implements(value = MediaCodecList.class, minSdk = LOLLIPOP)
public class ShadowMediaCodecList {

  private static final List<MediaCodecInfo> mediaCodecInfos =
      Collections.synchronizedList(new ArrayList<>());

  /**
   * Add a {@link MediaCodecInfo} to the list of MediaCodecInfos.
   *
   * @param mediaCodecInfo {@link MediaCodecInfo} describing the codec. Use {@link
   *     MediaCodecInfoBuilder} to create an instance of it.
   */
  @TargetApi(Q)
  public static void addCodec(MediaCodecInfo mediaCodecInfo) {
    mediaCodecInfos.add(mediaCodecInfo);
  }

  @Resetter
  public static void reset() {
    mediaCodecInfos.clear();
    ReflectionHelpers.setStaticField(MediaCodecList.class, "sAllCodecInfos", null);
    ReflectionHelpers.setStaticField(MediaCodecList.class, "sRegularCodecInfos", null);
  }

  @Implementation
  protected static int native_getCodecCount() {
    return mediaCodecInfos.size();
  }

  @Implementation
  protected static MediaCodecInfo getNewCodecInfoAt(int index) {
    return mediaCodecInfos.get(index);
  }

  @Implementation(minSdk = M)
  protected static Map<String, Object> native_getGlobalSettings() {
    return new HashMap<>();
  }
}
