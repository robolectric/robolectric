package com.xtremelabs.robolectric.shadows;

import android.widget.NumberPicker;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.fail;

@RunWith(WithTestDefaultsRunner.class)
public class NumberPickerTest {
    @Test
    public void setDisplayedValues_shouldCheckArraySize() throws Exception {
        NumberPicker picker = new NumberPicker(Robolectric.application);
        picker.setMaxValue(2);

        try {
            picker.setDisplayedValues(new String[] {"0", "1"});
            fail("should have complained about being too small");
        } catch (Exception e) {
            // pass
        }

        picker.setDisplayedValues(new String[] {"0", "1", "2"});
        // ahhh, just right

        try {
            picker.setDisplayedValues(new String[] {"0", "1", "2", "3"});
            fail("should have complained about being too big");
        } catch (Exception e) {
            // pass
        }
    }
}
