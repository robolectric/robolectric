package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@RunWith(WithTestDefaultsRunner.class)
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
        assertThat(m.getData(), equalTo(b));
    }

    @Test
    public void testPeekData() throws Exception {
        assertNull(new Message().peekData());

        Message m = new Message();
        Bundle b = new Bundle();
        m.setData(b);
        assertThat(m.peekData(), equalTo(b));
    }

    @Test
    public void testGetTarget() throws Exception {
        Message m = new Message();
        Handler h = new Handler();
        m.setTarget(h);
        assertThat(m.getTarget(), equalTo(h));
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

        assertThat(m2.arg1, equalTo(m.arg1));
        assertThat(m2.arg2, equalTo(m.arg2));
        assertThat(m2.obj, equalTo(m.obj));
        assertThat(m2.what, equalTo(m.what));
        assertThat(m2.getData(), equalTo(m.getData()));
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
        assertThat(m.getTarget(), equalTo(h));
    }

    @Test
    public void testObtainWithHandlerAndWhat() throws Exception {
        Handler h = new Handler();
        int what = 10;
        Message m = Message.obtain(h, what);

        assertThat(m.getTarget(), equalTo(h));
        assertThat(m.what, equalTo(what));
        assertThat(m.getTarget(), equalTo(h));
    }

    @Test
    public void testObtainWithHandlerWhatAndObject() throws Exception {
        Handler h = new Handler();
        int what = 10;
        Object obj = "test";
        Message m = Message.obtain(h, what, obj);

        assertThat(m.getTarget(), equalTo(h));
        assertThat(m.what, equalTo(what));
        assertThat(m.getTarget(), equalTo(h));
        assertThat(m.obj, equalTo(obj));
    }

    @Test
    public void testObtainWithHandlerWhatAndTwoArgs() throws Exception {
        Handler h = new Handler();
        int what = 2;
        int arg1 = 3;
        int arg2 = 5;
        Message m = Message.obtain(h, what, arg1, arg2);

        assertThat(m.getTarget(), equalTo(h));
        assertThat(m.what, equalTo(what));
        assertThat(m.arg1, equalTo(arg1));
        assertThat(m.arg2, equalTo(arg2));
    }

    @Test
    public void testObtainWithHandlerWhatTwoArgsAndObj() throws Exception {
        Handler h = new Handler();
        int what = 2;
        int arg1 = 3;
        int arg2 = 5;
        Object obj = "test";
        Message m = Message.obtain(h, what, arg1, arg2, obj);

        assertThat(m.getTarget(), equalTo(h));
        assertThat(m.what, equalTo(what));
        assertThat(m.arg1, equalTo(arg1));
        assertThat(m.arg2, equalTo(arg2));
        assertThat(m.obj, equalTo(obj));
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

        assertThat(m2.arg1, equalTo(m.arg1));
        assertThat(m2.arg2, equalTo(m.arg2));
        assertThat(m2.obj, equalTo(m.obj));
        assertThat(m2.what, equalTo(m.what));
        assertThat(m2.getData(), equalTo(m.getData()));
        assertThat(m2.getTarget(), equalTo(m.getTarget()));
    }

    @Test
    public void testSendToTarget() throws Exception {
        ShadowLooper.pauseMainLooper();
        Handler h = new Handler();
        Message.obtain(h, 123).sendToTarget();
        assertTrue(h.hasMessages(123));
    }
}
