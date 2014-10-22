package org.robolectric.shadows;

import android.content.IntentFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class IntentFilterAuthorityEntryTest {
  @Test(expected = NumberFormatException.class)
  public void constructor_shouldThrowAnExceptionIfPortIsNotAValidNumber() throws Exception {
    new IntentFilter.AuthorityEntry("", "not a number");
  }

  @Test
  public void constructor_shouldAllowNullPortAndSetToNegativeOne() throws Exception {
    IntentFilter.AuthorityEntry authorityEntry = new IntentFilter.AuthorityEntry("host", null);
    assertThat(authorityEntry.getPort()).isEqualTo(-1);
  }
}
