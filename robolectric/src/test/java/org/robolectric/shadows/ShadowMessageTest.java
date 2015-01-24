package org.robolectric.shadows;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowMessageTest {

  @Test
  public void testGetDataShouldLazilyCreateBundle() throws Exception {
    assertNotNull(new Message().getData());
    assertTrue(new Message().getData().isEmpty());
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
    assertNull(new Message().peekData());

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
    assertThat(m2.getData()).isEqualTo(m.getData());
    assertNull(m2.getTarget());
  }

  @Test
  public void testObtain() throws Exception {
    Message m = Message.obtain();
    assertNotNull(m);
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
    assertThat(m2.getData()).isEqualTo(m.getData());
    assertThat(m2.getTarget()).isEqualTo(m.getTarget());
  }

  @Test
  public void testSendToTarget() throws Exception {
    ShadowLooper.pauseMainLooper();
    Handler h = new Handler();
    Message.obtain(h, 123).sendToTarget();
    assertTrue(h.hasMessages(123));
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
  @Config(emulateSdk=19)
  public void recycle_shouldInvokeRealObject19() {
    recycle_shouldInvokeRealObject("recycle");
  }

  @Test
  @Config(emulateSdk=21)
  public void recycle_shouldInvokeRealObject21() {
    recycle_shouldInvokeRealObject("recycleUnchecked");
  }
  
  private void recycle_shouldInvokeRealObject(String recycleMethod) {
    Handler h = new Handler();
    Message msg = Message.obtain(h, 234);
    ReflectionHelpers.callInstanceMethod(msg, recycleMethod);
    assertThat(msg.what).isZero();
  }
  
  @Test
  @Config(emulateSdk=19)
  public void recycle_shouldRemoveMessageFromScheduler19() {
    recycle_shouldRemoveMessageFromScheduler();
  }
  
  @Test
  @Config(emulateSdk=21)
  public void recycle_shouldRemoveMessageFromScheduler21() {
    recycle_shouldRemoveMessageFromScheduler();
  }
  
  private void recycle_shouldRemoveMessageFromScheduler() {
    ShadowLooper.pauseMainLooper();
    Handler h = new Handler();
    Message msg = Message.obtain(h, 234);
    msg.sendToTarget();
    Scheduler scheduler = ShadowLooper.getUiThreadScheduler();
    assertThat(scheduler.size()).as("before recycle").isEqualTo(1);
    shadowOf(msg).recycleUnchecked();
    assertThat(scheduler.size()).as("after recycle").isEqualTo(0);
  }
  
  @Test
  public void reset_shouldEmptyMessagePool() {
    Message dummy1 = Message.obtain();
    shadowOf(dummy1).recycleUnchecked();
    Message dummy2 = Message.obtain();
    assertThat(dummy2).as("before resetting").isSameAs(dummy1);

    shadowOf(dummy2).recycleUnchecked();
    Robolectric.reset();
    dummy1 = Message.obtain();
    assertThat(dummy1).as("after resetting").isNotSameAs(dummy2);
  }
}
