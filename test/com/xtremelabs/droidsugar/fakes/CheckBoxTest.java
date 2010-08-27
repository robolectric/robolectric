package com.xtremelabs.droidsugar.fakes;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class CheckBoxTest {
    @Test
    public void testWorks() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(CompoundButton.class, FakeCompoundButton.class);
        CheckBox checkBox = new CheckBox(null);
        assertThat(checkBox.isChecked(), equalTo(false));

        checkBox.setChecked(true);
        assertThat(checkBox.isChecked(), equalTo(true));

        checkBox.performClick();
        assertThat(checkBox.isChecked(), equalTo(false));

        checkBox.toggle();
        assertThat(checkBox.isChecked(), equalTo(true));
    }
}
