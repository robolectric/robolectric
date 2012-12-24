package com.xtremelabs.robolectric.shadows;

import android.util.Log;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.ShadowLog.LogItem;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

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
    public void shouldLogAccordingToTag() throws Exception {
    	Log.d( "tag1", "1" );
    	Log.i( "tag2", "2" );
    	Log.e( "tag3", "3" );
    	Log.w( "tag1", "4" );
    	Log.i( "tag1", "5" );
    	Log.d( "tag2", "6" );
    	
    	List<LogItem> allItems = ShadowLog.getLogs();
    	assertThat( allItems.size(), equalTo(6) );
    	int i = 1;
    	for ( LogItem item : allItems ) {
    		assertThat( item.msg, equalTo(Integer.toString(i)) );
    		i++;
    	}
    	assertUniformLogsForTag( "tag1", 3 );
    	assertUniformLogsForTag( "tag2", 2 );
    	assertUniformLogsForTag( "tag3", 1 );   	
    }
    
    private void assertUniformLogsForTag( String tag, int count ) {
    	List<LogItem> tag1Items = ShadowLog.getLogsForTag( tag );
    	assertThat( tag1Items.size(), equalTo( count ) );
    	int last = -1;
    	for (LogItem item : tag1Items) {
    		assertThat(item.tag, equalTo(tag));
    		int current = Integer.parseInt(item.msg);
    		assertThat(current > last, equalTo(true));
    		last = current;
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
