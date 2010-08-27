package com.xtremelabs.droidsugar.fakes;

import android.text.TextUtils;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class TextUtilsTest {
    @Test
    public void testExpandTemplate() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(TextUtils.class, FakeTextUtils.class);

        assertThat(
                (String)TextUtils.expandTemplate("a^1b^2c^3d", "A", "B", "C", "D"),
                equalTo("aAbBcCd"));
    }
}
