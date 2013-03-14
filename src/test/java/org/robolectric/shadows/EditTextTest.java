package org.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.Random;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(TestRunners.WithDefaults.class)
public class EditTextTest {

    @Test
    public void shouldBeFocusableByDefault() throws Exception {
        assertTrue(new EditText(Robolectric.application).isFocusable());
        assertTrue(new EditText(Robolectric.application).isFocusableInTouchMode());
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

        ShadowTextView shadowTextView = Robolectric.shadowOf(editText);
        assertThat(shadowTextView.getSelectionStart()).isEqualTo(0);
        assertThat(shadowTextView.getSelectionEnd()).isEqualTo(2);
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
        AttributeSet attrs = mock(AttributeSet.class);
        when(attrs.getAttributeIntValue(eq("android"), eq("maxLength"), anyInt())).thenReturn(maxLength);
        return attrs;
    }

    private AttributeSet attributeSetWithoutMaxLength() {
        AttributeSet attrs = mock(AttributeSet.class);
        when(attrs.getAttributeIntValue("android", "maxLength", Integer.MAX_VALUE)).thenReturn(Integer.MAX_VALUE);
        return attrs;
    }

    @Test
    public void shouldGetHintFromXml() {
        Context context = new Activity();
        LayoutInflater inflater = LayoutInflater.from(context);
        EditText editText = (EditText) inflater.inflate(R.layout.edit_text, null);
        assertThat(editText.getHint().toString()).isEqualTo("Hello, Hint");
    }
}
