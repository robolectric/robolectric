package org.robolectric.shadows;

import android.media.ToneGenerator;
import androidx.annotation.VisibleForTesting;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
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
 *
 * <p>Additionally, there is a maximum number of tones that this class can support. Tones are stored
 * in a first-in-first-out basis.
 */
@Implements(value = ToneGenerator.class)
public class ShadowToneGenerator {
  // A maximum value is required to avoid OOM errors
  // The number chosen here is arbitrary but should be reasonable for any use case of this class
  @VisibleForTesting static final int MAXIMUM_STORED_TONES = 2000;

  // This is static because using ToneGenerator has the object created or destroyed on demand.
  // This makes it very difficult for a test to get access the appropriate instance of
  // toneGenerator.
  // The list offers a record of all tones that have been started.
  // The list has a maximum size of MAXIMUM_STORED_TONES to avoid OOM errors
  private static final Deque<Tone> playedTones = new ArrayDeque<>();

  /**
   * This method will intercept calls to startTone and record the played tone into a static list.
   *
   * <p>Note in the original {@link ToneGenerator}, this function will start a tone. Subsequent
   * calls to this function will cancel the currently playing tone and play a new tone instead.
   * Since no tone is actually played and no process is started, this tone cannot be interrupted.
   */
  @Implementation
  protected boolean startTone(int toneType, int durationMs) {
    playedTones.add(Tone.create(toneType, Duration.ofMillis(durationMs)));
    if (playedTones.size() > MAXIMUM_STORED_TONES) {
      playedTones.removeFirst();
    }

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
