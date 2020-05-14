package org.robolectric.shadows;

import android.media.audiofx.AudioEffect;
import android.media.audiofx.DynamicsProcessing;
import com.google.common.collect.ImmutableMap;
import java.nio.ByteBuffer;
import java.util.Optional;
import org.robolectric.annotation.Implements;

/** Implements {@link DynamicsProcessing} by relying on {@link ShadowAudioEffect}. */
@Implements(value = DynamicsProcessing.class, minSdk = 28)
public class ShadowDynamicsProcessing extends ShadowAudioEffect {

  // Default parameters needed in the DynamicsProcessing ctor.
  private static final ImmutableMap<ByteBuffer, ByteBuffer> DEFAULT_PARAMETERS =
      ImmutableMap.of(
          intToByteBuffer(0x10), // DynamicsProcessing.PARAM_GET_CHANNEL_COUNT
          intToByteBuffer(2) // Default channel count = STEREO
          );

  @Override
  protected Optional<ByteBuffer> getDefaultParameter(ByteBuffer parameter) {
    return Optional.ofNullable(DEFAULT_PARAMETERS.get(parameter));
  }

  private static ByteBuffer intToByteBuffer(int value) {
    return ShadowAudioEffect.createReadOnlyByteBuffer(AudioEffect.intToByteArray(value));
  }
}
