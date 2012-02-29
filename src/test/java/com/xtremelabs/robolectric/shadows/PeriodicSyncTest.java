package com.xtremelabs.robolectric.shadows;

import android.accounts.Account;
import android.content.PeriodicSync;
import android.os.Bundle;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class PeriodicSyncTest {

    @Test
    public void shouldHaveConstructor() throws Exception {
        Account a = new Account("a", "b");
        PeriodicSync sync = new PeriodicSync(a, "auth",
                new Bundle(), 120l);

        assertThat(sync.account, is(a));
        assertThat(sync.authority, equalTo("auth"));
        assertThat(sync.period, equalTo(120l));
        assertNotNull(sync.extras);
    }
}
