package org.robolectric.shadows;

import android.accounts.Account;
import android.content.PeriodicSync;
import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class PeriodicSyncTest {

  @Test
  public void shouldHaveConstructor() throws Exception {
    Account a = new Account("a", "b");
    PeriodicSync sync = new PeriodicSync(a, "auth",
        new Bundle(), 120l);

    assertThat(sync.account).isSameAs(a);
    assertThat(sync.authority).isEqualTo("auth");
    assertThat(sync.period).isEqualTo(120l);
    assertNotNull(sync.extras);
  }
}
