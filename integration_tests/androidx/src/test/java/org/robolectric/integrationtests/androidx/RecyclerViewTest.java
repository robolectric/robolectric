package org.robolectric.integrationtests.androidx;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.test.espresso.Espresso;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public final class RecyclerViewTest {

  @Test
  public void smoothScrollBy() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    RecyclerView recyclerView = new RecyclerView(activity);
    LayoutManager layoutManager = new LinearLayoutManager(activity);
    recyclerView.setLayoutManager(layoutManager);
    ((ViewGroup) activity.findViewById(android.R.id.content)).addView(recyclerView);

    // Set adapter with enough items to allow scrolling
    recyclerView.setAdapter(new TestAdapter(50));

    // Important: Measure and layout the RecyclerView
    recyclerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    recyclerView.layout(0, 0, 1000, 1000);

    // Run_scrollBy
    recyclerView.scrollBy(0, 10);
    assertThat(recyclerView.computeVerticalScrollOffset()).isEqualTo(10);

    // Run_smoothScrollBy
    recyclerView.smoothScrollBy(0, 100);
    Espresso.onIdle();
    assertThat(recyclerView.computeVerticalScrollOffset()).isEqualTo(110);
  }

  private static class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {
    private final int itemCount;

    TestAdapter(int itemCount) {
      this.itemCount = itemCount;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = new View(parent.getContext());
      view.setLayoutParams(
          new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, 200)); // Fixed item height
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      // No-op for this test
    }

    @Override
    public int getItemCount() {
      return itemCount;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
      ViewHolder(View itemView) {
        super(itemView);
      }
    }
  }
}
