package com.xtremelabs.robolectric;

import android.content.Context;
import android.view.View;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.TestOnClickListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class RobolectricTest {

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
    public void shouldLogMissingInvokedShadowMethodsWhenRequested() throws Exception {
        Robolectric.bindShadowClass(View.class, TestShadowView.class);
        Robolectric.logMissingInvokedShadowMethods();


        View aView = new View(null);
        // There's a shadow method for this
        aView.getContext();
        String output = buff.toString();
        assertEquals("", output);

        aView.findViewById(27);
        // No shadow here... should be logged
        output = buff.toString();
        assertEquals("No Shadow method found for View.findViewById(int)" + System.getProperty("line.separator"), output);
    }

    @Test // This is nasty because it depends on the test above having run first in order to fail
    public void shouldNotLogMissingInvokedShadowMethodsByDefault() throws Exception {
        Robolectric.bindShadowClass(View.class, ShadowWranglerTest.TestShadowView.class);

        View aView = new View(null);
        aView.findViewById(27);
        String output = buff.toString();

        assertEquals("", output);
    }

    @Test(expected= RuntimeException.class)
    public void clickOn_shouldThrowIfViewIsDisabled() throws Exception {
        View view = new View(null);
        view.setEnabled(false);
        Robolectric.clickOn(view);
    }

    public void clickOn_shouldCallClickListener() throws Exception {
        View view = new View(null);
        TestOnClickListener testOnClickListener = new TestOnClickListener();
        view.setOnClickListener(testOnClickListener);
        Robolectric.clickOn(view);
        assertTrue(testOnClickListener.clicked);
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
