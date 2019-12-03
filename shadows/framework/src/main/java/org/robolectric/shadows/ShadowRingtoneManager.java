package org.robolectric.shadows;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link RingtoneManager}. */
@Implements(RingtoneManager.class)
public class ShadowRingtoneManager {
  /** Holds a map of all available {@link Ringtone}. */
  private static final ConcurrentHashMap<Uri, Ringtone> uriToRingtone = new ConcurrentHashMap<>();

  /** Adds a {@link Ringtone} to be used, keyed by {@link Uri}. */
  public static void addRingtone(Uri ringtoneUri, Ringtone ringtone) {
    uriToRingtone.put(ringtoneUri, ringtone);
  }

  @Nullable
  @Implementation
  protected static Ringtone getRingtone(Context context, Uri ringtoneUri) {
    return uriToRingtone.get(ringtoneUri);
  }

  @Resetter
  public static void reset() {
    uriToRingtone.clear();
  }

  public ShadowRingtoneManager() {}
}
