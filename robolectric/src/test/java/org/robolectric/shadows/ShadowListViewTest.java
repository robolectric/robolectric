package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowListViewTest {

  private List<String> transcript;
  private ListView listView;
  private int checkedItemPosition;
  private SparseBooleanArray checkedItemPositions;
  private int lastCheckedPosition;
  private Application context;

  @Before
  public void setUp() throws Exception {
    transcript = new ArrayList<>();
    context = ApplicationProvider.getApplicationContext();
    listView = new ListView(context);
  }

  @Test
  public void addHeaderView_ShouldRecordHeaders() throws Exception {
    View view0 = new View(context);
    view0.setId(0);
    View view1 = new View(context);
    view1.setId(1);
    View view2 = new View(context);
    view2.setId(2);
    View view3 = new View(context);
    view3.setId(3);
    listView.addHeaderView(view0);
    listView.addHeaderView(view1);
    listView.addHeaderView(view2, null, false);
    listView.addHeaderView(view3, null, false);
    listView.setAdapter(new ShadowCountingAdapter(2));
    assertThat(listView.getHeaderViewsCount()).isEqualTo(4);
    assertThat(shadowOf(listView).getHeaderViews().get(0)).isSameAs(view0);
    assertThat(shadowOf(listView).getHeaderViews().get(1)).isSameAs(view1);
    assertThat(shadowOf(listView).getHeaderViews().get(2)).isSameAs(view2);
    assertThat(shadowOf(listView).getHeaderViews().get(3)).isSameAs(view3);

    assertThat((View) listView.findViewById(0)).isNotNull();
    assertThat((View) listView.findViewById(1)).isNotNull();
    assertThat((View) listView.findViewById(2)).isNotNull();
    assertThat((View) listView.findViewById(3)).isNotNull();
  }

  @Test
  public void addHeaderView_shouldAttachTheViewToTheList() throws Exception {
    View view = new View(context);
    view.setId(42);

    listView.addHeaderView(view);

    assertThat((View) listView.findViewById(42)).isSameAs(view);
  }

  @Test
  public void addFooterView_ShouldRecordFooters() throws Exception {
    View view0 = new View(context);
    View view1 = new View(context);
    listView.addFooterView(view0);
    listView.addFooterView(view1);
    listView.setAdapter(new ShadowCountingAdapter(3));
    assertThat(shadowOf(listView).getFooterViews().get(0)).isSameAs(view0);
    assertThat(shadowOf(listView).getFooterViews().get(1)).isSameAs(view1);
  }

  @Test
  public void addFooterView_shouldAttachTheViewToTheList() throws Exception {
    View view = new View(context);
    view.setId(42);

    listView.addFooterView(view);

    assertThat((View) listView.findViewById(42)).isSameAs(view);
  }

  @Test
  public void setAdapter_shouldNotClearHeaderOrFooterViews() throws Exception {
    View header = new View(context);
    listView.addHeaderView(header);
    View footer = new View(context);
    listView.addFooterView(footer);

    prepareListWithThreeItems();

    assertThat(listView.getChildCount()).isEqualTo(5);
    assertThat(listView.getChildAt(0)).isSameAs(header);
    assertThat(listView.getChildAt(4)).isSameAs(footer);
  }

  @Test
  public void testGetFooterViewsCount() throws Exception {
    listView.addHeaderView(new View(context));
    listView.addFooterView(new View(context));
    listView.addFooterView(new View(context));

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
    assertThat(transcript).containsExactly("item was clicked: 0");
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
    assertThat(transcript).containsExactly("clicked on item 1");
  }

  @Test
  public void clickItemContainingText_shouldPerformItemClickOnList_arrayAdapter() throws Exception {
    ArrayList<String> adapterFileList = new ArrayList<>();
    adapterFileList.add("Item 1");
    adapterFileList.add("Item 2");
    adapterFileList.add("Item 3");
    final ArrayAdapter<String> adapter = new ArrayAdapter<>(application, android.R.layout.simple_list_item_1, adapterFileList);
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
    assertThat(transcript).containsExactly("clicked on item Item 3");
  }

  @Test(expected = IllegalArgumentException.class)
  public void clickItemContainingText_shouldThrowExceptionIfNotFound() throws Exception {
    ShadowListView shadowListView = prepareListWithThreeItems();
    shadowListView.clickFirstItemContainingText("Non-existant item");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removeAllViews_shouldThrowAnException() throws Exception {
    listView.removeAllViews();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removeView_shouldThrowAnException() throws Exception {
    listView.removeView(new View(context));
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
    View view = new View(context);
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent.class)); // Android implementation requires the item have a parent
    assertThat(listView.getPositionForView(view)).isEqualTo(AdapterView.INVALID_POSITION);
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
    listView.setAdapter(new ShadowCountingAdapter(3));
    shadowOf(listView).populateItems();

    return shadowOf(listView);
  }

  private int anyListIndex() {
    return new Random().nextInt(3);
  }

  private static class ListAdapter extends BaseAdapter {
    public List<String> items = new ArrayList<>();

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
      LinearLayout linearLayout = new LinearLayout(ApplicationProvider.getApplicationContext());
      linearLayout.addView(new View(ApplicationProvider.getApplicationContext()));
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
}
