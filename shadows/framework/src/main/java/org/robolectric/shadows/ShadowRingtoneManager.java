package org.robolectric.shadows;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link android.media.RingtoneManager}. */
@Implements(RingtoneManager.class)
public class ShadowRingtoneManager {
  private static Map<Uri, Ringtone> ringtoneMap = new HashMap<>();

  /** Associates ringtone to a ringtoneUri for getRingtone */
  public static void addRingtone(Uri ringtoneUri, Ringtone ringtone) {
    ringtoneMap.put(ringtoneUri, ringtone);
  }

  @Nullable
  @Implementation
  /**
   * Return previously associated ringtone for given uri. Return null if matched uri is not found.
   */
  protected static Ringtone getRingtone(Context context, Uri ringtoneUri) {
    if (!ringtoneMap.containsKey(ringtoneUri)) {
      return null;
    }
    return ringtoneMap.get(ringtoneUri);
  }

  @Resetter
  public static void reset() {
    ringtoneMap.clear();
  }
}
