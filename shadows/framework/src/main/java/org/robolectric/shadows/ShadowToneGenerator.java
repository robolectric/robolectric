package org.robolectric.shadows;

import android.media.ToneGenerator;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of ToneGenerator.
 *
 * <p>Records all tones that were passed to the class.
 */
@Implements(value = ToneGenerator.class)
public class ShadowToneGenerator {
  private final List<Tone> playedTones = new ArrayList<>();

  @Implementation
  protected boolean startTone(int toneType, int durationMs) {
    playedTones.add(Tone.create(toneType, Duration.ofMillis(durationMs)));
    return false;
  }

  public ImmutableList<Tone> getPlayedTones() {
    return ImmutableList.copyOf(playedTones);
  }

  @AutoValue
  abstract static class Tone {
    public abstract int type();

    public abstract Duration duration();

    static Tone create(int type, Duration duration) {
      return new AutoValue_ShadowToneGenerator_Tone(type, duration);
    }
  }
}
