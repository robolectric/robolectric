package org.robolectric.shadows;

import android.content.Context;
import android.preference.DialogPreference;
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

@RunWith(TestRunners.WithDefaults.class)
public class DialogPreferenceTest {

  private static final String TEST_DIALOG_MESSAGE = "This is only a test";

  private DialogPreference preference;
  private ShadowDialogPreference shadow;

  private Context context;
  private RoboAttributeSet attrs;

  @Before
  public void setup() {
    List<Attribute> attributes = new ArrayList<Attribute>();
    attributes.add(new Attribute("android:attr/dialogMessage", TEST_DIALOG_MESSAGE, R.class.getPackage().getName()));
    context = Robolectric.application;
    attrs = new RoboAttributeSet(attributes, TestUtil.emptyResources(), null);
    preference = new TestDialogPreference(context, attrs);
    shadow = Robolectric.shadowOf(preference);
  }

  @Test
  public void testConstructors() {
    int defStyle = android.R.attr.buttonStyle;

    preference = new TestDialogPreference(context, attrs, defStyle);
    shadow = Robolectric.shadowOf(preference);
    assertThat(shadow.getContext()).isSameAs(context);
    assertThat(shadow.getAttrs()).isSameAs(attrs);
    assertThat(shadow.getDefStyle()).isEqualTo(defStyle);

    preference = new TestDialogPreference(context, attrs);
    shadow = Robolectric.shadowOf(preference);
    assertThat(shadow.getContext()).isSameAs(context);
    assertThat(shadow.getAttrs()).isSameAs(attrs);
    assertThat(shadow.getDefStyle()).isGreaterThan(7);
  }

  @Test
  public void testGetDialogMessage() {
    assertThat((String) preference.getDialogMessage()).isEqualTo(TEST_DIALOG_MESSAGE);
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
