package org.robolectric.shadows;

import android.telephony.PhoneNumberUtils;
import org.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PhoneNumberUtilTest {
    @Test
    public void testFormatNumber() {
        assertThat(PhoneNumberUtils.formatNumber("12345678901")).isEqualTo("12345678901-formatted");
    }

    @Test
    public void testStripSeparators() {
        assertThat(PhoneNumberUtils.stripSeparators("12345678901")).isEqualTo("12345678901-stripped");
    }
        
}
