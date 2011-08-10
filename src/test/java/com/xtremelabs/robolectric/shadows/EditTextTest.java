package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import com.xtremelabs.robolectric.R;
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
    public void shouldGetHintFromXml() {
        Context context = new Activity();
        LayoutInflater inflater = LayoutInflater.from(context);
        EditText editText = (EditText) inflater.inflate(R.layout.edit_text, null);
        assertThat(editText.getHint().toString(), equalTo("Hello, Hint"));
    }
}
