package org.robolectric.shadows;

import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import com.google.common.collect.ImmutableMap;
import java.nio.ByteBuffer;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Implements;

/** Implements {@link Equalizer} by relying on {@link ShadowAudioEffect}. */
@Implements(value = Equalizer.class)
public class ShadowEqualizer extends ShadowAudioEffect {

  // Default parameters and values needed in the Equalizer ctor.
  private static final ImmutableMap<ByteBuffer, ByteBuffer> DEFAULT_PARAMETERS =
      ImmutableMap.of(
          // (int) PARAM_NUM_BANDS -> (short) 5
          intToByteBuffer(0), shortToByteBuffer((short) 5),
          // (int) PARAM_GET_NUM_OF_PRESETS -> (short) 0
          intToByteBuffer(7), shortToByteBuffer((short) 0));

  @Nonnull
  private static ByteBuffer intToByteBuffer(int value) {
    return ShadowAudioEffect.createReadOnlyByteBuffer(AudioEffect.intToByteArray(value));
  }

  @Nonnull
  private static ByteBuffer shortToByteBuffer(short value) {
    return ShadowAudioEffect.createReadOnlyByteBuffer(AudioEffect.shortToByteArray(value));
  }

  @Override
  protected Optional<ByteBuffer> getDefaultParameter(ByteBuffer parameter) {
    return Optional.ofNullable(DEFAULT_PARAMETERS.get(parameter));
  }
}
