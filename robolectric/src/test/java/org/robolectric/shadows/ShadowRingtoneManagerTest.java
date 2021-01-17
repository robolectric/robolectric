package org.robolectric.shadows;

import static android.media.RingtoneManager.TYPE_ALARM;
import static android.media.RingtoneManager.TYPE_RINGTONE;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowRingtoneManager}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowRingtoneManagerTest {

  @Test
  public void getRingtone_returnSetUri() {
    Context appContext = ApplicationProvider.getApplicationContext();
    Uri uri = Uri.parse("content://media/external/330");
    int type = TYPE_RINGTONE;

    RingtoneManager.setActualDefaultRingtoneUri(appContext, type, uri);

    assertThat(RingtoneManager.getActualDefaultRingtoneUri(appContext, type)).isEqualTo(uri);
  }

  @Test
  public void getRingtone_noUriSet_returnNull() {
    Context appContext = ApplicationProvider.getApplicationContext();
    int type = TYPE_RINGTONE;

    assertThat(RingtoneManager.getActualDefaultRingtoneUri(appContext, type)).isNull();
  }

  @Test
  public void getRingtone_uriSetForDifferentType() {
    Context appContext = ApplicationProvider.getApplicationContext();
    int type = TYPE_RINGTONE;
    Uri uri = Uri.parse("content://media/external/330");
    RingtoneManager.setActualDefaultRingtoneUri(appContext, type, uri);

    assertThat(RingtoneManager.getActualDefaultRingtoneUri(appContext, TYPE_ALARM)).isNull();
  }
}
