package org.robolectric.shadows;

import android.content.IntentFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowIntentFilterAuthorityEntryTest {
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
