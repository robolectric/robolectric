package com.xtremelabs.robolectric.shadows;

import android.util.AttributeSet;
import android.widget.EditText;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(WithTestDefaultsRunner.class)
public class EditTextTest {

    @Test
    public void shouldBeFocusableByDefault() throws Exception {
        assertTrue(new EditText(null).isFocusable());
        assertTrue(new EditText(null).isFocusableInTouchMode());
    }

    @Test
    public void givenInitializingWithAttributeSet_whenMaxLengthDefined_thenRestrictTextLengthToMaxLength() {
        int maxLength = anyInteger();
        AttributeSet attrs = attributeSetWithMaxLength(maxLength);
        EditText editText = new EditText(null, attrs);
        String excessiveInput = stringOfLength(maxLength * 2);

        editText.setText(excessiveInput);

        assertThat(editText.getText().toString(), equalTo(excessiveInput.subSequence(0, maxLength)));
    }

    @Test
    public void givenInitializingWithAttributeSet_whenMaxLengthNotDefined_thenTextLengthShouldHaveNoRestrictions() {
        AttributeSet attrs = attributeSetWithoutMaxLength();
        EditText editText = new EditText(null, attrs);
        String input = anyString();

        editText.setText(input);

        assertThat(editText.getText().toString(), equalTo(input));
    }

    @Test
    public void whenInitializingWithoutAttributeSet_thenTextLengthShouldHaveNoRestrictions() {
        EditText editText = new EditText(null);
        String input = anyString();

        editText.setText(input);

        assertThat(editText.getText().toString(), equalTo(input));
    }

    @Test
    public void testSelectAll() {
        EditText editText = new EditText(null);
        editText.setText("foo");

        editText.selectAll();

        ShadowTextView shadowTextView = Robolectric.shadowOf(editText);
        assertThat(shadowTextView.getSelectionStart(), is(0));
        assertThat(shadowTextView.getSelectionEnd(), is(2));
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
}
