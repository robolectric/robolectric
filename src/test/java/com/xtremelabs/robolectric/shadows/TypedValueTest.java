package com.xtremelabs.robolectric.shadows;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TypedValueTest {

    @Test
    public void testApplyDimensionIsWired() throws Exception {
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.density = 0.5f;
        float convertedValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, metrics);
        assertThat(convertedValue, equalTo(50f));
    }
}
