package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.util.function.pooled.PooledLambda;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public final class PooledLambdaTest {
  @Test
  public void obtainRunnable() throws Exception {
    AtomicInteger result = new AtomicInteger(0);
    Runnable r = PooledLambda.obtainRunnable((a, b) -> result.set(a + b), 10, 20).recycleOnUse();
    r.run();
    assertThat(result.get()).isEqualTo(30);
  }

  @Test
  public void obtainMessage() throws Exception {
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AtomicBoolean called = new AtomicBoolean(false);
    Message msg = PooledLambda.obtainMessage((state) -> state.set(true), called);
    mainHandler.sendMessage(msg);
    ShadowLooper.idleMainLooper();
    assertThat(called.get()).isTrue();
  }
}
