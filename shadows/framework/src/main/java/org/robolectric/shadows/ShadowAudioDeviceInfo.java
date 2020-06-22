package third_party.java_src.robolectric.shadows.framework.src.main.java.org.robolectric.shadows;

import android.media.AudioDeviceInfo;
import org.robolectric.annotation.Implements;

/** Implements {@link AudioDeviceInfo} by shadowing its native methods. */
@Implements(value = AudioDeviceInfo.class)
public class ShadowAudioDeviceInfo {}
