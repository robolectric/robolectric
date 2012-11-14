package com.xtremelabs.robolectric.shadows;

import android.content.IntentFilter;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class IntentFilterAuthorityEntryTest {
    @Test(expected = NumberFormatException.class)
    public void constructor_shouldThrowAnExceptionIfPortIsNotAValidNumber() throws Exception {
        new IntentFilter.AuthorityEntry("", "not a number");
    }

    @Test
    public void constructor_shouldAllowNullPortAndSetToNegativeOne() throws Exception {
        IntentFilter.AuthorityEntry authorityEntry = new IntentFilter.AuthorityEntry("host", null);
        assertThat(authorityEntry.getPort(), equalTo(-1));
    }
}
