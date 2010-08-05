package com.xtremelabs.droidsugar.fakes;

import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.droidsugar.matchers.TextViewHasTextMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ListViewTest {
    private Transcript transcript;
    private ListView listView;

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(AdapterView.class, FakeAdapterView.class);
        DroidSugarAndroidTestRunner.addProxy(ListView.class, FakeListView.class);
        DroidSugarAndroidTestRunner.addProxy(View.class, FakeView.class);
        DroidSugarAndroidTestRunner.addProxy(TextView.class, FakeTextView.class);

        transcript = new Transcript();
        listView = new ListView(null);
        listView.setAdapter(new ArrayAdapter<Object>(null, 0));
    }

    @Test
    public void testSetSelection_ShouldFireOnItemSelectedListener() throws Exception {
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
    public void testSetAdapter_ShouldCauseViewsToBeRendered() throws Exception {
        listView.setAdapter(new CountingAdapter(2));
        assertThat(listView.getCount(), equalTo(2));
        assertThat(listView.getChildCount(), equalTo(2));
        assertThat((TextView) listView.getChildAt(0), hasText("Item 0"));
        assertThat((TextView) listView.getChildAt(1), hasText("Item 1"));
    }

    private static class CountingAdapter extends BaseAdapter {
        private int itemCount;

        public CountingAdapter(int itemCount) {
            this.itemCount = itemCount;
        }

        @Override
        public int getCount() {
            return itemCount;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(null);
            textView.setText("Item " + position);
            return textView;
        }
    }
}
