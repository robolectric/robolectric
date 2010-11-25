package com.xtremelabs.robolectric;

import android.app.Application;
import android.content.Context;
import android.view.View;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class RobolectricTestRunnerTest {

    private PrintStream originalSystemOut;
    private ByteArrayOutputStream buff;

    @Before
    public void setUp() {
        originalSystemOut = System.out;
        buff = new ByteArrayOutputStream();
        PrintStream testOut = new PrintStream(buff);
        System.setOut(testOut);
    }

    @After
    public void tearDown() {
        System.setOut(originalSystemOut);
    }

    @Test
    public void shouldInitializeApplication() throws Exception {
        assertNotNull(Robolectric.application);
        assertEquals(Application.class, Robolectric.application.getClass());
    }

    @Test
    public void shouldLogMissingInvokedShadowMethodsWhenRequested() throws Exception {
        Robolectric.bindShadowClass(View.class, TestShadowView.class);
        RobolectricTestRunner.logMissingInvokedShadowMethods();


        View aView = new View(null);
        // There's a shadow method for this
        aView.getContext();
        String output = buff.toString();
        assertEquals("", output);

        aView.findViewById(27);
        // No shadow here... should be logged
        output = buff.toString();
        assertEquals("No Shadow method found for View.findViewById(int)\n", output);
    }

    @Test // This is nasty because it depends on the test above having run first in order to fail
    public void shouldNotLogMissingInvokedShadowMethodsByDefault() throws Exception {
        Robolectric.bindShadowClass(View.class, ShadowWranglerTest.TestShadowView.class);

        View aView = new View(null);
        aView.findViewById(27);
        String output = buff.toString();

        assertEquals("", output);
    }

    @Implements(View.class)
    public static class TestShadowView extends ShadowWranglerTest.TestShadowViewParent {
        @SuppressWarnings({"UnusedDeclaration"})
        @Implementation
        public Context getContext() {
            return null;
        }
    }

}
