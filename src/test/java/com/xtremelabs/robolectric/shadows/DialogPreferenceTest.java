package com.xtremelabs.robolectric.shadows;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import android.app.Activity;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.internal.TestAttributeSet;

@RunWith(WithTestDefaultsRunner.class)
public class DialogPreferenceTest {
	
	private static final String TEST_DIALOG_MESSAGE = "This is only a test";

	private DialogPreference preference;

	@Before
	public void setup() {
		HashMap<String, String> hash = new HashMap<String, String>();
		hash.put("dialogMessage", TEST_DIALOG_MESSAGE);

		preference = new TestDialogPreference(new Activity(), new TestAttributeSet(hash));
	}
		
	@Test
	public void testGetDialogMessage() {
		assertThat( (String) preference.getDialogMessage(), equalTo(TEST_DIALOG_MESSAGE) );
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
