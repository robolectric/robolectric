package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowPreferenceGroupTest {

  private TestPreferenceGroup group;
  private ShadowPreference shadow;
  private Activity activity;
  private AttributeSet attrs;
  private Preference pref1, pref2;

  @Before
  public void setUp() throws Exception {
    activity = buildActivity(Activity.class).create().get();
    attrs =  Robolectric.buildAttributeSet().build();

    group = new TestPreferenceGroup(activity, attrs);
    shadow = shadowOf(group);
    shadow.callOnAttachedToHierarchy(new PreferenceManager(activity, 0));

    pref1 = new Preference(activity);
    pref1.setKey("pref1");

    pref2 = new Preference(activity);
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
    TestPreferenceGroup group2 = new TestPreferenceGroup(activity, attrs);
    shadowOf(group2).callOnAttachedToHierarchy(new PreferenceManager(activity, 0));
    group2.addPreference(pref2);

    group.addPreference(pref1);
    group.addPreference(group2);

    assertThat(group.findPreference(pref2.getKey())).isSameAs(pref2);
  }

  @Test
  public void shouldSetEnabledRecursively() {
    boolean[] values = {false, true};

    TestPreferenceGroup group2 = new TestPreferenceGroup(activity, attrs);
    shadowOf(group2).callOnAttachedToHierarchy(new PreferenceManager(activity, 0));
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
