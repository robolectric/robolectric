package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class DialogPreferenceTest {

    private static final String TEST_DIALOG_MESSAGE = "This is only a test";

    private DialogPreference preference;
    private ShadowDialogPreference shadow;

    private Context context;
    private TestAttributeSet attrs;

    @Before
    public void setup() {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("dialogMessage", TEST_DIALOG_MESSAGE);
        context = new Activity();
        attrs = new TestAttributeSet(hash);
        preference = new TestDialogPreference(context, attrs);
        shadow = Robolectric.shadowOf(preference);
    }

    @Test
    public void testConstructors() {
        int defStyle = 7;

        preference = new TestDialogPreference(context, attrs, defStyle);
        shadow = Robolectric.shadowOf(preference);
        assertThat(shadow.getContext(), sameInstance(context));
        assertThat(shadow.getAttrs(), sameInstance((AttributeSet) attrs));
        assertThat(shadow.getDefStyle(), equalTo(defStyle));

        preference = new TestDialogPreference(context, attrs);
        shadow = Robolectric.shadowOf(preference);
        assertThat(shadow.getContext(), sameInstance(context));
        assertThat(shadow.getAttrs(), sameInstance((AttributeSet) attrs));
        assertThat(shadow.getDefStyle(), equalTo(0));
    }

    @Test
    public void testGetDialogMessage() {
        assertThat((String) preference.getDialogMessage(), equalTo(TEST_DIALOG_MESSAGE));
    }

    protected static class TestDialogPreference extends DialogPreference {

        public TestDialogPreference(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public TestDialogPreference(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }
}
