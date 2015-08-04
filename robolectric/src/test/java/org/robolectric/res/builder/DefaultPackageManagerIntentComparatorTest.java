package org.robolectric.res.builder;

import android.content.Intent;
import org.junit.Test;
import org.assertj.core.api.Assertions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultPackageManagerIntentComparatorTest {

  @Test
  public void validCompareResult() {
    final DefaultPackageManager.IntentComparator intentComparator = new DefaultPackageManager.IntentComparator();

    Assertions.assertThat(intentComparator.compare(null, null)).isEqualTo(0);
    Assertions.assertThat(intentComparator.compare(new Intent(), null)).isEqualTo(1);
    Assertions.assertThat(intentComparator.compare(null, new Intent())).isEqualTo(-1);

    Intent intent1 = new Intent();
    Intent intent2 = new Intent();

    Assertions.assertThat(intentComparator.compare(intent1, intent2)).isEqualTo(0);
  }

  @Test
  public void canSustainConcurrentModification() {
    final DefaultPackageManager.IntentComparator intentComparator = new DefaultPackageManager.IntentComparator();

    Intent mockedIntent1 = mock(Intent.class);
    when(mockedIntent1.getAction()).thenReturn("actionstring0", null);

    Intent mockedIntent2 = mock(Intent.class);
    when(mockedIntent2.getAction()).thenReturn("actionstring1", null);

    Assertions.assertThat(intentComparator.compare(mockedIntent1, mockedIntent2)).isEqualTo(-1);
  }

}
