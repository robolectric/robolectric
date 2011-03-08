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
    public void testIsNotEmpty() {
    	assertThat( TextUtils.isEmpty( "test" ), equalTo( false ) );
    }
    
    @Test
    public void testIsNotEmptyWhitespace() {
    	assertThat( TextUtils.isEmpty( " " ), equalTo( false ) );    	
    }
    
    @Test
    public void testIsEmptyNull() {
    	assertThat( TextUtils.isEmpty( null ), equalTo( true ) );
    }
    
    @Test
    public void testIsEmptyZeroLengthString() {
    	assertThat( TextUtils.isEmpty( "" ), equalTo( true ) );
    }
}
