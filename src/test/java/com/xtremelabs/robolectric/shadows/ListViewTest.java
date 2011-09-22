package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(WithTestDefaultsRunner.class)
public class ListViewTest {
    private Transcript transcript;
    private ListView listView;

    @Before
    public void setUp() throws Exception {
        transcript = new Transcript();
        listView = new ListView(null);
    }

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
            listView.addHeaderView(new View(null));
            fail();
        } catch (java.lang.IllegalStateException exception) {
            assertThat(exception.getMessage(), equalTo("Cannot add header view to list -- setAdapter has already been called"));
        }
        
        try {
            listView.addHeaderView(new View(null), null, false );
            fail();
        } catch (java.lang.IllegalStateException exception) {
            assertThat(exception.getMessage(), equalTo("Cannot add header view to list -- setAdapter has already been called"));
        }
    }

    @Test
    public void addHeaderView_ShouldRecordHeaders() throws Exception {
        View view0 = new View(null);
        view0.setId( 0 );
        View view1 = new View(null);
        view1.setId( 1 );
        View view2 = new View(null);
        view2.setId( 2 );
        View view3 = new View(null);
        view3.setId( 3 );
        listView.addHeaderView(view0);
        listView.addHeaderView(view1);
        listView.addHeaderView( view2, null, false );
        listView.addHeaderView( view3, null, false );
        assertThat( listView.getHeaderViewsCount(), equalTo( 4 ) );
        assertThat(shadowOf(listView).getHeaderViews().get(0), sameInstance(view0));
        assertThat(shadowOf(listView).getHeaderViews().get(1), sameInstance(view1));
        assertThat(shadowOf(listView).getHeaderViews().get(2), sameInstance(view2));
        assertThat(shadowOf(listView).getHeaderViews().get(3), sameInstance(view3));

        assertThat( listView.findViewById( 0 ), notNullValue() );
        assertThat( listView.findViewById( 1 ), notNullValue() );
        assertThat( listView.findViewById( 2 ), notNullValue() );
        assertThat( listView.findViewById( 3 ), notNullValue() );
    }

    @Test
    public void addHeaderView_shouldAttachTheViewToTheList() throws Exception {
        View view = new View(null);
        view.setId(42);

        listView.addHeaderView(view);

        assertThat(listView.findViewById(42), is(view));
    }

    @Test
    public void addFooterView_ShouldThrowIfAdapterIsAlreadySet() throws Exception {
        listView.setAdapter(new CountingAdapter(1));
        try {
            listView.addFooterView(new View(null));
            fail();
        } catch (java.lang.IllegalStateException exception) {
            assertThat(exception.getMessage(), equalTo("Cannot add footer view to list -- setAdapter has already been called"));

        }
    }

    @Test
    public void addFooterView_ShouldRecordFooters() throws Exception {
        View view0 = new View(null);
        View view1 = new View(null);
        listView.addFooterView(view0);
        listView.addFooterView(view1);
        assertThat(shadowOf(listView).getFooterViews().get(0), sameInstance(view0));
        assertThat(shadowOf(listView).getFooterViews().get(1), sameInstance(view1));
    }

    @Test
    public void addFooterView_shouldAttachTheViewToTheList() throws Exception {
        View view = new View(null);
        view.setId(42);

        listView.addFooterView(view);

        assertThat(listView.findViewById(42), is(view));
    }

    @Test
    public void setAdapter_shouldNotClearHeaderOrFooterViews() throws Exception {
        View header = new View(null);
        listView.addHeaderView(header);
        View footer = new View(null);
        listView.addFooterView(footer);

        prepareListWithThreeItems();

        assertThat(listView.getChildCount(), equalTo(5));
        assertThat(listView.getChildAt(0), is(header));
        assertThat(listView.getChildAt(4), is(footer));
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
    public void shouldHaveAdapterViewCommonBehavior() throws Exception {
        AdapterViewBehavior.shouldActAsAdapterView(listView);
    }

    @Test
    public void findItemContainingText_shouldFindChildByString() throws Exception {
        ShadowListView shadowListView = prepareListWithThreeItems();
        View item1 = shadowListView.findItemContainingText("Item 1");
        assertThat(item1, sameInstance(listView.getChildAt(1)));
    }

    @Test
    public void findItemContainingText_shouldReturnNullIfNotFound() throws Exception {
        ShadowListView shadowListView = prepareListWithThreeItems();
        assertThat(shadowListView.findItemContainingText("Non-existant item"), nullValue());
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

    @Test(expected = IllegalArgumentException.class)
    public void clickItemContainingText_shouldThrowExceptionIfNotFound() throws Exception {
        ShadowListView shadowListView = prepareListWithThreeItems();
        shadowListView.clickFirstItemContainingText("Non-existant item");
    }

    @Test
    public void revalidate_whenItemsHaveNotChanged_shouldWork() throws Exception {
        prepareWithListAdapter();
        shadowOf(listView).checkValidity();
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void revalidate_removingAnItemWithoutInvalidating_shouldExplode() throws Exception {
        ListAdapter adapter = prepareWithListAdapter();
        adapter.items.remove(0);
        shadowOf(listView).checkValidity(); // should 'splode!
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void revalidate_addingAnItemWithoutInvalidating_shouldExplode() throws Exception {
        ListAdapter adapter = prepareWithListAdapter();
        adapter.items.add("x");
        shadowOf(listView).checkValidity(); // should 'splode!
    }

    @Test(expected = RuntimeException.class)
    public void revalidate_changingAnItemWithoutInvalidating_shouldExplode() throws Exception {
        ListAdapter adapter = prepareWithListAdapter();
        adapter.items.remove(2);
        adapter.items.add("x");
        shadowOf(listView).checkValidity(); // should 'splode!
    }

    @Test
    public void testShouldBeAbleToTurnOffAutomaticRowUpdates() throws Exception {
        try {
            TranscriptAdapter adapter1 = new TranscriptAdapter();
            assertThat(adapter1.getCount(), equalTo(1));
            listView.setAdapter(adapter1);
            transcript.assertEventsSoFar("called getView");
            transcript.clear();
            adapter1.notifyDataSetChanged();
            transcript.assertEventsSoFar("called getView");

            transcript.clear();
            ShadowAdapterView.automaticallyUpdateRowViews(false);

            TranscriptAdapter adapter2 = new TranscriptAdapter();
            assertThat(adapter2.getCount(), equalTo(1));
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
        listView.removeView(new View(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeViewAt_shouldThrowAnException() throws Exception {
        listView.removeViewAt(0);
    }

    @Test
    public void getPositionForView_shouldReturnThePositionInTheListForTheView() throws Exception {
        prepareWithListAdapter();
        View childViewOfListItem = ((ViewGroup) listView.getChildAt(1)).getChildAt(0);
        assertThat(listView.getPositionForView(childViewOfListItem), equalTo(1));
    }

    @Test
    public void getPositionForView_shouldReturnInvalidPostionForViewThatIsNotFound() throws Exception {
        prepareWithListAdapter();
        assertThat(listView.getPositionForView(new View(null)), equalTo(AdapterView.INVALID_POSITION));
    }

    private ListAdapter prepareWithListAdapter() {
        ListAdapter adapter = new ListAdapter("a", "b", "c");
        listView.setAdapter(adapter);
        ShadowHandler.idleMainLooper();
        return adapter;
    }

    private ShadowListView prepareListWithThreeItems() {
        listView.setAdapter(new CountingAdapter(3));
        ShadowHandler.idleMainLooper();

        return shadowOf(listView);
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

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout linearLayout = new LinearLayout(null);
            linearLayout.addView(new View(null));
            return linearLayout;
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
