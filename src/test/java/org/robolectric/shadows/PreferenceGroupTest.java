package org.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.util.AttributeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.util.TestUtil;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceGroupTest {

  private TestPreferenceGroup group;
  private ShadowPreferenceGroup shadow;
  private Context context;
  private RoboAttributeSet attrs;
  private Preference pref1, pref2;

  @Before
  public void setUp() throws Exception {
    context = buildActivity(Activity.class).create().get();
    attrs = new RoboAttributeSet(new ArrayList<Attribute>(), TestUtil.emptyResources(), null);

    group = new TestPreferenceGroup(context, attrs);
    shadow = Robolectric.shadowOf(group);

    pref1 = new Preference(context);
    pref1.setKey("pref1");

    pref2 = new Preference(context);
    pref2.setKey("pref2");
  }

  @Test
  public void shouldInheritFromPreference() {
    assertThat(shadow).isInstanceOf(ShadowPreference.class);
  }

  @Test
  public void shouldAddPreferences() {
    assertThat(group.getPreferenceCount()).isEqualTo(0);

    // First add succeeds
    assertThat(group.addPreference(pref1)).isTrue();
    assertThat(group.getPreferenceCount()).isEqualTo(1);

    // Dupe add fails silently
    assertThat(group.addPreference(pref1)).isTrue();
    assertThat(group.getPreferenceCount()).isEqualTo(1);

    // Second add succeeds
    assertThat(group.addPreference(pref2)).isTrue();
    assertThat(group.getPreferenceCount()).isEqualTo(2);
  }

  @Test
  public void shouldAddItemFromInflater() {
    assertThat(group.getPreferenceCount()).isEqualTo(0);

    // First add succeeds
    group.addItemFromInflater(pref1);
    assertThat(group.getPreferenceCount()).isEqualTo(1);

    // Dupe add fails silently
    group.addItemFromInflater(pref1);
    assertThat(group.getPreferenceCount()).isEqualTo(1);

    // Second add succeeds
    group.addItemFromInflater(pref2);
    assertThat(group.getPreferenceCount()).isEqualTo(2);
  }

  @Test
  public void shouldGetPreference() {
    group.addPreference(pref1);
    group.addPreference(pref2);

    assertThat(group.getPreference(0)).isSameAs(pref1);
    assertThat(group.getPreference(1)).isSameAs(pref2);
  }

  @Test
  public void shouldGetPreferenceCount() {
    assertThat(group.getPreferenceCount()).isEqualTo(0);
    group.addPreference(pref1);
    assertThat(group.getPreferenceCount()).isEqualTo(1);
    group.addPreference(pref2);
    assertThat(group.getPreferenceCount()).isEqualTo(2);
  }

  @Test
  public void shouldRemovePreference() {
    group.addPreference(pref1);
    group.addPreference(pref2);
    assertThat(group.getPreferenceCount()).isEqualTo(2);

    // First remove succeeds
    assertThat(group.removePreference(pref1)).isTrue();
    assertThat(group.getPreferenceCount()).isEqualTo(1);

    // Dupe remove fails
    assertThat(group.removePreference(pref1)).isFalse();
    assertThat(group.getPreferenceCount()).isEqualTo(1);

    // Second remove succeeds
    assertThat(group.removePreference(pref2)).isTrue();
    assertThat(group.getPreferenceCount()).isEqualTo(0);
  }

  @Test
  public void shouldRemoveAll() {
    group.addPreference(pref1);
    group.addPreference(pref2);
    assertThat(group.getPreferenceCount()).isEqualTo(2);

    group.removeAll();
    assertThat(group.getPreferenceCount()).isEqualTo(0);
  }

  @Test
  public void shouldFindPreference() {
    group.addPreference(pref1);
    group.addPreference(pref2);

    assertThat(group.findPreference(pref1.getKey())).isSameAs(pref1);
    assertThat(group.findPreference(pref2.getKey())).isSameAs(pref2);
  }

  @Test
  public void shouldFindPreferenceRecursively() {
    TestPreferenceGroup group2 = new TestPreferenceGroup(context, attrs);
    group2.addPreference(pref2);

    group.addPreference(pref1);
    group.addPreference(group2);

    assertThat(group.findPreference(pref2.getKey())).isSameAs(pref2);
  }

  @Test
  public void shouldSetEnabledRecursively() {
    boolean[] values = {false, true};

    TestPreferenceGroup group2 = new TestPreferenceGroup(context, attrs);
    group2.addPreference(pref2);

    group.addPreference(pref1);
    group.addPreference(group2);

    for (boolean enabled : values) {
      group.setEnabled(enabled);

      assertThat(group.isEnabled()).isEqualTo(enabled);
      assertThat(group2.isEnabled()).isEqualTo(enabled);
      assertThat(pref1.isEnabled()).isEqualTo(enabled);
      assertThat(pref2.isEnabled()).isEqualTo(enabled);
    }
  }

  private static class TestPreferenceGroup extends PreferenceGroup {
    public TestPreferenceGroup(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }
}
