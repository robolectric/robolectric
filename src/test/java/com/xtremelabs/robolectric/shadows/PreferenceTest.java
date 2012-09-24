package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceTest {

	private TestPreference preference;
	private ShadowPreference shadow;

	private Context context;
	private TestAttributeSet attrs;

	private boolean clicked = false;

	@Before
	public void setup() {
		context = new Activity();
		attrs = new TestAttributeSet( new HashMap<String, String>() );
		preference = new TestPreference(context, attrs);
		shadow = Robolectric.shadowOf( preference );
	}

	@Test
	public void shouldConstruct() {
		int defStyle = 7;

		preference = new TestPreference(context, attrs, defStyle);
		shadow = Robolectric.shadowOf(preference);
		assertThat(shadow.getContext(), sameInstance(context));
		assertThat(shadow.getAttrs(), sameInstance((AttributeSet)attrs));
		assertThat(shadow.getDefStyle(), equalTo(defStyle));

		preference = new TestPreference(context, attrs);
		shadow = Robolectric.shadowOf(preference);
		assertThat(shadow.getContext(), sameInstance( context ));
		assertThat(shadow.getAttrs(), sameInstance((AttributeSet)attrs));
		assertThat(shadow.getDefStyle(), equalTo(0));

		preference = new TestPreference(context);
		shadow = Robolectric.shadowOf(preference);
		assertThat(shadow.getContext(), sameInstance( context ));
		assertThat(shadow.getAttrs(), nullValue());
		assertThat(shadow.getDefStyle(), equalTo(0));
}

	@Test
	public void shouldInitializeFromAttributes() {
		String key = "key_value";
		HashMap<String, String> hash = new HashMap<String, String>();
		hash.put( "android:key", key );
		attrs = new TestAttributeSet( hash );

		preference = new TestPreference(context, attrs);
		assertThat( preference.getKey(), equalTo(key) );
	}

	@Test
	public void shouldHaveAKey() {
		String key = "key_value";

		assertThat(preference.getKey(), nullValue());
		preference.setKey(key);
		assertThat(preference.getKey(), equalTo(key));
	}

	@Test
	public void shouldHaveATitle() {
		CharSequence title = "Test Preference";

		assertThat(preference.getTitle(), nullValue());
		preference.setTitle(title);
		assertThat(preference.getTitle(), equalTo(title));
	}

	@Test
	public void shouldSetTitleByResourceId() {
		CharSequence expected = "Hello";

		assertThat(preference.getTitle(), not(equalTo(expected)));
		preference.setTitle(R.string.hello);
		assertThat(preference.getTitle(), equalTo(expected));
	}

	@Test
	public void shouldHaveASummary() {
		CharSequence summary = "This is only a test.";

		assertThat(preference.getSummary(), nullValue());
		preference.setSummary(summary);
		assertThat(preference.getSummary(), equalTo(summary));
	}

	@Test
	public void shouldSetSummaryByResourceId() {
		CharSequence expected = "Hello";

		assertThat(preference.getSummary(), not(equalTo(expected)));
		preference.setSummary(R.string.hello);
		assertThat(preference.getSummary(), equalTo(expected));
	}

	@Test
	public void shouldRememberDefaultValue() {
		Object defaultValue = "Zoodles was here";

		assertThat(shadow.getDefaultValue(), nullValue());
		preference.setDefaultValue(defaultValue);
		assertThat(shadow.getDefaultValue(), sameInstance(defaultValue));
	}

	@Test
	public void shouldOrder() {
		int[] values = { 0, 1, 2, 2011 };

		for(int order : values) {
			preference.setOrder(order);
			assertThat(preference.getOrder(), equalTo(order));
		}
	}

	@Test
	public void shouldEnable() {
		assertThat(preference.isEnabled(), equalTo(true));

		preference.setEnabled(false);
		assertThat(preference.isEnabled(), equalTo(false));

		preference.setEnabled(true);
		assertThat(preference.isEnabled(), equalTo(true));
	}

	@Test
	public void testPersistent() {
		boolean[] values = { true, false };

		for( boolean shouldPersist : values ) {
			shadow.setPersistent(shouldPersist);
			assertThat(preference.shouldPersist(), equalTo(shouldPersist));
			assertThat(preference.isPersistent(), equalTo(shouldPersist));
		}
	}

	@Test
	public void shouldPersistedIn() {
		int defaultValue = 727;
		int[] values = { 0, 1, 2, 2011 };

		for(int persistedInt : values) {
			shadow.persistInt(persistedInt);

			shadow.setPersistent(false);
			assertThat(preference.getPersistedInt(defaultValue), equalTo(defaultValue));

			shadow.setPersistent(true);
			assertThat(preference.getPersistedInt(defaultValue), equalTo(persistedInt));
		}
	}

	@Test
	public void shouldRememberOnClickListener() {
		Preference.OnPreferenceClickListener onPreferenceClickListener = new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				return true;
			}
		};

		preference.setOnPreferenceClickListener(onPreferenceClickListener);
		assertThat(shadow.getOnPreferenceClickListener(), sameInstance(onPreferenceClickListener));
	}

	@Test
	public void shouldClickThroughToClickListener() {
		Preference.OnPreferenceClickListener onPreferenceClickListener = new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clicked = true;
				return true;
			}
		};
		preference.setOnPreferenceClickListener(onPreferenceClickListener);

		assertThat( clicked, equalTo(false));
		assertThat( shadow.click(), equalTo(true));
		assertThat( clicked, equalTo(true));
	}

	@Test
	public void shouldRecordCallChangeListenerValue() {
		Integer[] values = { 0, 1, 2, 2011 };
		preference.setOnPreferenceChangeListener(null);

		for(Integer newValue : values) {
			assertThat("Case " + newValue, preference.callChangeListener(newValue), equalTo(true));
			assertThat("Case " + newValue, shadow.getCallChangeListenerValue(), sameInstance((Object)newValue));
		}
	}

	@Test
	public void shouldInvokeCallChangeListenerIfSet() {
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                return (Integer) o != 666;
            }
        });
        assertThat(preference.callChangeListener(666), is(false));
        assertThat(preference.callChangeListener(777), is(true));
	}

	@Test
	public void shouldRememberOnChangeListener() {
		Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object o) {
                throw new RuntimeException("Unimplemented");
			}
		};

		preference.setOnPreferenceChangeListener(onPreferenceChangeListener);
		assertThat(shadow.getOnPreferenceChangeListener(), sameInstance(onPreferenceChangeListener));
	}


	@Test
	public void shouldReturnIntent() {
		assertThat( preference.getIntent(), nullValue() );
		preference.setIntent( new Intent() );
		assertThat( preference.getIntent(), notNullValue() );
	}

	@Test
	public void shouldRememberDependency() {
		assertThat( preference.getDependency(), nullValue() );
		preference.setDependency("TEST_PREF_KEY");
		assertThat(preference.getDependency(), notNullValue());
		assertThat( preference.getDependency(), equalTo("TEST_PREF_KEY") );
	}

	private static class TestPreference extends Preference {
		public TestPreference(Context context) {
			super(context);
		}

		public TestPreference(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public TestPreference(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public boolean shouldPersist() {
			return super.shouldPersist();
		}

		public int getPersistedInt(int defaultReturnValue) {
			return super.getPersistedInt(defaultReturnValue);
		}

		public boolean persistInt(int value) {
			return super.persistInt(value);
		}

		public boolean callChangeListener(Object newValue) {
			return super.callChangeListener(newValue);
		}
	}
}
