package org.robolectric.fakes;

import android.os.SystemVibrator;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * @deprecated Vibrator support has switched to a shadow implmentation instead. This class will be
 * removed in Robolectric 3.8.
 *
 * Prefer instead:-
 *
 * ```java
 *   Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
 *   boolean vibrating = shadowOf(vibrator).isVibrating()
 * ```
 */
@Deprecated
@DoNotInstrument
public class RoboVibrator extends SystemVibrator {

}
