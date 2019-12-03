package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ShadowRingtoneManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowRingtoneManagerTest {
  private Context context;
  private Ringtone ringtone;
  private Ringtone otherRingtone;

  private final Uri uri = Uri.parse("test string");
  private final Uri otherUri = Uri.parse("other test string");

  @Before
  public void setup() throws Exception {
    context = ApplicationProvider.getApplicationContext();
    ringtone = ShadowRingtone.create();
    otherRingtone = ShadowRingtone.create();
  }

  @Test
  public void getRingtone_addRingtoneNotCalled_shouldReturnNull() {
    assertThat(RingtoneManager.getRingtone(context, uri)).isNull();
  }

  @Test
  public void getRingtone_addRingtoneCalled_shouldReturnRingtone() {
    ShadowRingtoneManager.addRingtone(uri, ringtone);

    RingtoneManager.getRingtone(context, uri);

    assertThat(RingtoneManager.getRingtone(context, uri)).isSameInstanceAs(ringtone);
  }

  @Test
  public void getRingtone_addRingtoneCalledTwice_shouldReturnCorrectRingtone() {
    ShadowRingtoneManager.addRingtone(uri, ringtone);
    ShadowRingtoneManager.addRingtone(otherUri, otherRingtone);

    RingtoneManager.getRingtone(context, uri);

    assertThat(RingtoneManager.getRingtone(context, uri)).isSameInstanceAs(ringtone);
  }
}
