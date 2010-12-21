package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.ExpandableListView;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ExpandableListViewTest {

    private Transcript transcript;
    private ExpandableListView expandableListView;
    private MyOnChildClickListener myOnChildClickListener;

    @Before
    public void setUp() {
        expandableListView = new ExpandableListView(null);
        transcript = new Transcript();
        myOnChildClickListener = new MyOnChildClickListener();
    }

    @Test
    public void testPerformItemClick_ShouldFireOnItemClickListener() throws Exception {
        expandableListView.setOnChildClickListener(myOnChildClickListener);
        expandableListView.performItemClick(null, 6, -1);
        transcript.assertEventsSoFar("item was clicked: 6");
    }

    @Test
    public void shouldTolerateNullChildClickListener() throws Exception {
        expandableListView.performItemClick(null, 6, -1);
    }

    @Test
    public void shouldPassTheViewToTheClickListener() throws Exception {
        expandableListView.setOnChildClickListener(myOnChildClickListener);
        expandableListView.performItemClick(null, 6, -1);
        assertThat(myOnChildClickListener.expandableListView, sameInstance(expandableListView));
    }

    private class MyOnChildClickListener implements ExpandableListView.OnChildClickListener {
        ExpandableListView expandableListView;

        @Override
        public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int position, long l) {
            this.expandableListView = expandableListView;
            transcript.add("item was clicked: " + position);
            return true;
        }
    }
}
