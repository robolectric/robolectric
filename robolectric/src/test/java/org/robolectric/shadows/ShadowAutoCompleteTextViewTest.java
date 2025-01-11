package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowAutoCompleteTextViewTest {
  private final AutoCompleteAdapter adapter =
      new AutoCompleteAdapter(ApplicationProvider.getApplicationContext());

  @Test
  public void shouldInvokeFilter() {
    shadowMainLooper().pause();
    AutoCompleteTextView view =
        new AutoCompleteTextView(ApplicationProvider.getApplicationContext());
    view.setAdapter(adapter);

    view.setText("Foo");
    assertThat(adapter.getCount()).isEqualTo(2);
  }

  private class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    public AutoCompleteAdapter(Context context) {
      super(context, android.R.layout.simple_list_item_1);
    }

    @Nonnull
    @Override
    public Filter getFilter() {
      return new AutoCompleteFilter();
    }
  }

  private class AutoCompleteFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence text) {
      FilterResults results = new FilterResults();
      if (text != null) {
        results.count = 2;
        results.values = new ArrayList<>(Arrays.asList("Foo", "Bar"));
      }
      return results;
    }

    @Override
    protected void publishResults(CharSequence text, FilterResults results) {
      if (results != null) {
        adapter.clear();
        adapter.addAll((List<String>) results.values);
        adapter.notifyDataSetChanged();
      }
    }
  }
}
