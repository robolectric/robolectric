package com.xtremelabs.robolectric.bytecode;

import android.app.Application;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowWranglerWithDefaultsTest {
    private String name;

    @Before
    public void setUp() throws Exception {
        name = "context";
    }

    /**
     * This verifies that if an real method call happens using an "int" resourceId, but the shadow class
     * doesn't have that method and only has one accepting a CharSequence, that Robolectric will get the resource Text
     * and call the CharSequence shadow method with it.  This eliminates the need to add a huge amount of duplication
     * in the shadow classes to accomodate the android API changes.
     */
    @Test
    public void testMethodDelegationFromResourceIdToCharSequence() throws Exception {
        int helloId = R.string.hello;
        Robolectric.bindShadowClass(WithCharSequence.class);

        Foo foo = new Foo(name);
        foo.displayText(helloId, false);
        WithCharSequence shadow = Robolectric.shadowOf_(foo);
        assertEquals("Hello", shadow.displayedText);
    }

    @Implements(Foo.class)
    public static class WithCharSequence {
        CharSequence displayedText;

        public void displayText(CharSequence text, boolean bold) {
            this.displayedText = text;
        }
    }
}
