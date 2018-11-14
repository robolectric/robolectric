package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TestRunnable;

@RunWith(AndroidJUnit4.class)
public class ShadowHandlerTest {
  private List<String> transcript;
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
    transcript = new ArrayList<>();
  }

  @Test
  public void testInsertsRunnablesBasedOnLooper() throws Exception {
    Looper looper = newLooper(false);

    Handler handler1 = new Handler(looper);
    handler1.post(new Say("first thing"));

    Handler handler2 = new Handler(looper);
    handler2.post(new Say("second thing"));

    shadowOf(looper).idle();

    assertThat(transcript).containsExactly("first thing", "second thing");
  }

  @Test
  public void testDefaultConstructorUsesDefaultLooper() throws Exception {
    Handler handler1 = new Handler();
    handler1.post(new Say("first thing"));

    Handler handler2 = new Handler(Looper.myLooper());
    handler2.post(new Say("second thing"));

    shadowOf(Looper.myLooper()).idle();

    assertThat(transcript).containsExactly("first thing", "second thing");
  }

  private static Looper newLooper(boolean canQuit) {
    return ReflectionHelpers.callConstructor(Looper.class, from(boolean.class, canQuit));
  }
  
  @Test
  public void testDifferentLoopersGetDifferentQueues() throws Exception {
    Looper looper1 = newLooper(true);
    ShadowLooper.pauseLooper(looper1);

    Looper looper2 = newLooper(true);
    ShadowLooper.pauseLooper(looper2);
    // Make sure looper has a different scheduler to the first
    shadowOf(looper2.getQueue()).setScheduler(new Scheduler());

    Handler handler1 = new Handler(looper1);
    handler1.post(new Say("first thing"));

    Handler handler2 = new Handler(looper2);
    handler2.post(new Say("second thing"));

    shadowOf(looper2).idle();

    assertThat(transcript).containsExactly("second thing");
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
    ShadowLooper.idleMainLooper();
    assertThat(scratchRunnable.wasRun).isTrue();
  }

  @Test
  public void postDelayedThenIdleMainLooper_shouldNotRunRunnable() throws Exception {
    new Handler().postDelayed(scratchRunnable, 1);
    ShadowLooper.idleMainLooper();
    assertThat(scratchRunnable.wasRun).isFalse();
  }

  @Test
  public void testPostDelayedThenRunMainLooperOneTask() throws Exception {
    new Handler().postDelayed(scratchRunnable, 1);
    ShadowLooper.runMainLooperOneTask();
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
    ShadowLooper.runMainLooperToNextTask();
    assertThat(scratchRunnable.wasRun).isTrue();
  }

  @Test
  public void testPostDelayedTwiceThenRunMainLooperToNextTask_shouldRunMultipleTasks() throws Exception {
    TestRunnable task1 = new TestRunnable();
    TestRunnable task2 = new TestRunnable();

    new Handler().postDelayed(task1, 1);
    new Handler().postDelayed(task2, 1);

    ShadowLooper.runMainLooperToNextTask();
    assertThat(task1.wasRun).isTrue();
    assertThat(task2.wasRun).isTrue();
  }

  @Test
  public void testPostDelayedTwiceThenRunMainLooperOneTask_shouldRunOnlyOneTask() throws Exception {
    TestRunnable task1 = new TestRunnable();
    TestRunnable task2 = new TestRunnable();

    new Handler().postDelayed(task1, 1);
    new Handler().postDelayed(task2, 1);

    ShadowLooper.runMainLooperOneTask();
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

    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
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

    ShadowLooper.runMainLooperOneTask();
    assertThat(task2.wasRun).isTrue();
    assertThat(task1.wasRun).isFalse();
    ShadowLooper.runMainLooperOneTask();
    assertThat(task1.wasRun).isTrue();
  }

  @Test
  public void testNestedPost_shouldRunLast() throws Exception {
    ShadowLooper.pauseMainLooper();
    final List<Integer> order = new ArrayList<>();
    final Handler h = new Handler();
    h.post(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        h.post(new Runnable() {
          @Override
          public void run() {
            order.add(3);
          }
        });
      }
    });
    h.post(new Runnable() {
      @Override
      public void run() {
        order.add(2);
      }
    });
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    assertThat(order).containsExactly(1, 2, 3);
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
    ShadowLooper.runMainLooperOneTask();
    assertTrue(handler.hasMessages(123));
    assertFalse(handler.hasMessages(345));
    ShadowLooper.runMainLooperOneTask();
    assertFalse(handler.hasMessages(123));
    assertFalse(handler.hasMessages(345));
  }

  @Test
  public void sendEmptyMessage_addMessageToQueue() {
    ShadowLooper.pauseMainLooper();
    Handler handler = new Handler();
    assertThat(handler.hasMessages(123)).isFalse();
    handler.sendEmptyMessage(123);
    assertThat(handler.hasMessages(456)).isFalse();
    assertThat(handler.hasMessages(123)).isTrue();
    ShadowLooper.idleMainLooper(0);
    assertThat(handler.hasMessages(123)).isFalse();
  }

  @Test
  public void sendEmptyMessageDelayed_sendsMessageAtCorrectTime() {
    ShadowLooper.pauseMainLooper();
    Handler handler = new Handler();
    handler.sendEmptyMessageDelayed(123, 500);
    assertThat(handler.hasMessages(123)).isTrue();
    ShadowLooper.idleMainLooper(100);
    assertThat(handler.hasMessages(123)).isTrue();
    ShadowLooper.idleMainLooper(400);
    assertThat(handler.hasMessages(123)).isFalse();
  }

  @Test
  public void sendMessageAtTime_sendsMessageAtCorrectTime() {
    ShadowLooper.pauseMainLooper();
    Handler handler = new Handler();
    Message message = handler.obtainMessage(123);
    handler.sendMessageAtTime(message, 500);
    assertThat(handler.hasMessages(123)).isTrue();
    ShadowLooper.idleMainLooper(100);
    assertThat(handler.hasMessages(123)).isTrue();
    ShadowLooper.idleMainLooper(400);
    assertThat(handler.hasMessages(123)).isFalse();
  }

  @Test
  public void removeMessages_takesMessageOutOfQueue() {
    ShadowLooper.pauseMainLooper();
    Handler handler = new Handler();
    handler.sendEmptyMessageDelayed(123, 500);
    handler.removeMessages(123);
    assertThat(handler.hasMessages(123)).isFalse();
  }

  @Test
  public void removeMessage_withSpecifiedObject() throws Exception {
    ShadowLooper.pauseMainLooper();
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
    ShadowLooper.pauseMainLooper();
    Object testObject = new Object();
    Handler handler = new Handler();
    Message message = handler.obtainMessage(123, testObject);

    assertFalse(handler.hasMessages(123, testObject));

    handler.sendMessage(message);

    assertTrue(handler.hasMessages(123, testObject));
  }

  @Test
  public void testSendToTarget() {
    ShadowLooper.pauseMainLooper();
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
    ShadowLooper.pauseMainLooper();
    Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        wasRun[0] = true;
      }
    };
    handler.sendEmptyMessageDelayed(123, 500);
    handler.removeMessages(123);
    ShadowLooper.unPauseMainLooper();
    assertThat(wasRun[0]).isFalse();
  }

  @Test
  public void scheduler_wontDispatchRemovedMessage_evenIfMessageReused() {
    final ArrayList<Long> runAt = new ArrayList<>();
    ShadowLooper.pauseMainLooper();
    Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        runAt.add(shadowOf(Looper.myLooper()).getScheduler().getCurrentTime());
      }
    };

    final long startTime = Robolectric.getForegroundThreadScheduler().getCurrentTime();
    Message msg = handler.obtainMessage(123);
    handler.sendMessageDelayed(msg, 200);
    handler.removeMessages(123);
    Message newMsg = handler.obtainMessage(123);
    assertThat(newMsg).named("new message").isSameAs(msg);
    handler.sendMessageDelayed(newMsg, 400);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    // Original implementation had a bug which caused reused messages to still
    // be invoked at their original post time.
    assertThat(runAt).named("handledAt").containsExactly(startTime + 400L);
  }

  @Test
  public void shouldRemoveAllCallbacksAndMessages() throws Exception {
    final boolean[] wasRun = new boolean[1];
    ShadowLooper.pauseMainLooper();
    Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        wasRun[0] = true;
      }
    };
    handler.sendEmptyMessage(0);
    handler.post(scratchRunnable);

    handler.removeCallbacksAndMessages(null);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(wasRun[0]).named("Message").isFalse();
    assertThat(scratchRunnable.wasRun).named("Callback").isFalse();
  }

  @Test
  public void shouldRemoveSingleMessage() throws Exception {
    final List<Object> objects = new ArrayList<>();
    ShadowLooper.pauseMainLooper();

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
    ShadowLooper.unPauseMainLooper();

    assertThat(objects).containsExactly(firstObj);
  }

  @Test
  public void shouldRemoveTaggedCallback() throws Exception {
    ShadowLooper.pauseMainLooper();
    Handler handler = new Handler();
    
    final int[] count = new int[1];
    Runnable r = new Runnable() {
      @Override
      public void run() {
        count[0]++;  
      }
    };
    
    String tag1 = "tag1", tag2 = "tag2";
    
    handler.postAtTime(r, tag1, 100);
    handler.postAtTime(r, tag2, 105);

    handler.removeCallbacks(r, tag2);
    ShadowLooper.unPauseMainLooper();

    assertThat(count[0]).named("run count").isEqualTo(1);
    // This assertion proves that it was the first runnable that ran,
    // which proves that the correctly tagged runnable was removed.
    assertThat(shadowOf(handler.getLooper()).getScheduler().getCurrentTime()).named("currentTime").isEqualTo(100);
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
    final List<Long>  whens = new ArrayList<>();
    Handler h = new Handler(new Handler.Callback() {
      @Override
      public boolean handleMessage(Message msg) {
        whens.add(msg.getWhen());
        return false;
      }
    });

    final long startTime = Robolectric.getForegroundThreadScheduler().getCurrentTime();
    h.sendEmptyMessage(0);
    h.sendEmptyMessageDelayed(0, 4000L);
    Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
    h.sendEmptyMessageDelayed(0, 12000L);
    Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();

    assertThat(whens).named("whens").containsExactly(startTime, startTime + 4000, startTime + 16000);
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
