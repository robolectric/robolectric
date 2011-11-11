package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Bundle;
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
    public void testCopyFrom() throws Exception {
        Bundle b = new Bundle();
        Message m = new Message();
        m.arg1 = 10;
        m.arg2 = 42;
        m.obj = "obj";
        m.setData(b);
        Message m2 = new Message();
        m2.copyFrom(m);

        assertThat(m2.arg1, equalTo(m.arg1));
        assertThat(m2.arg2, equalTo(m.arg2));
        assertThat(m2.obj, equalTo(m.obj));
        assertThat(m2.getData(), equalTo(m.getData()));
    }
}
