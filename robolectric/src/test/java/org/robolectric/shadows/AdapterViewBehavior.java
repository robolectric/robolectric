package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

@RunWith(AndroidJUnit4.class)
abstract public class AdapterViewBehavior {
  private AdapterView adapterView;

  @Before
  public void setUp() throws Exception {
    Shadows.shadowOf(Looper.getMainLooper()).pause();
    adapterView = createAdapterView();
  }

  abstract public AdapterView createAdapterView();

  @Test public void shouldIgnoreSetSelectionCallsWithInvalidPosition() {
    final List<String> transcript = new ArrayList<>();

    adapterView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        transcript.add("onItemSelected fired");
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    ShadowLooper.idleMainLooper();
    assertThat(transcript).isEmpty();
    adapterView.setSelection(AdapterView.INVALID_POSITION);
    ShadowLooper.idleMainLooper();
    assertThat(transcript).isEmpty();
  }

  @Test public void testSetAdapter_ShouldCauseViewsToBeRenderedAsynchronously() throws Exception {
    adapterView.setAdapter(new ShadowCountingAdapter(2));

    assertThat(adapterView.getCount()).isEqualTo(2);
    assertThat(adapterView.getChildCount()).isEqualTo(0);

    shadowOf(adapterView).populateItems();
    assertThat(adapterView.getChildCount()).isEqualTo(2);
    assertThat(((TextView) adapterView.getChildAt(0)).getText()).isEqualTo("Item 0");
    assertThat(((TextView) adapterView.getChildAt(1)).getText()).isEqualTo("Item 1");
  }

  @Test public void testSetEmptyView_ShouldHideAdapterViewIfAdapterIsNull() throws Exception {
    adapterView.setAdapter(null);

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test public void testSetEmptyView_ShouldHideAdapterViewIfAdapterViewIsEmpty() throws Exception {
    adapterView.setAdapter(new ShadowCountingAdapter(0));

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test public void testSetEmptyView_ShouldHideEmptyViewIfAdapterViewIsNotEmpty() throws Exception {
    adapterView.setAdapter(new ShadowCountingAdapter(1));

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);
  }

  @Test public void testSetEmptyView_ShouldHideEmptyViewWhenAdapterGetsNewItem() throws Exception {
    ShadowCountingAdapter adapter = new ShadowCountingAdapter(0);
    adapterView.setAdapter(adapter);

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);

    adapter.setCount(1);

    ShadowLooper.idleMainLooper();

    assertThat(adapterView.getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);
  }

  @Test public void testSetEmptyView_ShouldHideAdapterViewWhenAdapterBecomesEmpty() throws Exception {
    ShadowCountingAdapter adapter = new ShadowCountingAdapter(1);
    adapterView.setAdapter(adapter);

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);

    adapter.setCount(0);

    ShadowLooper.idleMainLooper();

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
  }
}
