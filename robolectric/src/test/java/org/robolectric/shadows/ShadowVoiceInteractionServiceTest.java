package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.ComponentName;
import android.os.Bundle;
import android.service.voice.VoiceInteractionService;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/** Test for ShadowVoiceInteractionService. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Q)
public class ShadowVoiceInteractionServiceTest {

  /** VoiceInteractionService needs to be extended to function. */
  public static class TestVoiceInteractionService extends VoiceInteractionService {
    public TestVoiceInteractionService() {}
  }

  private VoiceInteractionService service;
  private ShadowVoiceInteractionService shadowService;

  @Before
  public void setUp() {
    service = Robolectric.buildService(TestVoiceInteractionService.class).get();
    shadowService = shadowOf(service);
    ShadowVoiceInteractionService.reset();
  }

  @Test
  public void testSetUiHintsInvoked_returnsValues() {
    Bundle bundle1 = new Bundle();
    bundle1.putString("testKey", "value");
    Bundle bundle2 = new Bundle();
    bundle2.putString("secondKey", "value");

    service.onReady();
    service.setUiHints(bundle1);
    service.setUiHints(bundle2);

    assertThat(shadowService.getLastUiHintBundle()).isEqualTo(bundle2);
    assertThat(shadowService.getPreviousUiHintBundles()).containsExactly(bundle1, bundle2);
  }

  @Test
  public void testSetUiHintsNotInvoked_returnsValues() {
    service.onReady();
    assertThat(shadowService.getLastUiHintBundle()).isNull();
    assertThat(shadowService.getPreviousUiHintBundles()).isEmpty();
  }

  @Test(expected = NullPointerException.class)
  public void testSetUiHintsInvokedBeforeServiceReady_throwsException() {
    service.setUiHints(new Bundle());
  }

  @Test
  public void setActiveService_returnsDefaultFalse() {
    assertThat(
            VoiceInteractionService.isActiveService(
                ApplicationProvider.getApplicationContext(), new ComponentName("test", "test")))
        .isFalse();
  }

  @Test
  public void setActiveService_returnsChangedValue() {
    ShadowVoiceInteractionService.setActiveService(new ComponentName("test", "test"));
    assertThat(
            VoiceInteractionService.isActiveService(
                ApplicationProvider.getApplicationContext(), new ComponentName("test", "test")))
        .isTrue();
  }

  @Test
  public void resetter_resetsActiveServiceValue() {
    ShadowVoiceInteractionService.setActiveService(new ComponentName("test", "test"));

    ShadowVoiceInteractionService.reset();

    assertThat(
            VoiceInteractionService.isActiveService(
                ApplicationProvider.getApplicationContext(), new ComponentName("test", "test")))
        .isFalse();
  }
}
