package org.robolectric.shadows;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow of {@link MediaCodecList} that allows us to override relevant methods for testing */
@TargetApi(VERSION_CODES.LOLLIPOP)
@Implements(MediaCodecList.class)
public class ShadowMediaCodecList {

  @Implementation(minSdk = VERSION_CODES.LOLLIPOP)
  protected final String findEncoderForFormat(MediaFormat format) {
    return getDummyMediaCodecInfo().getName();
  }

  @Implementation(minSdk = VERSION_CODES.LOLLIPOP)
  protected final MediaCodecInfo[] getCodecInfos() {
    return new MediaCodecInfo[] {getDummyMediaCodecInfo()};
  }

  private static MediaCodecInfo getDummyMediaCodecInfo() {
    return ReflectionHelpers.newInstance(MediaCodecInfo.class);
  }
}
