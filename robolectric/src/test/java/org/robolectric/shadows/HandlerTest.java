package org.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.TestRunnable;
import org.robolectric.util.Transcript;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.newInstanceOf;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class HandlerTest {
  private Transcript transcript;
  TestRunnable scratchRunnable = new TestRunnable();

  private Handler.Callback callback = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      hasHandlerCallbackHandledMessage = true;
      return false;
    }
  };

  private Boolean hasHandlerCallbackHandledMessage = false;

  @Before
  public void setUp() throws Exception {
    transcript = new Transcript();
  }

  @Test
  public void testInsertsRunnablesBasedOnLooper() throws Exception {
    Looper looper = newInstanceOf(Looper.class);

    Handler handler1 = new Handler(looper);
    handler1.post(new Say("first thing"));

    Handler handler2 = new Handler(looper);
    handler2.post(new Say("second thing"));

    shadowOf(looper).idle();

    transcript.assertEventsSoFar("first thing", "second thing");
  }

  @Test
  public void testDefaultConstructorUsesDefaultLooper() throws Exception {
    Handler handler1 = new Handler();
    handler1.post(new Say("first thing"));

    Handler handler2 = new Handler(Looper.myLooper());
    handler2.post(new Say("second thing"));

    shadowOf(Looper.myLooper()).idle();

    transcript.assertEventsSoFar("first thing", "second thing");
  }

  @Test
  public void testDifferentLoopersGetDifferentQueues() throws Exception {
    Looper looper1 = Robolectric.newInstanceOf(Looper.class);
    Robolectric.pauseLooper(looper1);

    Looper looper2 = Robolectric.newInstanceOf(Looper.class);
    Robolectric.pauseLooper(looper2);

    Handler handler1 = new Handler(looper1);
    handler1.post(new Say("first thing"));

    Handler handler2 = new Handler(looper2);
    handler2.post(new Say("second thing"));

    shadowOf(looper2).idle();

    transcript.assertEventsSoFar("second thing");
  }

  @Test
  public void shouldCallProvidedHandlerCallback() {
    Handler handler = new Handler(callback);
    handler.sendMessage(new Message());
    assertTrue(hasHandlerCallbackHandledMessage);
  }

  @Test
  public void testPostAndIdleMainLooper() throws Exception {
    new Handler().post(scratchRunnable);
    ShadowHandler.idleMainLooper();
    assertThat(scratchRunnable.wasRun).isTrue();
  }

  @Test
  public void postDelayedThenIdleMainLooper_shouldNotRunRunnable() throws Exception {
    new Handler().postDelayed(scratchRunnable, 1);
    ShadowHandler.idleMainLooper();
    assertThat(scratchRunnable.wasRun).isFalse();
  }

  @Test
  public void testPostDelayedThenRunMainLooperOneTask() throws Exception {
    new Handler().postDelayed(scratchRunnable, 1);
    ShadowHandler.runMainLooperOneTask();
    assertThat(scratchRunnable.wasRun).isTrue();
  }

  @Test
  public void testRemoveCallbacks() throws Exception {
    Handler handler = new Handler();
    ShadowLooper shadowLooper = shadowOf(handler.getLooper());
    shadowLooper.pause();
    handler.post(scratchRunnable);
    handler.removeCallbacks(scratchRunnable);

    shadowLooper.unPause();

    assertThat(scratchRunnable.wasRun).isFalse();
  }

  @Test
  public void testPostDelayedThenRunMainLooperToNextTask_shouldRunOneTask() throws Exception {
    new Handler().postDelayed(scratchRunnable, 1);
    ShadowHandler.runMainLooperToNextTask();
    assertThat(scratchRunnable.wasRun).isTrue();
  }

  @Test
  public void testPostDelayedTwiceThenRunMainLooperToNextTask_shouldRunMultipleTasks() throws Exception {
    TestRunnable task1 = new TestRunnable();
    TestRunnable task2 = new TestRunnable();

    new Handler().postDelayed(task1, 1);
    new Handler().postDelayed(task2, 1);

    ShadowHandler.runMainLooperToNextTask();
    assertThat(task1.wasRun).isTrue();
    assertThat(task2.wasRun).isTrue();
  }

  @Test
  public void testPostDelayedTwiceThenRunMainLooperOneTask_shouldRunOnlyOneTask() throws Exception {
    TestRunnable task1 = new TestRunnable();
    TestRunnable task2 = new TestRunnable();

    new Handler().postDelayed(task1, 1);
    new Handler().postDelayed(task2, 1);

    ShadowHandler.runMainLooperOneTask();
    assertThat(task1.wasRun).isTrue();
    assertThat(task2.wasRun).isFalse();
  }

  @Test
  public void testPostDelayedMultipleThenRunMainLooperOneTask_shouldRunMultipleTask() throws Exception {
    TestRunnable task1 = new TestRunnable();
    TestRunnable task2 = new TestRunnable();
    TestRunnable task3 = new TestRunnable();

    new Handler().postDelayed(task1, 1);
    new Handler().postDelayed(task2, 10);
    new Handler().postDelayed(task3, 100);

    ShadowHandler.runMainLooperToEndOfTasks();
    assertThat(task1.wasRun).isTrue();
    assertThat(task2.wasRun).isTrue();
    assertThat(task3.wasRun).isTrue();
  }

  @Test
  public void testPostAtFrontOfQueueThenRunMainLooperOneTaskAtATime_shouldRunFrontOfQueueTaskFirst() throws Exception {
    TestRunnable task1 = new TestRunnable();
    TestRunnable task2 = new TestRunnable();

    ShadowLooper.pauseMainLooper();
    new Handler().post(task1);
    boolean result = new Handler().postAtFrontOfQueue(task2);

    assertTrue(result);

    ShadowHandler.runMainLooperOneTask();
    assertThat(task2.wasRun).isTrue();
    assertThat(task1.wasRun).isFalse();
    ShadowHandler.runMainLooperOneTask();
    assertThat(task1.wasRun).isTrue();
  }

  @Test
  public void testSendMessageAtFrontOfQueueThenRunMainLooperOneMsgAtATime_shouldRunFrontOfQueueMsgFirst() throws Exception {
    Handler handler = new Handler();

    ShadowLooper.pauseMainLooper();
    // Post two messages to handler. Handle first message and confirm that msg posted
    // to front is removed.
    handler.obtainMessage(123).sendToTarget();
    Message frontMsg = handler.obtainMessage(345);
    boolean result = handler.sendMessageAtFrontOfQueue(frontMsg);

    assertTrue(result);

    assertTrue(handler.hasMessages(123));
    assertTrue(handler.hasMessages(345));
    ShadowHandler.runMainLooperOneTask();
    assertTrue(handler.hasMessages(123));
    assertFalse(handler.hasMessages(345));
    ShadowHandler.runMainLooperOneTask();
    assertFalse(handler.hasMessages(123));
    assertFalse(handler.hasMessages(345));
  }

  @Test
  public void sendEmptyMessage_addMessageToQueue() {
    Robolectric.pauseMainLooper();
    Handler handler = new Handler();
    assertThat(handler.hasMessages(123)).isFalse();
    handler.sendEmptyMessage(123);
    assertThat(handler.hasMessages(456)).isFalse();
    assertThat(handler.hasMessages(123)).isTrue();
    Robolectric.idleMainLooper(0);
    assertThat(handler.hasMessages(123)).isFalse();
  }

  @Test
  public void sendEmptyMessageDelayed_sendsMessageAtCorrectTime() {
    Robolectric.pauseMainLooper();
    Handler handler = new Handler();
    handler.sendEmptyMessageDelayed(123, 500);
    assertThat(handler.hasMessages(123)).isTrue();
    Robolectric.idleMainLooper(100);
    assertThat(handler.hasMessages(123)).isTrue();
    Robolectric.idleMainLooper(400);
    assertThat(handler.hasMessages(123)).isFalse();
  }

  @Test
  public void sendMessageAtTime_sendsMessageAtCorrectTime() {
    Robolectric.pauseMainLooper();
    Handler handler = new Handler();
    Message message = handler.obtainMessage(123);
    handler.sendMessageAtTime(message, 500);
    assertThat(handler.hasMessages(123)).isTrue();
    Robolectric.idleMainLooper(100);
    assertThat(handler.hasMessages(123)).isTrue();
    Robolectric.idleMainLooper(400);
    assertThat(handler.hasMessages(123)).isFalse();
  }

  @Test
  public void removeMessages_takesMessageOutOfQueue() {
    Robolectric.pauseMainLooper();
    Handler handler = new Handler();
    handler.sendEmptyMessageDelayed(123, 500);
    handler.removeMessages(123);
    assertThat(handler.hasMessages(123)).isFalse();
  }

  @Test
  public void removeMessage_withSpecifiedObject() throws Exception {
    Robolectric.pauseMainLooper();
    Handler handler = new Handler();
    Message.obtain(handler, 123, "foo").sendToTarget();
    Message.obtain(handler, 123, "bar").sendToTarget();

    assertThat(handler.hasMessages(123)).isTrue();
    assertThat(handler.hasMessages(123, "foo")).isTrue();
    assertThat(handler.hasMessages(123, "bar")).isTrue();
    assertThat(handler.hasMessages(123, "baz")).isFalse();

    handler.removeMessages(123, "foo");
    assertThat(handler.hasMessages(123)).isTrue();

    handler.removeMessages(123, "bar");
    assertThat(handler.hasMessages(123)).isFalse();
  }

  @Test
  public void testHasMessagesWithWhatAndObject() {
    Robolectric.pauseMainLooper();
    Object testObject = new Object();
    Handler handler = new Handler();
    Message message = handler.obtainMessage(123, testObject);

    assertFalse(handler.hasMessages(123, testObject));

    handler.sendMessage(message);

    assertTrue(handler.hasMessages(123, testObject));
  }

  @Test
  public void testSendToTarget() {
    Robolectric.pauseMainLooper();
    Object testObject = new Object();
    Handler handler = new Handler();
    Message message = handler.obtainMessage(123, testObject);

    assertThat(handler).isEqualTo(message.getTarget());

    message.sendToTarget();

    assertTrue(handler.hasMessages(123, testObject));
  }

  @Test
  public void removeMessages_removesFromLooperQueueAsWell() {
    final boolean[] wasRun = new boolean[1];
    Robolectric.pauseMainLooper();
    Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        wasRun[0] = true;
      }
    };
    handler.sendEmptyMessageDelayed(123, 500);
    handler.removeMessages(123);
    Robolectric.unPauseMainLooper();
    assertThat(wasRun[0]).isFalse();
  }

  @Test
  public void shouldRemoveAllMessages() throws Exception {
    final boolean[] wasRun = new boolean[1];
    Robolectric.pauseMainLooper();
    Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        wasRun[0] = true;
      }
    };
    handler.sendEmptyMessage(0);
    handler.removeCallbacksAndMessages(null);
    Robolectric.unPauseMainLooper();
    assertThat(wasRun[0]).isFalse();
  }

  @Test
  public void shouldRemoveSingleMessage() throws Exception {
    final List<Object> objects = new ArrayList<Object>();
    Robolectric.pauseMainLooper();

    Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        objects.add(msg.obj);
      }
    };

    Object firstObj = new Object();
    handler.sendMessage(handler.obtainMessage(0, firstObj));

    Object secondObj = new Object();
    handler.sendMessage(handler.obtainMessage(0, secondObj));

    handler.removeCallbacksAndMessages(secondObj);
    Robolectric.unPauseMainLooper();

    assertThat(objects.contains(firstObj)).isTrue();
    assertThat(objects.contains(secondObj)).isFalse();
  }

  @Test
  public void shouldObtainMessage() throws Exception {
    Message m0 = new Handler().obtainMessage();
    assertThat(m0.what).isEqualTo(0);
    assertThat(m0.obj).isNull();

    Message m1 = new Handler().obtainMessage(1);
    assertThat(m1.what).isEqualTo(1);
    assertThat(m1.obj).isNull();

    Message m2 = new Handler().obtainMessage(1, "foo");
    assertThat(m2.what).isEqualTo(1);
    assertThat(m2.obj).isEqualTo((Object) "foo");

    Message m3 = new Handler().obtainMessage(1, 2, 3);
    assertThat(m3.what).isEqualTo(1);
    assertThat(m3.arg1).isEqualTo(2);
    assertThat(m3.arg2).isEqualTo(3);
    assertThat(m3.obj).isNull();

    Message m4 = new Handler().obtainMessage(1, 2, 3, "foo");
    assertThat(m4.what).isEqualTo(1);
    assertThat(m4.arg1).isEqualTo(2);
    assertThat(m4.arg2).isEqualTo(3);
    assertThat(m4.obj).isEqualTo((Object) "foo");
  }

  @Test
  public void shouldSetWhenOnMessage() throws Exception {
    final List<Message>  msgs = new ArrayList<Message>();
    Handler h = new Handler(new Handler.Callback() {
      @Override
      public boolean handleMessage(Message msg) {
        msgs.add(msg);
        return false;
      }
    });

    h.sendEmptyMessage(0);
    h.sendEmptyMessageDelayed(0, 4000l);
    Robolectric.getUiThreadScheduler().advanceToLastPostedRunnable();
    h.sendEmptyMessageDelayed(0, 12000l);
    Robolectric.getUiThreadScheduler().advanceToLastPostedRunnable();
    assertThat(msgs.size()).isEqualTo(3);

    Message m0 = msgs.get(0);
    Message m1 = msgs.get(1);
    Message m2 = msgs.get(2);

    assertThat(m0.getWhen()).isEqualTo(0l);
    assertThat(m1.getWhen()).isEqualTo(4000l);
    assertThat(m2.getWhen()).isEqualTo(16000l);
  }

  @Test
  public void shouldRemoveMessageFromQueueBeforeDispatching() throws Exception {
    Handler h = new Handler(Looper.myLooper()) {
      @Override
      public void handleMessage(Message msg) {
        assertFalse(hasMessages(0));
      }
    };
    h.sendEmptyMessage(0);
    h.sendMessageAtFrontOfQueue(h.obtainMessage());
  }


  private class Say implements Runnable {
    private String event;

    public Say(String event) {
      this.event = event;
    }

    @Override
    public void run() {
      transcript.add(event);
    }
  }
}
