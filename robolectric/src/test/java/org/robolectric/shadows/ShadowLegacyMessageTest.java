package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
@LooperMode(Mode.LEGACY)
public class ShadowLegacyMessageTest {

  @Test
  public void testGetDataShouldLazilyCreateBundle() {
    assertThat(new Message().getData()).isNotNull();
    assertThat(new Message().getData().isEmpty()).isTrue();
  }

  @Test
  public void testGetData() {
    Message m = new Message();
    Bundle b = new Bundle();
    m.setData(b);
    assertThat(m.getData()).isEqualTo(b);
  }

  @Test
  public void testPeekData() {
    assertThat(new Message().peekData()).isNull();

    Message m = new Message();
    Bundle b = new Bundle();
    m.setData(b);
    assertThat(m.peekData()).isEqualTo(b);
  }

  @Test
  public void testGetTarget() {
    Message m = new Message();
    Handler h = new Handler();
    m.setTarget(h);
    assertThat(m.getTarget()).isEqualTo(h);
  }

  @Test
  public void testCopyFrom() {
    Bundle b = new Bundle();
    Message m = new Message();
    m.arg1 = 10;
    m.arg2 = 42;
    m.obj = "obj";
    m.what = 24;
    m.setData(b);
    m.setTarget(new Handler());
    Message m2 = new Message();
    m2.copyFrom(m);

    assertThat(m2.arg1).isEqualTo(m.arg1);
    assertThat(m2.arg2).isEqualTo(m.arg2);
    assertThat(m2.obj).isEqualTo(m.obj);
    assertThat(m2.what).isEqualTo(m.what);
    assertThat(m2.getTarget()).isNull();
    assertThat(m2.getData()).isNotNull();
    assertThat(m2.getData().isEmpty()).isTrue();
  }

  @Test
  public void testObtain() {
    Message m = Message.obtain();
    assertThat(m).isNotNull();
  }

  @Test
  public void testObtainWithHandler() {
    Handler h = new Handler();
    Message m = Message.obtain(h);
    assertThat(m.getTarget()).isEqualTo(h);
  }

  @Test
  public void testObtainWithHandlerAndWhat() {
    Handler h = new Handler();
    int what = 10;
    Message m = Message.obtain(h, what);

    assertThat(m.getTarget()).isEqualTo(h);
    assertThat(m.what).isEqualTo(what);
    assertThat(m.getTarget()).isEqualTo(h);
  }

  @Test
  public void testObtainWithHandlerWhatAndObject() {
    Handler h = new Handler();
    int what = 10;
    Object obj = "test";
    Message m = Message.obtain(h, what, obj);

    assertThat(m.getTarget()).isEqualTo(h);
    assertThat(m.what).isEqualTo(what);
    assertThat(m.getTarget()).isEqualTo(h);
    assertThat(m.obj).isEqualTo(obj);
  }

  @Test
  public void testObtainWithHandlerWhatAndTwoArgs() {
    Handler h = new Handler();
    int what = 2;
    int arg1 = 3;
    int arg2 = 5;
    Message m = Message.obtain(h, what, arg1, arg2);

    assertThat(m.getTarget()).isEqualTo(h);
    assertThat(m.what).isEqualTo(what);
    assertThat(m.arg1).isEqualTo(arg1);
    assertThat(m.arg2).isEqualTo(arg2);
  }

  @Test
  public void testObtainWithHandlerWhatTwoArgsAndObj() {
    Handler h = new Handler();
    int what = 2;
    int arg1 = 3;
    int arg2 = 5;
    Object obj = "test";
    Message m = Message.obtain(h, what, arg1, arg2, obj);

    assertThat(m.getTarget()).isEqualTo(h);
    assertThat(m.what).isEqualTo(what);
    assertThat(m.arg1).isEqualTo(arg1);
    assertThat(m.arg2).isEqualTo(arg2);
    assertThat(m.obj).isEqualTo(obj);
  }

  @Test
  public void testObtainWithMessage() {
    Bundle b = new Bundle();
    Message m = new Message();
    m.arg1 = 10;
    m.arg2 = 42;
    m.obj = "obj";
    m.what = 24;
    m.setData(b);
    m.setTarget(new Handler());
    Message m2 = Message.obtain(m);

    assertThat(m2.arg1).isEqualTo(m.arg1);
    assertThat(m2.arg2).isEqualTo(m.arg2);
    assertThat(m2.obj).isEqualTo(m.obj);
    assertThat(m2.what).isEqualTo(m.what);
    assertThat(m2.getTarget()).isEqualTo(m.getTarget());
    assertThat(m2.getData()).isNotNull();
    assertThat(m2.getData().isEmpty()).isTrue();
  }

  @Test
  public void testSendToTarget() {
    ShadowLooper.pauseMainLooper();
    Handler h = new Handler();
    Message.obtain(h, 123).sendToTarget();
    assertThat(h.hasMessages(123)).isTrue();
  }
  
  @Test
  public void testSetGetNext() {
    Message msg = Message.obtain();
    Message msg2 = Message.obtain();
    ShadowLegacyMessage sMsg = Shadow.extract(msg);
    sMsg.setNext(msg2);
    assertThat(sMsg.getNext()).isSameInstanceAs(msg2);
  }

  @Test
  @Config(maxSdk = KITKAT_WATCH)
  public void recycle_shouldInvokeRealObject19() {
    recycle_shouldInvokeRealObject("recycle");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void recycle_shouldInvokeRealObject21() {
    recycle_shouldInvokeRealObject("recycleUnchecked");
  }
  
  private void recycle_shouldInvokeRealObject(String recycleMethod) {
    Handler h = new Handler();
    Message msg = Message.obtain(h, 234);
    ReflectionHelpers.callInstanceMethod(msg, recycleMethod);
    assertThat(msg.what).isEqualTo(0);
  }
  
  @Test
  @Config(maxSdk = KITKAT_WATCH)
  public void recycle_shouldRemoveMessageFromScheduler19() {
    recycle_shouldRemoveMessageFromScheduler();
  }
  
  @Test
  @Config(minSdk = LOLLIPOP)
  public void recycle_shouldRemoveMessageFromScheduler21() {
    recycle_shouldRemoveMessageFromScheduler();
  }
  
  private void recycle_shouldRemoveMessageFromScheduler() {
    ShadowLooper.pauseMainLooper();
    Handler h = new Handler();
    Message msg = Message.obtain(h, 234);
    msg.sendToTarget();
    Scheduler scheduler = Robolectric.getForegroundThreadScheduler();
    assertWithMessage("before recycle").that(scheduler.size()).isEqualTo(1);
    shadowOf(msg).recycleUnchecked();
    assertWithMessage("after recycle").that(scheduler.size()).isEqualTo(0);
  }
  
  @Test
  public void reset_shouldEmptyMessagePool() {
    Message dummy1 = Message.obtain();
    shadowOf(dummy1).recycleUnchecked();
    Message dummy2 = Message.obtain();
    assertWithMessage("before resetting").that(dummy2).isSameInstanceAs(dummy1);

    shadowOf(dummy2).recycleUnchecked();
    ShadowLegacyMessage.reset();
    dummy1 = Message.obtain();
    assertWithMessage("after resetting").that(dummy1).isNotSameInstanceAs(dummy2);
  }
}
