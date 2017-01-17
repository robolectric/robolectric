package org.robolectric.res.builder;

import android.content.Intent;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultPackageManagerIntentComparatorTest {

  @Test
  public void validCompareResult() {
    final DefaultPackageManager.IntentComparator intentComparator = new DefaultPackageManager.IntentComparator();

    assertThat(intentComparator.compare(null, null)).isEqualTo(0);
    assertThat(intentComparator.compare(new Intent(), null)).isEqualTo(1);
    assertThat(intentComparator.compare(null, new Intent())).isEqualTo(-1);

    Intent intent1 = new Intent();
    Intent intent2 = new Intent();

    assertThat(intentComparator.compare(intent1, intent2)).isEqualTo(0);
  }

  @Test
  public void canSustainConcurrentModification() {
    final DefaultPackageManager.IntentComparator intentComparator = new DefaultPackageManager.IntentComparator();

    Intent intent1 = new Intent("actionstring0");
    Intent intent2 = new Intent("actionstring1");
    assertThat(intentComparator.compare(intent1, intent2)).isEqualTo(-1);
  }

}
