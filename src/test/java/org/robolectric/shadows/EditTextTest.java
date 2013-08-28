package org.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;

import java.util.Arrays;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class EditTextTest {

  @Ignore("maybe not a valid test in the 2.0 world?") // todo 2.0-cleanup
  @Test
  public void shouldBeFocusableByDefault() throws Exception {
    assertThat(new EditText(Robolectric.application).isFocusable()).isTrue();
    assertThat(new EditText(Robolectric.application).isFocusableInTouchMode()).isFalse();
  }

  @Test
  public void givenInitializingWithAttributeSet_whenMaxLengthDefined_thenRestrictTextLengthToMaxLength() {
    int maxLength = anyInteger();
    AttributeSet attrs = attributeSetWithMaxLength(maxLength);
    EditText editText = new EditText(Robolectric.application, attrs);
    String excessiveInput = stringOfLength(maxLength * 2);

    editText.setText(excessiveInput);

    assertThat((CharSequence) editText.getText().toString()).isEqualTo(excessiveInput.subSequence(0, maxLength));
  }

  @Test
  public void givenInitializingWithAttributeSet_whenMaxLengthNotDefined_thenTextLengthShouldHaveNoRestrictions() {
    AttributeSet attrs = attributeSetWithoutMaxLength();
    EditText editText = new EditText(Robolectric.application, attrs);
    String input = anyString();

    editText.setText(input);

    assertThat(editText.getText().toString()).isEqualTo(input);
  }

  @Test
  public void whenInitializingWithoutAttributeSet_thenTextLengthShouldHaveNoRestrictions() {
    EditText editText = new EditText(Robolectric.application);
    String input = anyString();

    editText.setText(input);

    assertThat(editText.getText().toString()).isEqualTo(input);
  }

  @Test
  public void testSelectAll() {
    EditText editText = new EditText(Robolectric.application);
    editText.setText("foo");

    editText.selectAll();

    assertThat(editText.getSelectionStart()).isEqualTo(0);
    assertThat(editText.getSelectionEnd()).isEqualTo(3);
  }

  private String anyString() {
    return stringOfLength(anyInteger());
  }

  private String stringOfLength(int length) {
    StringBuilder stringBuilder = new StringBuilder();

    for (int i = 0; i < length; i++)
      stringBuilder.append('x');

    return stringBuilder.toString();
  }

  private int anyInteger() {
    return new Random().nextInt(1000) + 1;
  }

  private AttributeSet attributeSetWithMaxLength(int maxLength) {
    return new RoboAttributeSet(
        asList(new Attribute(new ResName("android", "attr", "maxLength"), maxLength + "", "android")),
        Robolectric.application.getResources(), null);
  }

  private AttributeSet attributeSetWithoutMaxLength() {
    return new RoboAttributeSet(Arrays.<Attribute>asList(),
        Robolectric.application.getResources(), null);
  }

  @Test
  public void shouldGetHintFromXml() {
    Context context = Robolectric.application;
    LayoutInflater inflater = LayoutInflater.from(context);
    EditText editText = (EditText) inflater.inflate(R.layout.edit_text, null);
    assertThat(editText.getHint().toString()).isEqualTo("Hello, Hint");
  }
}
