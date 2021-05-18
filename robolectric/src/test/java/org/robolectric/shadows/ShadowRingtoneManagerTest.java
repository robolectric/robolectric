package org.robolectric.shadows;

import static android.media.RingtoneManager.TYPE_ALARM;
import static android.media.RingtoneManager.TYPE_RINGTONE;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.media.Ringtone;
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
  public void getActualDefaultRingtoneUri_returnSetUri() {
    Context appContext = ApplicationProvider.getApplicationContext();
    Uri uri = Uri.parse("content://media/external/330");
    int type = TYPE_RINGTONE;

    RingtoneManager.setActualDefaultRingtoneUri(appContext, type, uri);

    assertThat(RingtoneManager.getActualDefaultRingtoneUri(appContext, type)).isEqualTo(uri);
  }

  @Test
  public void getActualDefaultRingtoneUri_noUriSet_returnNull() {
    Context appContext = ApplicationProvider.getApplicationContext();
    int type = TYPE_RINGTONE;

    assertThat(RingtoneManager.getActualDefaultRingtoneUri(appContext, type)).isNull();
  }

  @Test
  public void getActualDefaultRingtoneUri_uriSetForDifferentType() {
    Context appContext = ApplicationProvider.getApplicationContext();
    int type = TYPE_RINGTONE;
    Uri uri = Uri.parse("content://media/external/330");
    RingtoneManager.setActualDefaultRingtoneUri(appContext, type, uri);

    assertThat(RingtoneManager.getActualDefaultRingtoneUri(appContext, TYPE_ALARM)).isNull();
  }

  @Test
  public void getRingtone_noUriSet_returnsNull() {
    Context appContext = ApplicationProvider.getApplicationContext();
    Uri uri = Uri.parse("content://media/external/330");

    assertThat(RingtoneManager.getRingtone(appContext, uri)).isNull();
  }

  @Test
  public void getRingtone_uriSet_returnsNotNull() {
    Context appContext = ApplicationProvider.getApplicationContext();
    Uri uri = Uri.parse("content://media/external/330");

    Ringtone ringtone = mock(Ringtone.class);
    ShadowRingtoneManager.addRingtone(uri, ringtone);

    assertThat(RingtoneManager.getRingtone(appContext, uri)).isNotNull();
  }
}
