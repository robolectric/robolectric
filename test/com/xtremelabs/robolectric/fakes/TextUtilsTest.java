package com.xtremelabs.robolectric.fakes;

import android.text.TextUtils;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DogfoodRobolectricTestRunner.class)
public class TextUtilsTest {
    @Test
    public void testExpandTemplate() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(TextUtils.class, ShadowTextUtils.class);

        assertThat(
                (String)TextUtils.expandTemplate("a^1b^2c^3d", "A", "B", "C", "D"),
                equalTo("aAbBcCd"));
    }
}
