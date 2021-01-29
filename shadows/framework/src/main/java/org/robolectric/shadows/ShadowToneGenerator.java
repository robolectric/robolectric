package org.robolectric.shadows;

import android.media.ToneGenerator;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Shadow of ToneGenerator.
 *
 * <p>Records all tones that were passed to the class.
 *
 * <p>This class uses _static_ state to store the tones that were passed to it. This is because
 * users of the original class are expected to instantiate new instances of ToneGenerator on demand
 * and clean up the instance after use. This makes it messy to grab the correct instance of
 * ToneGenerator to properly shadow.
 */
@Implements(value = ToneGenerator.class)
public class ShadowToneGenerator {
  // This is static because using ToneGenerator has the object created or destroyed on demand.
  // This makes it very difficult for a test to get access the appropriate instance of
  // toneGenerator.
  // The list offers a record of all tones that have been started.
  private static final List<Tone> playedTones = new ArrayList<>();

  /**
   * This method will intercept calls to startTone and record the played tone into a static list.
   *
   * <p>Note in the original {@link ToneGenerator}, this function will start a tone. Subsequent
   * calls to this function will cancel the currently playing tone and play a new tone instead.
   * since no tone is actually played and no process is started, this tone cannot be interrupted.
   */
  @Implementation
  protected boolean startTone(int toneType, int durationMs) {
    playedTones.add(Tone.create(toneType, Duration.ofMillis(durationMs)));
    return true;
  }

  /**
   * This function returns the list of tones that the application requested to be played. Note that
   * this will return all tones requested by all ToneGenerators.
   *
   * @return A defensive copy of the list of tones played by all tone generators.
   */
  public static ImmutableList<Tone> getPlayedTones() {
    return ImmutableList.copyOf(playedTones);
  }

  @Resetter
  public static void reset() {
    playedTones.clear();
  }

  /** Stores data about a tone played by the ToneGenerator */
  @AutoValue
  public abstract static class Tone {

    /**
     * The type of the tone.
     *
     * @see ToneGenerator for a list of possible tones
     */
    public abstract int type();

    public abstract Duration duration();

    static Tone create(int type, Duration duration) {
      return new AutoValue_ShadowToneGenerator_Tone(type, duration);
    }
  }
}
