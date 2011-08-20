package com.xtremelabs.robolectric;

import android.util.Log;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowLoggerTest {
    @Test
    public void d_shouldLogAppropriately() {
        Log.d("tag", "msg");

        assertLogged(ShadowLogger.LogType.debug, "tag", "msg", null);
    }

    @Test
    public void d_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.d("tag", "msg", throwable);

        assertLogged(ShadowLogger.LogType.debug, "tag", "msg", throwable);
    }

    @Test
    public void e_shouldLogAppropriately() {
        Log.e("tag", "msg");

        assertLogged(ShadowLogger.LogType.error, "tag", "msg", null);
    }

    @Test
    public void e_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.e("tag", "msg", throwable);

        assertLogged(ShadowLogger.LogType.error, "tag", "msg", throwable);
    }

    @Test
    public void i_shouldLogAppropriately() {
        Log.i("tag", "msg");

        assertLogged(ShadowLogger.LogType.info, "tag", "msg", null);
    }

    @Test
    public void i_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.i("tag", "msg", throwable);

        assertLogged(ShadowLogger.LogType.info, "tag", "msg", throwable);
    }

    @Test
    public void v_shouldLogAppropriately() {
        Log.v("tag", "msg");

        assertLogged(ShadowLogger.LogType.verbose, "tag", "msg", null);
    }

    @Test
    public void v_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.v("tag", "msg", throwable);

        assertLogged(ShadowLogger.LogType.verbose, "tag", "msg", throwable);
    }

    @Test
    public void w_shouldLogAppropriately() {
        Log.w("tag", "msg");

        assertLogged(ShadowLogger.LogType.warning, "tag", "msg", null);
    }

    @Test
    public void w_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.w("tag", "msg", throwable);

        assertLogged(ShadowLogger.LogType.warning, "tag", "msg", throwable);
    }

    @Test
    public void wtf_shouldLogAppropriately() {
        Log.wtf("tag", "msg");

        assertLogged(ShadowLogger.LogType.wtf, "tag", "msg", null);
    }

    @Test
    public void wtf_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.wtf("tag", "msg", throwable);

        assertLogged(ShadowLogger.LogType.wtf, "tag", "msg", throwable);
    }

    @After
    public void tearDown() {
        ShadowLogger.reset();
    }

    private void assertLogged(ShadowLogger.LogType type, String tag, String msg, Throwable throwable) {
        ShadowLogger.LogItem lastLog = ShadowLogger.getLogs().get(0);
        assertEquals(type, lastLog.type);
        assertEquals(msg, lastLog.msg);
        assertEquals(tag, lastLog.tag);
        assertEquals(throwable, lastLog.throwable);
    }
}
