package org.robolectric.shadows;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
public class MessageTest {

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
}
