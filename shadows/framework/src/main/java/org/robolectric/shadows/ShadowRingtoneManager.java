package org.robolectric.shadows;

import static android.media.RingtoneManager.TYPE_ALARM;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static android.media.RingtoneManager.TYPE_RINGTONE;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** A shadow implementation of {@link android.media.RingtoneManager}. */
@Implements(RingtoneManager.class)
public final class ShadowRingtoneManager {

  private static final Map<Uri, Ringtone> ringtoneMap = new HashMap<>();

  @Implementation
  protected static void setActualDefaultRingtoneUri(Context context, int type, Uri ringtoneUri) {
    Settings.System.putString(
        context.getContentResolver(),
        getSettingForType(type),
        ringtoneUri != null ? ringtoneUri.toString() : null);
  }

  private static String getSettingForType(int type) {
    if ((type & TYPE_RINGTONE) != 0) {
      return Settings.System.RINGTONE;
    } else if ((type & TYPE_NOTIFICATION) != 0) {
      return Settings.System.NOTIFICATION_SOUND;
    } else if ((type & TYPE_ALARM) != 0) {
      return Settings.System.ALARM_ALERT;
    } else {
      return null;
    }
  }

  public static void addRingtone(Uri ringtoneUri, Ringtone ringtone) {
    ringtoneMap.put(ringtoneUri, ringtone);
  }

  @Implementation
  protected static Ringtone getRingtone(Context context, Uri ringtoneUri) {
    if (!ringtoneMap.containsKey(ringtoneUri)) {
      return null;
    }

    return ringtoneMap.get(ringtoneUri);
  }
}
