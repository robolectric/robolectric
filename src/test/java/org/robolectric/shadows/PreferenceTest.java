package org.robolectric.shadows;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.util.TestUtil;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceTest {

  private TestPreference preference;
  private ShadowPreference shadow;

  private Context context;
  private RoboAttributeSet attrs;

  private boolean clicked = false;

  @Before
  public void setup() {
    context = Robolectric.application;
    attrs = new RoboAttributeSet(new ArrayList<Attribute>(), TestUtil.emptyResources(), null);
    preference = new TestPreference(context, attrs);
    shadow = Robolectric.shadowOf(preference);
  }

  @Test
  public void shouldConstruct() {
    // Must be an attr that points to a style or else it won't be able to
    // create the correctly styled view.
    int defStyle = R.attr.animalStyle;

    preference = new TestPreference(context, attrs, defStyle);
    shadow = Robolectric.shadowOf(preference);
    assertThat(shadow.getContext()).isSameAs(context);
    assertThat(shadow.getAttrs()).isSameAs(attrs);
    assertThat(shadow.getDefStyle()).isEqualTo(defStyle);

    preference = new TestPreference(context, attrs);
    shadow = Robolectric.shadowOf(preference);
    assertThat(shadow.getContext()).isSameAs(context);
    assertThat(shadow.getAttrs()).isSameAs(attrs);
    assertThat(shadow.getDefStyle()).isEqualTo(0);

    preference = new TestPreference(context);
    shadow = Robolectric.shadowOf(preference);
    assertThat(shadow.getContext()).isSameAs(context);
    assertThat(shadow.getAttrs()).isNull();
    assertThat(shadow.getDefStyle()).isEqualTo(0);
  }

  @Test
  public void shouldInitializeFromAttributes() {
    String key = "key_value";
    String title = "title_value";
    String summary = "summary_value";

    List<Attribute> attributes = new ArrayList<Attribute>();
    attributes.add(new Attribute("android:attr/key", key, R.class.getPackage().getName()));
    attributes.add(new Attribute("android:attr/title", title, R.class.getPackage().getName()));
    attributes.add(new Attribute("android:attr/summary", summary, R.class.getPackage().getName()));
    attrs = new RoboAttributeSet(attributes, TestUtil.emptyResources(), null);

    preference = new TestPreference(context, attrs);
    assertThat(preference.getKey()).isEqualTo(key);
    assertThat(preference.getTitle()).isEqualTo(title);
    assertThat(preference.getSummary()).isEqualTo(summary);
  }

  @Test
  public void shouldInitializeDefaultValueFromAttribute() throws Exception {
    String defaultValue = "default_value";
    List<Attribute> attributes = new ArrayList<Attribute>();
    attributes.add(new Attribute("android:attr/defaultValue", defaultValue, R.class.getPackage().getName()));
    attrs = new RoboAttributeSet(attributes, TestUtil.emptyResources(), null);
    preference = new TestPreference(context, attrs);
    assertThat(Robolectric.shadowOf(preference).getDefaultValue()).isEqualTo(defaultValue);
  }

  @Test
  public void shouldInitializeAndResolveFromResourceAttributes() {
    List<Attribute> attributes = new ArrayList<Attribute>();
    attributes.add(new Attribute("android:attr/key", "@string/preference_resource_key", R.class.getPackage().getName()));
    attributes.add(new Attribute("android:attr/title", "@string/preference_resource_title", R.class.getPackage().getName()));
    attributes.add(new Attribute("android:attr/summary", "@string/preference_resource_summary", R.class.getPackage().getName()));
    attrs = new RoboAttributeSet(attributes, TestUtil.emptyResources(), null);

    preference = new TestPreference(context, attrs);
    assertThat(preference.getKey()).isEqualTo("preference_resource_key_value");
    assertThat(preference.getTitle()).isEqualTo("preference_resource_title_value");
    assertThat(preference.getSummary()).isEqualTo("preference_resource_summary_value");
  }

  @Test
  public void shouldInitializeAndResolveDefaultValueFromResourceAttribute() throws Exception {
    List<Attribute> attributes = new ArrayList<Attribute>();
    attributes.add(new Attribute("android:attr/defaultValue",
        "@string/preference_resource_default_value", R.class.getPackage().getName()));
    attrs = new RoboAttributeSet(attributes, TestUtil.emptyResources(), null);
    preference = new TestPreference(context, attrs);
    assertThat(Robolectric.shadowOf(preference).getDefaultValue())
        .isEqualTo("preference_resource_default_value");
  }

  @Test
  public void shouldHaveAKey() {
    String key = "key_value";

    assertThat(preference.getKey()).isNull();
    preference.setKey(key);
    assertThat(preference.getKey()).isEqualTo(key);
  }

  @Test
  public void shouldHaveATitle() {
    CharSequence title = "Test Preference";

    assertThat(preference.getTitle()).isNull();
    preference.setTitle(title);
    assertThat(preference.getTitle()).isEqualTo(title);
  }

  @Test
  public void shouldSetTitleByResourceId() {
    CharSequence expected = "Hello";

    assertThat(preference.getTitle()).isNotEqualTo(expected);
    preference.setTitle(R.string.hello);
    assertThat(preference.getTitle()).isEqualTo(expected);
  }

  @Test
  public void shouldHaveASummary() {
    CharSequence summary = "This is only a test.";

    assertThat(preference.getSummary()).isNull();
    preference.setSummary(summary);
    assertThat(preference.getSummary()).isEqualTo(summary);
  }

  @Test
  public void shouldSetSummaryByResourceId() {
    CharSequence expected = "Hello";

    assertThat(preference.getSummary()).isNotEqualTo(expected);
    preference.setSummary(R.string.hello);
    assertThat(preference.getSummary()).isEqualTo(expected);
  }

  @Test
  public void shouldRememberDefaultValue() {
    Object defaultValue = "Zoodles was here";

    assertThat(shadow.getDefaultValue()).isNull();
    preference.setDefaultValue(defaultValue);
    assertThat(shadow.getDefaultValue()).isSameAs(defaultValue);
  }

  @Test
  public void shouldOrder() {
    int[] values = {0, 1, 2, 2011};

    for (int order : values) {
      preference.setOrder(order);
      assertThat(preference.getOrder()).isEqualTo(order);
    }
  }

  @Test
  public void shouldEnable() {
    assertThat(preference.isEnabled()).isTrue();

    preference.setEnabled(false);
    assertThat(preference.isEnabled()).isFalse();

    preference.setEnabled(true);
    assertThat(preference.isEnabled()).isTrue();
  }

  @Test
  public void testPersistent() {
    boolean[] values = {true, false};

    for (boolean shouldPersist : values) {
      shadow.setPersistent(shouldPersist);
      assertThat(preference.shouldPersist()).isEqualTo(shouldPersist);
      assertThat(preference.isPersistent()).isEqualTo(shouldPersist);
    }
  }

  @Test
  public void shouldPersistedIn() {
    int defaultValue = 727;
    int[] values = {0, 1, 2, 2011};

    for (int persistedInt : values) {
      shadow.persistInt(persistedInt);

      shadow.setPersistent(false);
      assertThat(preference.getPersistedInt(defaultValue)).isEqualTo(defaultValue);

      shadow.setPersistent(true);
      assertThat(preference.getPersistedInt(defaultValue)).isEqualTo(persistedInt);
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
    assertThat(shadow.getOnPreferenceClickListener()).isSameAs(onPreferenceClickListener);
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

    assertThat(clicked).isFalse();
    assertThat(shadow.click()).isTrue();
    assertThat(clicked).isTrue();
  }

  @Test
  public void shouldRecordCallChangeListenerValue() {
    Integer[] values = {0, 1, 2, 2011};
    preference.setOnPreferenceChangeListener(null);

    for (Integer newValue : values) {
      assertThat(preference.callChangeListener(newValue)).isTrue().as("Case " + newValue);
      assertThat(shadow.getCallChangeListenerValue()).isSameAs(newValue).as("Case " + newValue);
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
    assertThat(preference.callChangeListener(666)).isFalse();
    assertThat(preference.callChangeListener(777)).isTrue();
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
    assertThat(shadow.getOnPreferenceChangeListener()).isSameAs(onPreferenceChangeListener);
  }


  @Test
  public void shouldReturnIntent() {
    assertThat(preference.getIntent()).isNull();
    preference.setIntent(new Intent());
    assertThat(preference.getIntent()).isNotNull();
  }

  @Test
  public void shouldRememberDependency() {
    assertThat(preference.getDependency()).isNull();
    preference.setDependency("TEST_PREF_KEY");
    assertThat(preference.getDependency()).isNotNull();
    assertThat(preference.getDependency()).isEqualTo("TEST_PREF_KEY");
  }

  @Test
  public void getSharedPreferencesShouldReturnSharedPreferences() {
    final SharedPreferences sharedPreferences = preference.getSharedPreferences();
    assertNotNull(sharedPreferences);
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
