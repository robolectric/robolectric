package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.media.MediaCodecInfo.CodecCapabilities;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Shadow for {@link CodecCapabilities}.
 *
 * <p>This is needed to force the java implementation (as opposed to the unsupported native
 * implementation) on post-baklava SDKs.
 */
@Implements(CodecCapabilities.class)
public class ShadowCodecCapabilities {

  // TODO: figure out why minSdk = PostBaklava doesn't work
  @Implementation(minSdk = BAKLAVA)
  protected static CodecCapabilities createFromProfileLevel(String mime, int profile, int level) {
    if (RuntimeEnvironment.getApiLevel() <= BAKLAVA) {
      return reflector(CodecCapabiltiesReflector.class)
          .createFromProfileLevel(mime, profile, level);
    }
    // force LegacyImpl not native which is unsupported
    Object impl =
        reflector(CodecCapsLegacyImplReflector.class).createFromProfileLevel(mime, profile, level);
    try {
      return ReflectionHelpers.callConstructor(
          CodecCapabilities.class,
          ClassParameter.from(
              Class.forName("android.media.MediaCodecInfo$CodecCapabilities$CodecCapsIntf"), impl));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @ForType(CodecCapabilities.class)
  interface CodecCapabiltiesReflector {
    @Direct
    @Static
    CodecCapabilities createFromProfileLevel(String mime, int profile, int level);
  }

  @ForType(className = "android.media.MediaCodecInfo$CodecCapabilities$CodecCapsLegacyImpl")
  interface CodecCapsLegacyImplReflector {

    @Static
    Object createFromProfileLevel(String mime, int profile, int level);
  }
}
