package org.robolectric.shadows;

import android.view.View;
import android.widget.ExpandableListView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ExpandableListViewTest {

    private Transcript transcript;
    private ExpandableListView expandableListView;
    private MyOnChildClickListener myOnChildClickListener;

    @Before
    public void setUp() {
        expandableListView = new ExpandableListView(Robolectric.application);
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
        assertThat(myOnChildClickListener.expandableListView).isSameAs(expandableListView);
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
