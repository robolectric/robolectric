package com.xtremelabs.robolectric.shadows;

import android.util.Log;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class LogTest {
    @Test
    public void d_shouldLogAppropriately() {
        Log.d("tag", "msg");

        assertLogged(ShadowLog.LogType.debug, "tag", "msg", null);
    }

    @Test
    public void d_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.d("tag", "msg", throwable);

        assertLogged(ShadowLog.LogType.debug, "tag", "msg", throwable);
    }

    @Test
    public void e_shouldLogAppropriately() {
        Log.e("tag", "msg");

        assertLogged(ShadowLog.LogType.error, "tag", "msg", null);
    }

    @Test
    public void e_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.e("tag", "msg", throwable);

        assertLogged(ShadowLog.LogType.error, "tag", "msg", throwable);
    }

    @Test
    public void i_shouldLogAppropriately() {
        Log.i("tag", "msg");

        assertLogged(ShadowLog.LogType.info, "tag", "msg", null);
    }

    @Test
    public void i_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.i("tag", "msg", throwable);

        assertLogged(ShadowLog.LogType.info, "tag", "msg", throwable);
    }

    @Test
    public void v_shouldLogAppropriately() {
        Log.v("tag", "msg");

        assertLogged(ShadowLog.LogType.verbose, "tag", "msg", null);
    }

    @Test
    public void v_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.v("tag", "msg", throwable);

        assertLogged(ShadowLog.LogType.verbose, "tag", "msg", throwable);
    }

    @Test
    public void w_shouldLogAppropriately() {
        Log.w("tag", "msg");

        assertLogged(ShadowLog.LogType.warning, "tag", "msg", null);
    }

    @Test
    public void w_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.w("tag", "msg", throwable);

        assertLogged(ShadowLog.LogType.warning, "tag", "msg", throwable);
    }

    @Test
    public void wtf_shouldLogAppropriately() {
        Log.wtf("tag", "msg");

        assertLogged(ShadowLog.LogType.wtf, "tag", "msg", null);
    }

    @Test
    public void wtf_shouldLogAppropriately_withThrowable() {
        Throwable throwable = new Throwable();

        Log.wtf("tag", "msg", throwable);

        assertLogged(ShadowLog.LogType.wtf, "tag", "msg", throwable);
    }

    private void assertLogged(ShadowLog.LogType type, String tag, String msg, Throwable throwable) {
        ShadowLog.LogItem lastLog = ShadowLog.getLogs().get(0);
        assertEquals(type, lastLog.type);
        assertEquals(msg, lastLog.msg);
        assertEquals(tag, lastLog.tag);
        assertEquals(throwable, lastLog.throwable);
    }
}
