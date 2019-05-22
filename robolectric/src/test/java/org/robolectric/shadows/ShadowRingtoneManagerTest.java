package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/** Unit test for {@link ShadowRingtoneManager}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowRingtoneManagerTest {
  private Application context;
  @Mock private Ringtone mockRingtone;
  @Mock private Ringtone mockRingtone2;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @After
  public void tareDown() {
    ShadowRingtoneManager.reset();
  }

  @Test
  public void getRingtone_hasRingtone() {
    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    ShadowRingtoneManager.addRingtone(uri, mockRingtone);
    assertThat(RingtoneManager.getRingtone(context, uri)).isEqualTo(mockRingtone);
  }

  @Test
  public void getRingtone_hasNoRingtone() {
    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    assertThat(RingtoneManager.getRingtone(context, uri)).isNull();
  }

  @Test
  public void getRingtone_hasTwoRingtone() {
    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    ShadowRingtoneManager.addRingtone(uri, mockRingtone);

    Uri uri2 = Uri.parse("//test");
    ShadowRingtoneManager.addRingtone(uri2, mockRingtone2);

    assertThat(RingtoneManager.getRingtone(context, uri)).isEqualTo(mockRingtone);
    assertThat(RingtoneManager.getRingtone(context, uri2)).isEqualTo(mockRingtone2);
  }

  @Test
  public void getRingtone_hasMismatchedRingtone() {
    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    ShadowRingtoneManager.addRingtone(uri, mockRingtone);
    assertThat(RingtoneManager.getRingtone(context, uri)).isEqualTo(mockRingtone);

    Uri uri2 = Uri.parse("//test");
    assertThat(RingtoneManager.getRingtone(context, uri2)).isNull();
  }
}
