package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public abstract class AdapterViewBehavior {
  private AdapterView<ShadowCountingAdapter> adapterView;

  @Before
  public void setUp() throws Exception {
    shadowMainLooper().pause();
    adapterView = createAdapterView();
  }

  public abstract AdapterView<ShadowCountingAdapter> createAdapterView();

  @Test
  public void shouldIgnoreSetSelectionCallsWithInvalidPosition() {
    final List<String> transcript = new ArrayList<>();

    adapterView.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            transcript.add("onItemSelected fired");
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });

    shadowMainLooper().idle();
    assertThat(transcript).isEmpty();
    adapterView.setSelection(AdapterView.INVALID_POSITION);
    shadowMainLooper().idle();
    assertThat(transcript).isEmpty();
  }

  @Test
  public void testSetAdapter_shouldCauseViewsToBeRenderedAsynchronously() throws Exception {
    adapterView.setAdapter(new ShadowCountingAdapter(2));

    assertThat(adapterView.getCount()).isEqualTo(2);
    assertThat(adapterView.getChildCount()).isEqualTo(0);

    shadowOf(adapterView).populateItems();
    assertThat(adapterView.getChildCount()).isEqualTo(2);
    assertThat(((TextView) adapterView.getChildAt(0)).getText().toString()).isEqualTo("Item 0");
    assertThat(((TextView) adapterView.getChildAt(1)).getText().toString()).isEqualTo("Item 1");
  }

  @Test
  public void testSetEmptyView_shouldHideAdapterViewIfAdapterIsNull() throws Exception {
    adapterView.setAdapter(null);

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void testSetEmptyView_shouldHideAdapterViewIfAdapterViewIsEmpty() throws Exception {
    adapterView.setAdapter(new ShadowCountingAdapter(0));

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void testSetEmptyView_shouldHideEmptyViewIfAdapterViewIsNotEmpty() throws Exception {
    adapterView.setAdapter(new ShadowCountingAdapter(1));

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void testSetEmptyView_shouldHideEmptyViewWhenAdapterGetsNewItem() throws Exception {
    ShadowCountingAdapter adapter = new ShadowCountingAdapter(0);
    adapterView.setAdapter(adapter);

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);

    adapter.setCount(1);

    shadowMainLooper().idle();

    assertThat(adapterView.getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void testSetEmptyView_shouldHideAdapterViewWhenAdapterBecomesEmpty() throws Exception {
    ShadowCountingAdapter adapter = new ShadowCountingAdapter(1);
    adapterView.setAdapter(adapter);

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);

    adapter.setCount(0);

    shadowMainLooper().idle();

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
  }
}
