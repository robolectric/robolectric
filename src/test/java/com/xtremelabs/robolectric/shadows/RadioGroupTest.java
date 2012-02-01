package com.xtremelabs.robolectric.shadows;

import android.widget.RadioGroup;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class RadioGroupTest {
    @Test
    public void checkedRadioButtonId() throws Exception {
        RadioGroup radioGroup = new RadioGroup(null);
        assertThat(radioGroup.getCheckedRadioButtonId(), equalTo(-1));
        radioGroup.check(99);
        assertThat(radioGroup.getCheckedRadioButtonId(), equalTo(99));
    }
}
