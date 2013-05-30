package com.xtremelabs.robolectric.shadows.testing;

import android.text.style.ScaleXSpan;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ScaleXSpanTest {
    @Test
    public void getScaleX_returnsValueFromConstruction() throws Exception {
        assertThat(new ScaleXSpan(1.5f).getScaleX(), equalTo(1.5f));
    }
}
