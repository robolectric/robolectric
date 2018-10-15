package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.IntentService;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowIntentServiceTest {
  @Test
  public void shouldSetIntentRedelivery() {
    IntentService intentService = new TestIntentService();
    ShadowIntentService shadowIntentService = shadowOf(intentService);
    assertThat(shadowIntentService.getIntentRedelivery()).isFalse();
    intentService.setIntentRedelivery(true);
    assertThat(shadowIntentService.getIntentRedelivery()).isTrue();
    intentService.setIntentRedelivery(false);
    assertThat(shadowIntentService.getIntentRedelivery()).isFalse();
  }

  private static class TestIntentService extends IntentService {
    public TestIntentService() {
      super("TestIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
  }
}
