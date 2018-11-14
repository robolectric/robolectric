package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowEditTextTest {
  private EditText editText;
  private Application context;

  @Before
  public void setup() {
    AttributeSet attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.maxLength, "5")
        .build();

    context = ApplicationProvider.getApplicationContext();
    editText = new EditText(context, attributeSet);
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

  @Test
  public void givenInitializingWithAttributeSet_whenMaxLengthDefined_thenRestrictTextLengthToMaxLength() {
    int maxLength = anyInteger();
    AttributeSet attrs = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.maxLength, maxLength + "")
        .build();

    EditText editText = new EditText(context, attrs);
    String excessiveInput = stringOfLength(maxLength * 2);

    editText.setText(excessiveInput);

    assertThat((CharSequence) editText.getText().toString()).isEqualTo(excessiveInput.subSequence(0, maxLength));
  }

  @Test
  public void givenInitializingWithAttributeSet_whenMaxLengthNotDefined_thenTextLengthShouldHaveNoRestrictions() {
    AttributeSet attrs = Robolectric.buildAttributeSet().build();
    EditText editText = new EditText(context, attrs);
    String input = anyString();

    editText.setText(input);

    assertThat(editText.getText().toString()).isEqualTo(input);
  }

  @Test
  public void whenInitializingWithoutAttributeSet_thenTextLengthShouldHaveNoRestrictions() {
    EditText editText = new EditText(context);
    String input = anyString();

    editText.setText(input);

    assertThat(editText.getText().toString()).isEqualTo(input);
  }

  @Test
  public void testSelectAll() {
    EditText editText = new EditText(context);
    editText.setText("foo");

    editText.selectAll();

    assertThat(editText.getSelectionStart()).isEqualTo(0);
    assertThat(editText.getSelectionEnd()).isEqualTo(3);
  }

  @Test
  public void shouldGetHintFromXml() {
    LayoutInflater inflater = LayoutInflater.from(context);
    EditText editText = (EditText) inflater.inflate(R.layout.edit_text, null);
    assertThat(editText.getHint().toString()).isEqualTo("Hello, Hint");
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

}
