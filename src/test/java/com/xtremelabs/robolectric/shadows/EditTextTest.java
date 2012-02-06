package com.xtremelabs.robolectric.shadows;

import java.util.Random;

import android.widget.EditText;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class EditTextTest {
    @Test
    public void shouldBeFocusableByDefault() throws Exception {
        assertTrue(new EditText(null).isFocusable());
        assertTrue(new EditText(null).isFocusableInTouchMode());
    }
    
    @Test
    public void whenInitializing_thenMaxLengthShouldHaveNoLimitDefined() {
        EditText editText = new EditText(null);
        String input = anyString();
        
        editText.setText(input);
        
        assertThat(editText.getText().toString(), equalTo(input));
    }

    private String anyString() {
        int length = new Random().nextInt(1000);
        StringBuilder stringBuilder = new StringBuilder();
        
        for (int i = 0; i < length; i++)
            stringBuilder.append('x');
        
        return stringBuilder.toString();
    }
}
