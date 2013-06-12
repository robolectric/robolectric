package org.robolectric.shadows;

import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
abstract public class AdapterViewBehavior {
  private AdapterView adapterView;

  @Before
  public void setUp() throws Exception {
    Robolectric.shadowOf(Looper.getMainLooper()).pause();
    adapterView = createAdapterView();
  }

  abstract public AdapterView createAdapterView();

  @Test public void shouldIgnoreSetSelectionCallsWithInvalidPosition() {
    final Transcript transcript = new Transcript();

    adapterView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        transcript.add("onItemSelected fired");
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    ShadowHandler.idleMainLooper();
    transcript.assertNoEventsSoFar();
    adapterView.setSelection(AdapterView.INVALID_POSITION);
    ShadowHandler.idleMainLooper();
    transcript.assertNoEventsSoFar();
  }

  @Test public void testSetAdapter_ShouldCauseViewsToBeRenderedAsynchronously() throws Exception {
    adapterView.setAdapter(new CountingAdapter(2));

    assertThat(adapterView.getCount()).isEqualTo(2);
    assertThat(adapterView.getChildCount()).isEqualTo(0);

    shadowOf(adapterView).populateItems();
    assertThat(adapterView.getChildCount()).isEqualTo(2);
    assertThat(((TextView) adapterView.getChildAt(0)).getText()).isEqualTo("Item 0");
    assertThat(((TextView) adapterView.getChildAt(1)).getText()).isEqualTo("Item 1");
  }

  @Ignore("maybe not a valid test in the 2.0 world?") // todo 2.0-cleanup
  @Test public void testSetAdapter_ShouldSelectFirstItemAsynchronously() throws Exception {
    final Transcript transcript = new Transcript();

    adapterView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        assertThat(parent).isSameAs(adapterView);
        assertThat(view).isNotNull();
        assertThat(view).isSameAs(adapterView.getChildAt(position));
        assertThat(id).isEqualTo(adapterView.getAdapter().getItemId(position));
        transcript.add("selected item " + position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
    adapterView.setAdapter(new CountingAdapter(2));
    transcript.assertNoEventsSoFar();
    ShadowHandler.idleMainLooper();
    transcript.assertEventsSoFar("selected item 0");
  }

  @Ignore("maybe not a valid test in the 2.0 world?") // todo
  @Test public void testSetAdapter_ShouldFireOnNothingSelectedWhenAdapterCountIsReducedToZero() throws Exception {
    final Transcript transcript = new Transcript();

    adapterView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        transcript.add("onNothingSelected fired");
      }
    });
    CountingAdapter adapter = new CountingAdapter(2);
    adapterView.setAdapter(adapter);
    ShadowHandler.idleMainLooper();
    transcript.assertNoEventsSoFar();
    adapter.setCount(0);
    ShadowHandler.idleMainLooper();
    transcript.assertEventsSoFar("onNothingSelected fired");
  }

  @Test public void testSetEmptyView_ShouldHideAdapterViewIfAdapterIsNull() throws Exception {
    adapterView.setAdapter(null);

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test public void testSetEmptyView_ShouldHideAdapterViewIfAdapterViewIsEmpty() throws Exception {
    adapterView.setAdapter(new CountingAdapter(0));

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test public void testSetEmptyView_ShouldHideEmptyViewIfAdapterViewIsNotEmpty() throws Exception {
    adapterView.setAdapter(new CountingAdapter(1));

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);
  }

  @Test public void testSetEmptyView_ShouldHideEmptyViewWhenAdapterGetsNewItem() throws Exception {
    CountingAdapter adapter = new CountingAdapter(0);
    adapterView.setAdapter(adapter);

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);

    adapter.setCount(1);

    ShadowHandler.idleMainLooper();

    assertThat(adapterView.getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);
  }

  @Test public void testSetEmptyView_ShouldHideAdapterViewWhenAdapterBecomesEmpty() throws Exception {
    CountingAdapter adapter = new CountingAdapter(1);
    adapterView.setAdapter(adapter);

    View emptyView = new View(adapterView.getContext());
    adapterView.setEmptyView(emptyView);

    assertThat(adapterView.getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);

    adapter.setCount(0);

    ShadowHandler.idleMainLooper();

    assertThat(adapterView.getVisibility()).isEqualTo(View.GONE);
    assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Ignore("maybe not a valid test in the 2.0 world?") // todo 2.0-cleanup
  @Test public void shouldOnlyUpdateOnceIfInvalidatedMultipleTimes() {
    final Transcript transcript = new Transcript();
    CountingAdapter adapter = new CountingAdapter(2) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        transcript.add("getView for " + position);
        return super.getView(position, convertView, parent);
      }
    };
    adapterView.setAdapter(adapter);

    transcript.assertNoEventsSoFar();

    adapter.notifyDataSetChanged();
    adapter.notifyDataSetChanged();

    ShadowHandler.idleMainLooper();

    transcript.assertEventsSoFar("getView for 0", "getView for 1");
  }
}
