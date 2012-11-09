package com.xtremelabs.robolectric.shadows;

import android.util.Log;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class LogTest {
    @Test
    public void d_shouldLogAppropriately() {
        Log.d("tag", "msg");

        assertLogged(Log.DEBUG, "tag", "msg", null);
    }

    @Test
    public void d_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.d("tag", "msg", throwable);

        assertLogged(Log.DEBUG, "tag", "msg", throwable);
    }

    @Test
    public void e_shouldLogAppropriately() {
        Log.e("tag", "msg");

        assertLogged(Log.ERROR, "tag", "msg", null);
    }

    @Test
    public void e_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.e("tag", "msg", throwable);

        assertLogged(Log.ERROR, "tag", "msg", throwable);
    }

    @Test
    public void i_shouldLogAppropriately() {
        Log.i("tag", "msg");

        assertLogged(Log.INFO, "tag", "msg", null);
    }

    @Test
    public void i_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.i("tag", "msg", throwable);

        assertLogged(Log.INFO, "tag", "msg", throwable);
    }

    @Test
    public void v_shouldLogAppropriately() {
        Log.v("tag", "msg");

        assertLogged(Log.VERBOSE, "tag", "msg", null);
    }

    @Test
    public void v_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.v("tag", "msg", throwable);

        assertLogged(Log.VERBOSE, "tag", "msg", throwable);
    }

    @Test
    public void w_shouldLogAppropriately() {
        Log.w("tag", "msg");

        assertLogged(Log.WARN, "tag", "msg", null);
    }

    @Test
    public void w_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.w("tag", "msg", throwable);

        assertLogged(Log.WARN, "tag", "msg", throwable);
    }

    @Test
    public void w_shouldLogAppropriately_withJustThrowable() {
        Throwable throwable = new Throwable();
        Log.w("tag", throwable);
        assertLogged(Log.WARN, "tag", null, throwable);
    }

    @Test
    public void wtf_shouldLogAppropriately() {
        Log.wtf("tag", "msg");

        assertLogged(Log.ASSERT, "tag", "msg", null);
    }

    @Test
    public void wtf_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.wtf("tag", "msg", throwable);

        assertLogged(Log.ASSERT, "tag", "msg", throwable);
    }

    @Test
    public void shouldLogToProvidedStream() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream old = ShadowLog.stream;
        try {
            ShadowLog.stream = new PrintStream(bos);
            Log.d("tag", "msg");
            assertThat(new String(bos.toByteArray()), equalTo("D/tag: msg" + System.getProperty("line.separator")));


            Log.w("tag", new RuntimeException());
            assertTrue(new String(bos.toByteArray()).contains("RuntimeException"));
        } finally {
            ShadowLog.stream = old;
        }
    }

    @Test
    public void infoIsDefaultLoggableLevel() throws Exception {
        PrintStream old = ShadowLog.stream;
        ShadowLog.stream = null;
        assertFalse(Log.isLoggable("FOO", Log.VERBOSE));
        assertFalse(Log.isLoggable("FOO", Log.DEBUG));

        assertTrue(Log.isLoggable("FOO", Log.INFO));
        assertTrue(Log.isLoggable("FOO", Log.WARN));
        assertTrue(Log.isLoggable("FOO", Log.ERROR));
        assertTrue(Log.isLoggable("FOO", Log.ASSERT));
        ShadowLog.stream = old;
    }

    @Test
    public void shouldAlwaysBeLoggableIfStreamIsSpecified() throws Exception {
        PrintStream old = ShadowLog.stream;
        ShadowLog.stream = new PrintStream(new ByteArrayOutputStream());
        assertTrue(Log.isLoggable("FOO", Log.VERBOSE));
        assertTrue(Log.isLoggable("FOO", Log.DEBUG));
        assertTrue(Log.isLoggable("FOO", Log.INFO));
        assertTrue(Log.isLoggable("FOO", Log.WARN));
        assertTrue(Log.isLoggable("FOO", Log.ERROR));
        assertTrue(Log.isLoggable("FOO", Log.ASSERT));
        ShadowLog.stream = old;
    }

    private void assertLogged(int type, String tag, String msg, Throwable throwable) {
        ShadowLog.LogItem lastLog = ShadowLog.getLogs().get(0);
        assertEquals(type, lastLog.type);
        assertEquals(msg, lastLog.msg);
        assertEquals(tag, lastLog.tag);
        assertEquals(throwable, lastLog.throwable);
    }
}
