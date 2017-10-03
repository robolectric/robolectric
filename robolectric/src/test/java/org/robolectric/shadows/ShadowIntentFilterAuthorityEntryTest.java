package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.IntentFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
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
