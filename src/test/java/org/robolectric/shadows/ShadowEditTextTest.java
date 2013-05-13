package org.robolectric.shadows;

import android.widget.EditText;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowEditTextTest {
  private EditText editText;

  @Before
  public void setup() {
    List<Attribute> attributes = new ArrayList<Attribute>();
    attributes.add(new Attribute("android:attr/maxLength", "5", R.class.getPackage().getName()));
    RoboAttributeSet attributeSet = new RoboAttributeSet(attributes, Robolectric.application.getResources(), null);
    editText = new EditText(Robolectric.application, attributeSet);
  }

  @Test
  public void shouldRespectMaxLength() throws Exception {
    editText.setText("0123456678");
    assertThat(editText.getText().toString()).isEqualTo("01234");
  }

  @Test
  public void shouldAcceptNullStrings() {
    editText.setText(null);
    assertThat(editText.getText().toString()).isEqualTo("");
  }
}
