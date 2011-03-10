package com.xtremelabs.robolectric.shadows;

import android.text.SpannableStringBuilder;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SpannableStringBuilderTest {

    @Test
    public void testAppend() throws Exception {
        SpannableStringBuilder builder = new SpannableStringBuilder("abc");
        builder.append('d').append("e").append("f");
        assertThat(builder.toString(), equalTo("abcdef"));
    }

    @Test
    public void testLength() throws Exception {
        SpannableStringBuilder builder = new SpannableStringBuilder("abc");
        assertThat(builder.length(), equalTo(3));
    }

    @Test
    public void testReplace() throws Exception {
        SpannableStringBuilder builder = new SpannableStringBuilder("abcd");
        builder.replace(1,3,"XXX");
        assertThat(builder.toString(), equalTo("aXXXd"));
    }

    @Test
    public void testReplace_extraParams() throws Exception {
        SpannableStringBuilder builder = new SpannableStringBuilder("abcd");
        builder.replace(1,3,"ignoreXXXignore", 6, 9);
        assertThat(builder.toString(), equalTo("aXXXd"));
    }
}
