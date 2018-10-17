package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.IntentFilter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
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
