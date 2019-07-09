package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowBroadcastReceiver} */
@RunWith(AndroidJUnit4.class)
public class ShadowBroadcastReceiverTest {

  private BroadcastReceiver receiver;
  private PendingResult pendingResult;

  @Before
  public void setup() {
    receiver = new MyBroadcastReceiver();
    pendingResult = ShadowBroadcastPendingResult.create(1, "result", new Bundle(), true);
    receiver.setPendingResult(pendingResult);
  }

  @Test
  public void testWithoutGoAsync() {
    assertThat(shadowOf(receiver).wentAsync()).isFalse();
    assertThat(shadowOf(receiver).getOriginalPendingResult()).isSameInstanceAs(pendingResult);
  }

  @Test
  public void testWithGoAsync() {
    final PendingResult pendingResultFromGoAsync = receiver.goAsync();
    assertThat(shadowOf(receiver).wentAsync()).isTrue();
    assertThat(pendingResultFromGoAsync).isEqualTo(pendingResult);
    assertThat(shadowOf(receiver).getOriginalPendingResult()).isSameInstanceAs(pendingResult);
  }

  private static class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {}
  }
}
