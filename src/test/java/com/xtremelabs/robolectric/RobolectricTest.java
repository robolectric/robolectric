package com.xtremelabs.robolectric;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowDisplay;
import com.xtremelabs.robolectric.util.TestOnClickListener;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class RobolectricTest {

    private PrintStream originalSystemOut;
    private ByteArrayOutputStream buff;
    private String defaultLineSeparator;

    @Before
    public void setUp() {
        originalSystemOut = System.out;
        defaultLineSeparator = System.getProperty("line.separator");

        System.setProperty("line.separator", "\n");
        buff = new ByteArrayOutputStream();
        PrintStream testOut = new PrintStream(buff);
        System.setOut(testOut);
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("line.separator", defaultLineSeparator);
        System.setOut(originalSystemOut);
    }

    @Test
    public void shouldLogMissingInvokedShadowMethodsWhenRequested() throws Exception {
        Robolectric.bindShadowClass(TestShadowView.class);
        Robolectric.logMissingInvokedShadowMethods();


        View aView = new View(null);
        // There's a shadow method for this
        aView.getContext();
        String output = buff.toString();
        assertEquals("No Shadow method found for View.<init>(android.content.Context)\n", output);
        buff.reset();

        aView.findViewById(27);
        // No shadow here... should be logged
        output = buff.toString();
        assertEquals("No Shadow method found for View.findViewById(int)\n", output);
    }

    @Test // This is nasty because it depends on the test above having run first in order to fail
    public void shouldNotLogMissingInvokedShadowMethodsByDefault() throws Exception {
        View aView = new View(null);
        aView.findViewById(27);
        String output = buff.toString();

        assertEquals("", output);
    }

    @Test(expected = RuntimeException.class)
    public void clickOn_shouldThrowIfViewIsDisabled() throws Exception {
        View view = new View(null);
        view.setEnabled(false);
        Robolectric.clickOn(view);
    }

    @Test
    public void shouldResetBackgroundSchedulerBeforeTests() throws Exception {
        assertThat(Robolectric.getBackgroundScheduler().isPaused(), equalTo(false));
        Robolectric.getBackgroundScheduler().pause();
    }

    @Test
    public void shouldResetBackgroundSchedulerAfterTests() throws Exception {
        assertThat(Robolectric.getBackgroundScheduler().isPaused(), equalTo(false));
        Robolectric.getBackgroundScheduler().pause();
    }

    @Test
    public void httpRequestWasSent_ReturnsTrueIfRequestWasSent() throws IOException, HttpException {
        makeRequest("http://example.com");

        assertTrue(Robolectric.httpRequestWasMade());
    }

    @Test
    public void httpRequestWasMade_ReturnsFalseIfNoRequestWasMade() {
        assertFalse(Robolectric.httpRequestWasMade());
    }

    @Test
    public void httpRequestWasMade_returnsTrueIfRequestMatchingGivenRuleWasMade() throws IOException, HttpException {
        makeRequest("http://example.com");
        assertTrue(Robolectric.httpRequestWasMade("http://example.com"));
    }

    @Test
    public void httpRequestWasMade_returnsFalseIfNoRequestMatchingGivenRuleWasMAde() throws IOException, HttpException {
        makeRequest("http://example.com");
        assertFalse(Robolectric.httpRequestWasMade("http://example.org"));
    }

    @Test
    public void idleMainLooper_executesScheduledTasks() {
        final boolean[] wasRun = new boolean[]{false};
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wasRun[0] = true;
            }
        }, 2000);

        assertFalse(wasRun[0]);
        Robolectric.idleMainLooper(1999);
        assertFalse(wasRun[0]);
        Robolectric.idleMainLooper(1);
        assertTrue(wasRun[0]);
    }

    @Test
    public void shouldUseSetDensityForContexts() throws Exception {
        assertThat(new Activity().getResources().getDisplayMetrics().density, equalTo(1.0f));
        Robolectric.setDisplayMetricsDensity(1.5f);
        assertThat(new Activity().getResources().getDisplayMetrics().density, equalTo(1.5f));
    }

    @Test
    public void shouldUseSetDisplayForContexts() throws Exception {
        assertThat(new Activity().getResources().getDisplayMetrics().widthPixels, equalTo(480));
        assertThat(new Activity().getResources().getDisplayMetrics().heightPixels, equalTo(800));

        Display display = Robolectric.newInstanceOf(Display.class);
        ShadowDisplay shadowDisplay = shadowOf(display);
        shadowDisplay.setWidth(100);
        shadowDisplay.setHeight(200);
        Robolectric.setDefaultDisplay(display);

        assertThat(new Activity().getResources().getDisplayMetrics().widthPixels, equalTo(100));
        assertThat(new Activity().getResources().getDisplayMetrics().heightPixels, equalTo(200));
    }

    @Test
    public void clickOn_shouldCallClickListener() throws Exception {
        View view = new View(null);
        TestOnClickListener testOnClickListener = new TestOnClickListener();
        view.setOnClickListener(testOnClickListener);
        Robolectric.clickOn(view);
        assertTrue(testOnClickListener.clicked);
    }

    @Implements(View.class)
    public static class TestShadowView {
        @SuppressWarnings({"UnusedDeclaration"})
        @Implementation
        public Context getContext() {
            return null;
        }
    }

    private void makeRequest(String uri) throws HttpException, IOException {
        Robolectric.addPendingHttpResponse(200, "a happy response body");

        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
                return 0;
            }

        };
        DefaultRequestDirector requestDirector = new DefaultRequestDirector(null, null, null, connectionKeepAliveStrategy, null, null, null, null, null, null, null, null);

        requestDirector.execute(null, new HttpGet(uri), null);
    }
}
