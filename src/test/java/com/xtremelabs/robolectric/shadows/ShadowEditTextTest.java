package com.xtremelabs.robolectric.shadows;

import android.widget.EditText;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowEditTextTest {
    private EditText editText;

    @Before
    public void setup() {
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("android:attr/maxLength", "5", R.class.getPackage().getName()));
        TestAttributeSet attributeSet = new TestAttributeSet(attributes, Robolectric.getShadowApplication().getResourceLoader(), null);
        editText = new EditText(Robolectric.application, attributeSet);
    }

    @Test
    public void shouldRespectMaxLength() throws Exception {
        editText.setText("0123456678");
        assertThat(editText.getText().toString(), equalTo("01234"));
    }
    
    @Test
    public void shouldAcceptNullStrings() {
        editText.setText(null);
        assertThat(editText.getText().toString(), equalTo(""));
    }
}
