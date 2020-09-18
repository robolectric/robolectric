package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.hardware.input.InputManager;
import android.view.MotionEvent;
import android.view.VerifiedMotionEvent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowInputManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = R)
public class ShadowInputManagerTest {
  private InputManager inputManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    inputManager = context.getSystemService(InputManager.class);
  }

  @Test
  public void verifyMotionEvent() {
    MotionEvent motionEvent =
        MotionEvent.obtain(12345, 23456, MotionEvent.ACTION_UP, 30.0f, 40.0f, 0);
    VerifiedMotionEvent verifiedMotionEvent =
        (VerifiedMotionEvent) inputManager.verifyInputEvent(motionEvent);

    assertThat(verifiedMotionEvent.getRawX()).isEqualTo(30.0f);
    assertThat(verifiedMotionEvent.getRawY()).isEqualTo(40.0f);
    assertThat(verifiedMotionEvent.getEventTimeNanos()).isEqualTo(23456000000L);
    assertThat(verifiedMotionEvent.getDownTimeNanos()).isEqualTo(12345000000L);
  }
}
