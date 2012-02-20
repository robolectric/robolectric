package com.xtremelabs.robolectric.shadows;

import android.widget.EditText;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowEditTextTest {
    @Test
    public void shouldRespectMaxLength() throws Exception {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("android:maxLength", "5");
        TestAttributeSet attributeSet = new TestAttributeSet(hash);
        EditText editText = new EditText(Robolectric.application, attributeSet);
        editText.setText("0123456678");
        assertThat(editText.getText().toString(), equalTo("01234"));
    }
}
