package org.robolectric.shadows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.os.HandlerThread;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowHandlerThreadTest {

  private HandlerThread handlerThread;

  @After
  public void tearDown() throws Exception {
    // Try to ensure we've exited the thread at the end of each test
    if ( handlerThread != null ) {
      handlerThread.quit();
      handlerThread.join();
    }
  }

  @Test
  public void shouldReturnLooper() throws Exception {
    handlerThread = new HandlerThread("test");
    handlerThread.start();
    assertNotNull(handlerThread.getLooper());
    assertNotSame(
        handlerThread.getLooper(), ApplicationProvider.getApplicationContext().getMainLooper());
  }

  @Test
  public void shouldReturnNullIfThreadHasNotBeenStarted() throws Exception {
    handlerThread = new HandlerThread("test");
    assertNull(handlerThread.getLooper());
  }

  @Test
  public void shouldQuitLooperAndThread() throws Exception {
    handlerThread = new HandlerThread("test");
    Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
    handlerThread.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
    handlerThread.start();
    assertTrue(handlerThread.isAlive());
    assertTrue(handlerThread.quit());
    handlerThread.join();
    assertFalse(handlerThread.isAlive());
    handlerThread = null;
  }

  @Test
  public void shouldStopThreadIfLooperIsQuit() throws Exception {
    handlerThread = new HandlerThread("test1");
    handlerThread.start();
    Looper looper = handlerThread.getLooper();
    assertFalse(shadowOf(looper).quit);
    looper.quit();
    handlerThread.join();
    assertFalse(handlerThread.isAlive());
    assertTrue(shadowOf(looper).quit);
    handlerThread = null;
  }

  @Test
  public void shouldCallOnLooperPrepared() throws Exception {
    final Boolean[] wasCalled = new Boolean[] { false };
    final CountDownLatch latch = new CountDownLatch(1);
    handlerThread = new HandlerThread("test") {
      @Override
      protected void onLooperPrepared() {
        wasCalled[0] = true;
        latch.countDown();
      }
    };
    handlerThread.start();
    try {
      assertNotNull(handlerThread.getLooper());
      latch.await(1, TimeUnit.SECONDS);
      assertTrue(wasCalled[0]);
    } finally {
      handlerThread.quit();
    }
  }

  private static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      e.printStackTrace();
    }
  }
}
