package org.robolectric.shadows;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;

import java.util.Arrays;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class EditTextTest {

  @Test
  public void givenInitializingWithAttributeSet_whenMaxLengthDefined_thenRestrictTextLengthToMaxLength() {
    int maxLength = anyInteger();
    AttributeSet attrs = attributeSetWithMaxLength(maxLength);
    EditText editText = new EditText(RuntimeEnvironment.application, attrs);
    String excessiveInput = stringOfLength(maxLength * 2);

    editText.setText(excessiveInput);

    assertThat((CharSequence) editText.getText().toString()).isEqualTo(excessiveInput.subSequence(0, maxLength));
  }

  @Test
  public void givenInitializingWithAttributeSet_whenMaxLengthNotDefined_thenTextLengthShouldHaveNoRestrictions() {
    AttributeSet attrs = attributeSetWithoutMaxLength();
    EditText editText = new EditText(RuntimeEnvironment.application, attrs);
    String input = anyString();

    editText.setText(input);

    assertThat(editText.getText().toString()).isEqualTo(input);
  }

  @Test
  public void whenInitializingWithoutAttributeSet_thenTextLengthShouldHaveNoRestrictions() {
    EditText editText = new EditText(RuntimeEnvironment.application);
    String input = anyString();

    editText.setText(input);

    assertThat(editText.getText().toString()).isEqualTo(input);
  }

  @Test
  public void testSelectAll() {
    EditText editText = new EditText(RuntimeEnvironment.application);
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
    Resources resources = RuntimeEnvironment.application.getResources();
    ResourceLoader resourceLoader = shadowOf(resources).getResourceLoader();
    return new RoboAttributeSet(
        asList(new Attribute(new ResName("android", "attr", "maxLength"), maxLength + "", "android")),
        resourceLoader);
  }

  private AttributeSet attributeSetWithoutMaxLength() {
    Resources resources = RuntimeEnvironment.application.getResources();
    ResourceLoader resourceLoader = shadowOf(resources).getResourceLoader();
    return new RoboAttributeSet(Arrays.<Attribute>asList(),
        resourceLoader);
  }

  @Test
  public void shouldGetHintFromXml() {
    Context context = RuntimeEnvironment.application;
    LayoutInflater inflater = LayoutInflater.from(context);
    EditText editText = (EditText) inflater.inflate(R.layout.edit_text, null);
    assertThat(editText.getHint().toString()).isEqualTo("Hello, Hint");
  }
}
