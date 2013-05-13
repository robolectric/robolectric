package org.robolectric.shadows;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class MessengerTest {

  @Test
  public void testMessengerSend() throws Exception {
    Handler handler = new Handler();
    Messenger messenger = new Messenger(handler);

    ShadowLooper.pauseMainLooper();
    Message msg = Message.obtain(null, 123);
    messenger.send(msg);

    assertTrue(handler.hasMessages(123));
    ShadowHandler.runMainLooperOneTask();
    assertFalse(handler.hasMessages(123));
  }
}
