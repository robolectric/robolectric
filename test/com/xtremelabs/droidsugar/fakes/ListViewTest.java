package com.xtremelabs.droidsugar.fakes;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.util.TestUtil;
import com.xtremelabs.droidsugar.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner.proxyFor;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ListViewTest {
    private Transcript transcript;
    private ListView listView;

    @Before
    public void setUp() throws Exception {
        TestUtil.addAllProxies();

        transcript = new Transcript();
        listView = new ListView(null);
    }

    @Test
    public void testSetSelection_ShouldFireOnItemSelectedListener() throws Exception {
        listView.setAdapter(new CountingAdapter(1));
        FakeHandler.flush();

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
        FakeHandler.flush();
        transcript.assertEventsSoFar("item was selected: 0");
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
        FakeListView fakeListView = prepareListWithThreeItems();
        View item1 = fakeListView.findItemContainingText("Item 1");
        assertThat(item1, sameInstance(listView.getChildAt(1)));
    }

    @Test
    public void findItemContainingText_shouldReturnNullIfNotFound() throws Exception {
        FakeListView fakeListView = prepareListWithThreeItems();
        assertThat(fakeListView.findItemContainingText("Non-existant item"), nullValue());
    }

    @Test
    public void clickItemContainingText_shouldPerformItemClickOnList() throws Exception {
        FakeListView fakeListView = prepareListWithThreeItems();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                transcript.add("clicked on item " + position);
            }
        });
        fakeListView.clickFirstItemContainingText("Item 1");
        transcript.assertEventsSoFar("clicked on item 1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void clickItemContainingText_shouldThrowExceptionIfNotFound() throws Exception {
        FakeListView fakeListView = prepareListWithThreeItems();
        fakeListView.clickFirstItemContainingText("Non-existant item");
    }

    @Test
    public void revalidate_whenItemsHaveNotChanged_shouldWork() throws Exception {
        prepareWithListAdapter();
        ((FakeAdapterView) proxyFor(listView)).checkValidity();
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void revalidate_removingAnItemWithoutInvalidating_shouldExplode() throws Exception {
        ListAdapter adapter = prepareWithListAdapter();
        adapter.items.remove(0);
        ((FakeAdapterView) proxyFor(listView)).checkValidity(); // should 'splode!
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void revalidate_addingAnItemWithoutInvalidating_shouldExplode() throws Exception {
        ListAdapter adapter = prepareWithListAdapter();
        adapter.items.add("x");
        ((FakeAdapterView) proxyFor(listView)).checkValidity(); // should 'splode!
    }

    @Test(expected = RuntimeException.class)
    public void revalidate_changingAnItemWithoutInvalidating_shouldExplode() throws Exception {
        ListAdapter adapter = prepareWithListAdapter();
        adapter.items.remove(2);
        adapter.items.add("x");
        ((FakeAdapterView) proxyFor(listView)).checkValidity(); // should 'splode!
    }

    private ListAdapter prepareWithListAdapter() {
        ListAdapter adapter = new ListAdapter("a", "b", "c");
        listView.setAdapter(adapter);
        FakeHandler.flush();
        return adapter;
    }

    private FakeListView prepareListWithThreeItems() {
        listView.setAdapter(new CountingAdapter(3));
        FakeHandler.flush();

        return (FakeListView) proxyFor(listView);
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
