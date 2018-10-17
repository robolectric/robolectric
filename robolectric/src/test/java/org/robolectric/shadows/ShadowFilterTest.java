package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.widget.Filter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowFilterTest {

  @Test
  public void testFilterShouldCallPerformFilteringAndPublishResults() throws InterruptedException {
    final AtomicBoolean performFilteringCalled = new AtomicBoolean(false);
    final AtomicBoolean publishResultsCalled = new AtomicBoolean(false);
    Filter filter = new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        performFilteringCalled.set(true);
        return null;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        assertThat(filterResults).isNull();
        publishResultsCalled.set(true);
      }
    };
    filter.filter("");
    assertThat(performFilteringCalled.get()).isTrue();
    assertThat(publishResultsCalled.get()).isTrue();
  }

  @Test
  public void testFilterShouldCallListenerWithCorrectCount() throws InterruptedException {
    final AtomicBoolean listenerCalled = new AtomicBoolean(false);
    Filter filter = new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        results.values = null;
        results.count = 4;
        return results;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        assertThat(filterResults.values).isNull();
        assertThat(filterResults.count).isEqualTo(4);
      }
    };
    filter.filter("", new Filter.FilterListener() {
      @Override
      public void onFilterComplete(int i) {
        assertThat(i).isEqualTo(4);
        listenerCalled.set(true);
      }
    });
    assertThat(listenerCalled.get()).isTrue();
  }

  @Test
  public void testFilter_whenNullResults_ShouldCallListenerWithMinusOne() throws InterruptedException {
    final AtomicBoolean listenerCalled = new AtomicBoolean(false);
    Filter filter = new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        return null;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {}
    };
    filter.filter("", new Filter.FilterListener() {
      @Override
      public void onFilterComplete(int i) {
        assertThat(i).isEqualTo(-1);
        listenerCalled.set(true);
      }
    });
    assertThat(listenerCalled.get()).isTrue();
  }

  @Test
  public void testFilter_whenExceptionThrown_ShouldReturn() throws InterruptedException {
    final AtomicBoolean listenerCalled = new AtomicBoolean(false);
    Filter filter = new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
        throw new RuntimeException("unchecked exception during filtering");
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {}
    };
    filter.filter("", new Filter.FilterListener() {
      @Override
      public void onFilterComplete(int resultCount) {
        assertThat(resultCount).isEqualTo(0);
        listenerCalled.set(true);
      }
    });
    assertThat(listenerCalled.get()).isTrue();
  }
}
