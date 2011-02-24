package com.xtremelabs.robolectric.shadows;

import android.text.TextUtils;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TextUtilsTest {
    @Test
    public void testExpandTemplate() throws Exception {
        assertThat(
                (String) TextUtils.expandTemplate("a^1b^2c^3d", "A", "B", "C", "D"),
                equalTo("aAbBcCd"));
    }

    @Test
    public void testIsEmpty() throws Exception {
        assertThat(TextUtils.isEmpty(null), equalTo(true));
        assertThat(TextUtils.isEmpty(""), equalTo(true));
        assertThat(TextUtils.isEmpty(" "), equalTo(false));
        assertThat(TextUtils.isEmpty("123"), equalTo(false));
    }
}
