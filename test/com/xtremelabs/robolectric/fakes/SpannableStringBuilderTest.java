package com.xtremelabs.robolectric.fakes;

import android.text.SpannableStringBuilder;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DogfoodRobolectricTestRunner.class)
public class SpannableStringBuilderTest {
    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(SpannableStringBuilder.class, ShadowSpannableStringBuilder.class);
    }

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
}
