package com.xtremelabs.robolectric;

import android.content.Context;
import android.view.View;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.util.TestOnClickListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
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
}
