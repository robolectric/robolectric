package org.robolectric.shadows;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.shadowOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

@RunWith(TestRunners.WithDefaults.class)
public class ListViewTest {

  private Transcript transcript;
  private ListView listView;
  private int checkedItemPosition;
  private SparseBooleanArray checkedItemPositions;
  private int lastCheckedPosition;

  @Before
  public void setUp() throws Exception {
    transcript = new Transcript();
    listView = new ListView(Robolectric.application);
  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void testSetSelection_ShouldFireOnItemSelectedListener() throws Exception {
    listView.setAdapter(new CountingAdapter(1));
    ShadowHandler.idleMainLooper();

    listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        transcript.add("item was selected: " + position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    listView.setSelection(0);
    ShadowHandler.idleMainLooper();
    transcript.assertEventsSoFar("item was selected: 0");
  }

  @Test
  public void addHeaderView_ShouldThrowIfAdapterIsAlreadySet() throws Exception {
    listView.setAdapter(new CountingAdapter(1));
    try {
      listView.addHeaderView(new View(Robolectric.application));
      fail();
    } catch (java.lang.IllegalStateException exception) {
      assertThat(exception.getMessage()).isEqualTo("Cannot add header view to list -- setAdapter has already been called.");
    }

    try {
      listView.addHeaderView(new View(Robolectric.application), null, false);
      fail();
    } catch (java.lang.IllegalStateException exception) {
      assertThat(exception.getMessage()).isEqualTo("Cannot add header view to list -- setAdapter has already been called.");
    }
  }

  @Test
  public void addHeaderView_ShouldRecordHeaders() throws Exception {
    View view0 = new View(Robolectric.application);
    view0.setId(0);
    View view1 = new View(Robolectric.application);
    view1.setId(1);
    View view2 = new View(Robolectric.application);
    view2.setId(2);
    View view3 = new View(Robolectric.application);
    view3.setId(3);
    listView.addHeaderView(view0);
    listView.addHeaderView(view1);
    listView.addHeaderView(view2, null, false);
    listView.addHeaderView(view3, null, false);
    listView.setAdapter(new CountingAdapter(2));
    assertThat(listView.getHeaderViewsCount()).isEqualTo(4);
    assertThat(shadowOf(listView).getHeaderViews().get(0)).isSameAs(view0);
    assertThat(shadowOf(listView).getHeaderViews().get(1)).isSameAs(view1);
    assertThat(shadowOf(listView).getHeaderViews().get(2)).isSameAs(view2);
    assertThat(shadowOf(listView).getHeaderViews().get(3)).isSameAs(view3);

    assertThat(listView.findViewById(0)).isNotNull();
    assertThat(listView.findViewById(1)).isNotNull();
    assertThat(listView.findViewById(2)).isNotNull();
    assertThat(listView.findViewById(3)).isNotNull();
  }

  @Test
  public void addHeaderView_shouldAttachTheViewToTheList() throws Exception {
    View view = new View(Robolectric.application);
    view.setId(42);

    listView.addHeaderView(view);

    assertThat(listView.findViewById(42)).isSameAs(view);
  }

  @Test @Ignore("Android doesn't behave this way, at least as of Jelly Bean.")
  public void addFooterView_ShouldThrowIfAdapterIsAlreadySet() throws Exception {
    listView.setAdapter(new CountingAdapter(1));
    try {
      listView.addFooterView(new View(Robolectric.application));
      fail();
    } catch (java.lang.IllegalStateException exception) {
      assertThat(exception.getMessage()).isEqualTo("Cannot add footer view to list -- setAdapter has already been called");

    }
  }

  @Test
  public void addFooterView_ShouldRecordFooters() throws Exception {
    View view0 = new View(Robolectric.application);
    View view1 = new View(Robolectric.application);
    listView.addFooterView(view0);
    listView.addFooterView(view1);
    listView.setAdapter(new CountingAdapter(3));
    assertThat(shadowOf(listView).getFooterViews().get(0)).isSameAs(view0);
    assertThat(shadowOf(listView).getFooterViews().get(1)).isSameAs(view1);
  }

  @Test
  public void addFooterView_shouldAttachTheViewToTheList() throws Exception {
    View view = new View(Robolectric.application);
    view.setId(42);

    listView.addFooterView(view);

    assertThat(listView.findViewById(42)).isSameAs(view);
  }

  @Test
  public void setAdapter_shouldNotClearHeaderOrFooterViews() throws Exception {
    View header = new View(Robolectric.application);
    listView.addHeaderView(header);
    View footer = new View(Robolectric.application);
    listView.addFooterView(footer);

    prepareListWithThreeItems();

    assertThat(listView.getChildCount()).isEqualTo(5);
    assertThat(listView.getChildAt(0)).isSameAs(header);
    assertThat(listView.getChildAt(4)).isSameAs(footer);
  }

  @Test
  public void testGetFooterViewsCount() throws Exception {
    listView.addHeaderView(new View(Robolectric.application));
    listView.addFooterView(new View(Robolectric.application));
    listView.addFooterView(new View(Robolectric.application));

    prepareListWithThreeItems();

    assertThat(listView.getFooterViewsCount()).isEqualTo(2);
  }

  @Test
  public void smoothScrollBy_shouldBeRecorded() throws Exception {
    listView.smoothScrollBy(42, 420);
    assertThat(shadowOf(listView).getLastSmoothScrollByDistance()).isEqualTo(42);
    assertThat(shadowOf(listView).getLastSmoothScrollByDuration()).isEqualTo(420);
  }

  @Test
  public void testPerformItemClick_ShouldFireOnItemClickListener() throws Exception {
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        transcript.add("item was clicked: " + position);
      }
    });

    listView.performItemClick(null, 0, -1);
    transcript.assertEventsSoFar("item was clicked: 0");
  }

  @Test
  public void testSetSelection_WhenNoItemSelectedListenerIsSet_ShouldDoNothing() throws Exception {
    listView.setSelection(0);
  }

  @Test
  public void findItemContainingText_shouldFindChildByString() throws Exception {
    ShadowListView shadowListView = prepareListWithThreeItems();
    View item1 = shadowListView.findItemContainingText("Item 1");
    assertThat(item1).isSameAs(listView.getChildAt(1));
  }

  @Test
  public void findItemContainingText_shouldReturnNullIfNotFound() throws Exception {
    ShadowListView shadowListView = prepareListWithThreeItems();
    assertThat(shadowListView.findItemContainingText("Non-existent item")).isNull();
  }

  @Test
  public void clickItemContainingText_shouldPerformItemClickOnList() throws Exception {
    ShadowListView shadowListView = prepareListWithThreeItems();
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        transcript.add("clicked on item " + position);
      }
    });
    shadowListView.clickFirstItemContainingText("Item 1");
    transcript.assertEventsSoFar("clicked on item 1");
  }

  @Test
  public void clickItemContainingText_shouldPerformItemClickOnList_arrayAdapter() throws Exception {
    ArrayList<String> adapterFileList = new ArrayList<String>();
    adapterFileList.add("Item 1");
    adapterFileList.add("Item 2");
    adapterFileList.add("Item 3");
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(application, android.R.layout.simple_list_item_1, adapterFileList);
    listView.setAdapter(adapter);
    shadowOf(listView).populateItems();
    ShadowListView shadowListView = shadowOf(listView);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        transcript.add("clicked on item " + adapter.getItem(position));
      }
    });
    shadowListView.clickFirstItemContainingText("Item 3");
    transcript.assertEventsSoFar("clicked on item Item 3");
  }

  @Test(expected = IllegalArgumentException.class)
  public void clickItemContainingText_shouldThrowExceptionIfNotFound() throws Exception {
    ShadowListView shadowListView = prepareListWithThreeItems();
    shadowListView.clickFirstItemContainingText("Non-existant item");
  }

  @Test @Ignore("Old safety checks, no longer supported.")
  public void revalidate_whenItemsHaveNotChanged_shouldWork() throws Exception {
    prepareWithListAdapter();
    shadowOf(listView).checkValidity();
  }

  @Test @Ignore("Old safety checks, no longer supported.")
  public void revalidate_removingAnItemWithoutInvalidating_shouldExplode() throws Exception {
    ListAdapter adapter = prepareWithListAdapter();
    adapter.items.remove(0);
    try {
      shadowOf(listView).checkValidity(); // should 'splode!
      fail("should have thrown!");
    } catch (RuntimeException e) {
      // expected
    }
  }

  @Test @Ignore("Old safety checks, no longer supported.")
  public void revalidate_addingAnItemWithoutInvalidating_shouldExplode() throws Exception {
    ListAdapter adapter = prepareWithListAdapter();
    adapter.items.add("x");
    try {
      shadowOf(listView).checkValidity(); // should 'splode!
      fail("should have thrown!");
    } catch (RuntimeException e) {
      // expected
    }
  }

  @Test @Ignore("Old safety checks, no longer supported.")
  public void revalidate_changingAnItemWithoutInvalidating_shouldExplode() throws Exception {
    ListAdapter adapter = prepareWithListAdapter();
    adapter.items.remove(2);
    adapter.items.add("x");
    try {
      shadowOf(listView).checkValidity(); // should 'splode!
      fail("should have thrown!");
    } catch (RuntimeException e) {
      // expected
    }
  }

  @Test @Ignore("Not supported as of Robolectric 2.0-alpha3.")
  public void testShouldBeAbleToTurnOffAutomaticRowUpdates() throws Exception {
    try {
      TranscriptAdapter adapter1 = new TranscriptAdapter();
      assertThat(adapter1.getCount()).isEqualTo(1);
      listView.setAdapter(adapter1);
      transcript.assertEventsSoFar("called getView");
      transcript.clear();
      adapter1.notifyDataSetChanged();
      transcript.assertEventsSoFar("called getView");

      transcript.clear();
      ShadowAdapterView.automaticallyUpdateRowViews(false);

      TranscriptAdapter adapter2 = new TranscriptAdapter();
      assertThat(adapter2.getCount()).isEqualTo(1);
      listView.setAdapter(adapter2);
      adapter2.notifyDataSetChanged();
      transcript.assertNoEventsSoFar();

    } finally {
      ShadowAdapterView.automaticallyUpdateRowViews(true);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removeAllViews_shouldThrowAnException() throws Exception {
    listView.removeAllViews();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removeView_shouldThrowAnException() throws Exception {
    listView.removeView(new View(Robolectric.application));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removeViewAt_shouldThrowAnException() throws Exception {
    listView.removeViewAt(0);
  }

  @Test
  public void getPositionForView_shouldReturnThePositionInTheListForTheView() throws Exception {
    prepareWithListAdapter();
    View childViewOfListItem = ((ViewGroup) listView.getChildAt(1)).getChildAt(0);
    assertThat(listView.getPositionForView(childViewOfListItem)).isEqualTo(1);
  }

  @Test
  public void getPositionForView_shouldReturnInvalidPositionForViewThatIsNotFound() throws Exception {
    prepareWithListAdapter();
    View view = new View(Robolectric.application);
    shadowOf(view).setMyParent(new StubViewRoot()); // Android implementation requires the item have a parent
    assertThat(listView.getPositionForView(view)).isEqualTo(AdapterView.INVALID_POSITION);
  }

  @Test @Ignore("Old safety checks, no longer supported.")
  public void revalidate_withALazyAdapterShouldWork() {
    ListAdapter lazyAdapter = new ListAdapter() {
      List<String> lazyItems = Arrays.asList("a", "b", "c");

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        if (items.isEmpty()) items.addAll(lazyItems);
        return super.getView(position, convertView, parent);
      }

      @Override
      public int getCount() {
        return lazyItems.size();
      }
    };
    listView.setAdapter(lazyAdapter);
    ShadowHandler.idleMainLooper();
    shadowOf(listView).checkValidity();
  }

  @Test
  public void shouldRecordLatestCallToSmoothScrollToPostion() throws Exception {
    listView.smoothScrollToPosition(10);
    assertThat(shadowOf(listView).getSmoothScrolledPosition()).isEqualTo(10);
  }

  @Test
  public void givenChoiceModeIsSingle_whenGettingCheckedItemPosition_thenReturnPosition() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_SINGLE).withAnyItemChecked();

    assertThat(listView.getCheckedItemPosition()).isEqualTo(checkedItemPosition);
  }

  @Test
  public void givenChoiceModeIsMultiple_whenGettingCheckedItemPosition_thenReturnInvalidPosition() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_MULTIPLE).withAnyItemChecked();

    assertThat(listView.getCheckedItemPosition()).isEqualTo(ListView.INVALID_POSITION);
  }

  @Test
  public void givenChoiceModeIsNone_whenGettingCheckedItemPosition_thenReturnInvalidPosition() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_NONE);

    assertThat(listView.getCheckedItemPosition()).isEqualTo(ListView.INVALID_POSITION);
  }

  @Test
  public void givenNoItemsChecked_whenGettingCheckedItemOisition_thenReturnInvalidPosition() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_SINGLE);

    assertThat(listView.getCheckedItemPosition()).isEqualTo(ListView.INVALID_POSITION);
  }

  @Test
  public void givenChoiceModeIsSingleAndAnItemIsChecked_whenSettingChoiceModeToNone_thenGetCheckedItemPositionShouldReturnInvalidPosition() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_SINGLE).withAnyItemChecked();

    listView.setChoiceMode(ListView.CHOICE_MODE_NONE);

    assertThat(listView.getCheckedItemPosition()).isEqualTo(ListView.INVALID_POSITION);
  }

  @Test
  public void givenChoiceModeIsMultipleAndMultipleItemsAreChecked_whenGettingCheckedItemPositions_thenReturnCheckedPositions() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_MULTIPLE).withAnyItemsChecked();

    assertThat(listView.getCheckedItemCount()).isEqualTo(checkedItemPositions.size());
    for (int i = 0; i < checkedItemPositions.size(); i++) {
      assertThat(listView.getCheckedItemPositions().get(i)).isTrue();
    }
  }

  @Test
  public void givenChoiceModeIsSingleAndMultipleItemsAreChecked_whenGettingCheckedItemPositions_thenReturnOnlyTheLastCheckedPosition() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_SINGLE).withAnyItemsChecked();

    assertThat(listView.getCheckedItemPositions().get(lastCheckedPosition)).isTrue();
    assertThat(listView.getCheckedItemCount()).isEqualTo(1);
  }

  @Test
  public void givenChoiceModeIsNoneAndMultipleItemsAreChecked_whenGettingCheckedItemPositions_thenReturnNull() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_NONE).withAnyItemsChecked();

    assertNull(listView.getCheckedItemPositions());
  }

  @Test
  public void givenItemIsNotCheckedAndChoiceModeIsSingle_whenPerformingItemClick_thenItemShouldBeChecked() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_SINGLE);
    int positionToClick = anyListIndex();

    listView.performItemClick(null, positionToClick, 0);

    assertThat(listView.getCheckedItemPosition()).isEqualTo(positionToClick);
  }

  @Test
  public void givenItemIsCheckedAndChoiceModeIsSingle_whenPerformingItemClick_thenItemShouldBeChecked() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_SINGLE).withAnyItemChecked();

    listView.performItemClick(null, checkedItemPosition, 0);

    assertThat(listView.getCheckedItemPosition()).isEqualTo(checkedItemPosition);
  }

  @Test
  public void givenItemIsNotCheckedAndChoiceModeIsMultiple_whenPerformingItemClick_thenItemShouldBeChecked() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    int positionToClick = anyListIndex();

    listView.performItemClick(null, positionToClick, 0);

    assertThat(listView.getCheckedItemPositions().get(positionToClick)).isTrue();
    assertThat(listView.getCheckedItemCount()).isEqualTo(1);
  }

  @Test
  public void givenItemIsCheckedAndChoiceModeIsMultiple_whenPerformingItemClick_thenItemShouldNotBeChecked() {
    prepareListAdapter().withChoiceMode(ListView.CHOICE_MODE_MULTIPLE).withAnyItemChecked();

    listView.performItemClick(null, checkedItemPosition, 0);

    assertFalse(listView.getCheckedItemPositions().get(checkedItemPosition));
  }

  private ListAdapterBuilder prepareListAdapter() {
    return new ListAdapterBuilder();
  }

  private ListAdapter prepareWithListAdapter() {
    ListAdapter adapter = new ListAdapter("a", "b", "c");
    listView.setAdapter(adapter);
    shadowOf(listView).populateItems();
    return adapter;
  }

  private ShadowListView prepareListWithThreeItems() {
    listView.setAdapter(new CountingAdapter(3));
    shadowOf(listView).populateItems();

    return shadowOf(listView);
  }

  private int anyListIndex() {
    return new Random().nextInt(3);
  }

  private static class ListAdapter extends BaseAdapter {
    public List<String> items = new ArrayList<String>();

    public ListAdapter(String... items) {
      this.items.addAll(asList(items));
    }

    @Override
    public int getCount() {
      return items.size();
    }

    @Override
    public Object getItem(int position) {
      return items.get(position);
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      LinearLayout linearLayout = new LinearLayout(Robolectric.application);
      linearLayout.addView(new View(Robolectric.application));
      return linearLayout;
    }
  }

  public class ListAdapterBuilder {

    public ListAdapterBuilder() {
      prepareListWithThreeItems();
    }

    public ListAdapterBuilder withChoiceMode(int choiceMode) {
      listView.setChoiceMode(choiceMode);
      return this;
    }

    public ListAdapterBuilder withAnyItemChecked() {
      checkedItemPosition = anyListIndex();
      listView.setItemChecked(checkedItemPosition, true);
      return this;
    }

    public void withAnyItemsChecked() {
      checkedItemPositions = new SparseBooleanArray();
      int numberOfSelections = anyListIndex() + 1;
      for (int i = 0; i < numberOfSelections; i++) {
        checkedItemPositions.put(i, true);
        listView.setItemChecked(i, true);
        lastCheckedPosition = i;
      }

    }
  }

  private class TranscriptAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      return 1;
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      transcript.add("called getView");
      return new View(parent.getContext());
    }
  }
}
