package com.xtremelabs.robolectric.shadows;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.preference.EditTextPreference;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;

@RunWith(WithTestDefaultsRunner.class)
public class EditTextPreferenceTest {

	private static final String SOME_TEXT = "some text";
	private EditTextPreference preference;

	private Context context;
	private TestAttributeSet attrs;

	@Before
	public void setup() {
		context = new Activity();
		preference = new EditTextPreference(context, attrs);
	}

	@Test
	public void testConstructor() {
		preference = new EditTextPreference(context, attrs, 7);
		assertNotNull(preference.getEditText());
	}

	@Test
	public void testSetText() {
		preference.setText(SOME_TEXT);
		assertThat((String) preference.getEditText().getText().toString(), equalTo(SOME_TEXT));
	}

}
