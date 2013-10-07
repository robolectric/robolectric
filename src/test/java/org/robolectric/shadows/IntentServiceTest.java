package org.robolectric.shadows;

import android.app.IntentService;
import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class IntentServiceTest {
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

  private class TestIntentService extends IntentService {
    public TestIntentService() {
      super("TestIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
  }
}
