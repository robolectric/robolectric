package com.xtremelabs.droidsugar.fakes;

import android.text.SpannableStringBuilder;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class SpannableStringBuilderTest {
    @Test
    public void testAppend() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(SpannableStringBuilder.class, FakeSpannableStringBuilder.class);
        SpannableStringBuilder builder = new SpannableStringBuilder("abc");
        builder.append('d').append("e").append("f");
        assertThat(builder.toString(), equalTo("abcdef"));
    }

}
