package org.robolectric.android;

import static com.google.common.truth.Truth.assertThat;

import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowPackageManager.IntentComparator;

@RunWith(AndroidJUnit4.class)
public class DefaultPackageManagerIntentComparatorTest {

  @Test
  public void validCompareResult() {
    final IntentComparator intentComparator = new IntentComparator();

    assertThat(intentComparator.compare(null, null)).isEqualTo(0);
    assertThat(intentComparator.compare(new Intent(), null)).isEqualTo(1);
    assertThat(intentComparator.compare(null, new Intent())).isEqualTo(-1);

    Intent intent1 = new Intent();
    Intent intent2 = new Intent();

    assertThat(intentComparator.compare(intent1, intent2)).isEqualTo(0);
  }

  @Test
  public void canSustainConcurrentModification() {
    final IntentComparator intentComparator = new IntentComparator();

    Intent intent1 = new Intent("actionstring0");
    Intent intent2 = new Intent("actionstring1");
    assertThat(intentComparator.compare(intent1, intent2)).isEqualTo(-1);
  }

}
