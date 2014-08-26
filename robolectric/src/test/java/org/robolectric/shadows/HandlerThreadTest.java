package org.robolectric.shadows;

import android.os.HandlerThread;
import android.os.Looper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class HandlerThreadTest {

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
    assertNotSame(handlerThread.getLooper(), Robolectric.application.getMainLooper());
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
    handlerThread = new HandlerThread("test") {
      @Override
      protected void onLooperPrepared() {
        wasCalled[0] = true;
      }
    };
    handlerThread.start();
    assertNotNull(handlerThread.getLooper());
    assertTrue(wasCalled[0]);
  }

  private static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      e.printStackTrace();
    }
  }
}
