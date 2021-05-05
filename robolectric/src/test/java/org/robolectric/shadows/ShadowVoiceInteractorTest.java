package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowVoiceInteractor}. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Q)
public final class ShadowVoiceInteractorTest {

  @Test
  public void testGetDirectActionsInvalidationCount() {
    Activity testActivity = buildActivity(Activity.class).create().get();
    shadowOf(testActivity).initializeVoiceInteractor();
    testActivity.getVoiceInteractor().notifyDirectActionsChanged();
    ShadowVoiceInteractor shadowVoiceInteractor = shadowOf(testActivity.getVoiceInteractor());
    assertThat(shadowVoiceInteractor.getDirectActionsInvalidationCount()).isEqualTo(1);
    AssertionError unused =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(shadowVoiceInteractor.getDirectActionsInvalidationCount())
                    .isEqualTo(2));
    testActivity.getVoiceInteractor().notifyDirectActionsChanged();
    assertThat(shadowVoiceInteractor.getDirectActionsInvalidationCount()).isEqualTo(2);
  }
}
