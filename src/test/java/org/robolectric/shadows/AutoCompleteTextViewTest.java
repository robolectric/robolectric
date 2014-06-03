package org.robolectric.shadows;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import com.google.android.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AutoCompleteTextViewTest {
  private final AutoCompleteAdapter adapter = new AutoCompleteAdapter(Robolectric.application);

  @Test
  public void shouldInvokeFilter() throws Exception {
    Robolectric.getUiThreadScheduler().pause();
    AutoCompleteTextView view = new AutoCompleteTextView(Robolectric.application);
    view.setAdapter(adapter);

    view.setText("Foo");
    assertThat(adapter.getCount()).isEqualTo(2);
  }

  private class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    public AutoCompleteAdapter(Context context) {
      super(context, android.R.layout.simple_list_item_1);
    }

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
        results.values = Lists.newArrayList("Foo", "Bar");
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
