package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
public class ShadowMessageTest {

  @Test
  public void testGetDataShouldLazilyCreateBundle() throws Exception {
    assertThat(new Message().getData()).isNotNull();
    assertThat(new Message().getData().isEmpty()).isTrue();
  }

  @Test
  public void testGetData() throws Exception {
    Message m = new Message();
    Bundle b = new Bundle();
    m.setData(b);
    assertThat(m.getData()).isEqualTo(b);
  }

  @Test
  public void testPeekData() throws Exception {
    assertThat(new Message().peekData()).isNull();

    Message m = new Message();
    Bundle b = new Bundle();
    m.setData(b);
    assertThat(m.peekData()).isEqualTo(b);
  }

  @Test
  public void testGetTarget() throws Exception {
    Message m = new Message();
    Handler h = new Handler();
    m.setTarget(h);
    assertThat(m.getTarget()).isEqualTo(h);
  }

  @Test
  public void testCopyFrom() throws Exception {
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
  public void testObtain() throws Exception {
    Message m = Message.obtain();
    assertThat(m).isNotNull();
  }

  @Test
  public void testObtainWithHandler() throws Exception {
    Handler h = new Handler();
    Message m = Message.obtain(h);
    assertThat(m.getTarget()).isEqualTo(h);
  }

  @Test
  public void testObtainWithHandlerAndWhat() throws Exception {
    Handler h = new Handler();
    int what = 10;
    Message m = Message.obtain(h, what);

    assertThat(m.getTarget()).isEqualTo(h);
    assertThat(m.what).isEqualTo(what);
    assertThat(m.getTarget()).isEqualTo(h);
  }

  @Test
  public void testObtainWithHandlerWhatAndObject() throws Exception {
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
  public void testObtainWithHandlerWhatAndTwoArgs() throws Exception {
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
  public void testObtainWithHandlerWhatTwoArgsAndObj() throws Exception {
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
  public void testObtainWithMessage() throws Exception {
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
  public void testSendToTarget() throws Exception {
    ShadowLooper.pauseMainLooper();
    Handler h = new Handler();
    Message.obtain(h, 123).sendToTarget();
    assertThat(h.hasMessages(123)).isTrue();
  }
  
  @Test
  public void testSetGetNext() {
    Message msg = Message.obtain();
    Message msg2 = Message.obtain();
    ShadowMessage sMsg = shadowOf(msg);
    sMsg.setNext(msg2);
    assertThat(sMsg.getNext()).isSameAs(msg2);
  }
  
  @Test
  @Config(minSdk = LOLLIPOP)
  public void testIsInUse() {
    ShadowLooper.pauseMainLooper();
    Handler h = new Handler();
    Message msg = Message.obtain(h, 123);
    ShadowMessage sMsg = shadowOf(msg);
    assertThat(sMsg.isInUse()).isFalse();
    msg.sendToTarget();
    assertThat(sMsg.isInUse()).isTrue();
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
    assertThat(scheduler.size()).named("before recycle").isEqualTo(1);
    shadowOf(msg).recycleUnchecked();
    assertThat(scheduler.size()).named("after recycle").isEqualTo(0);
  }
  
  @Test
  public void reset_shouldEmptyMessagePool() {
    Message dummy1 = Message.obtain();
    shadowOf(dummy1).recycleUnchecked();
    Message dummy2 = Message.obtain();
    assertThat(dummy2).named("before resetting").isSameAs(dummy1);

    shadowOf(dummy2).recycleUnchecked();
    ShadowMessage.reset();
    dummy1 = Message.obtain();
    assertThat(dummy1).named("after resetting").isNotSameAs(dummy2);
  }
}
