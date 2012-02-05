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
    public void testDelete() throws Exception {
        SpannableStringBuilder builder = new SpannableStringBuilder("abc");
        assertThat(builder.length(), equalTo(3));
        builder.delete( 0, 3 );
        assertThat( builder.length(), equalTo(0));
    }
}
