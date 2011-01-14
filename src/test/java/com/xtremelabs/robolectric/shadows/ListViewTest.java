package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
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
    }

    @Test
    public void addHeaderView_ShouldRecordHeaders() throws Exception {
        View view0 = new View(null);
        View view1 = new View(null);
        listView.addHeaderView(view0);
        listView.addHeaderView(view1);
        assertThat(shadowOf(listView).getHeaderViews().get(0), sameInstance(view0));
        assertThat(shadowOf(listView).getHeaderViews().get(1), sameInstance(view1));
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
    public void addFooterView_ShouldRecordHeaders() throws Exception {
        View view0 = new View(null);
        View view1 = new View(null);
        listView.addFooterView(view0);
        listView.addFooterView(view1);
        assertThat(shadowOf(listView).getFooterViews().get(0), sameInstance(view0));
        assertThat(shadowOf(listView).getFooterViews().get(1), sameInstance(view1));
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

        @Override public int getCount() {
            return items.size();
        }

        @Override public Object getItem(int position) {
            return items.get(position);
        }

        @Override public long getItemId(int position) {
            return 0;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            return new View(null);
        }
    }
}
