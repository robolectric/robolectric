package com.xtremelabs.droidsugar.fakes;

import android.util.Base64;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class Base64Test {

    @Before
    public void setUp() {
        DroidSugarAndroidTestRunner.addProxy(Base64.class, FakeBase64.class);
    }
    
    @Test
    public void shouldSimulateEncodingOfByeArrays() {
        String encodedValue = Base64.encodeToString("hello world".getBytes(), Base64.DEFAULT);
        assertThat(encodedValue, equalTo("hello world__fake_Base64_encode_string__" + Base64.DEFAULT));
    }
}
