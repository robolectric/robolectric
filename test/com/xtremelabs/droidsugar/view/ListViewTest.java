package com.xtremelabs.droidsugar.view;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ListViewTest {
    private Transcript transcript;
    private ListView listView;

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(AdapterView.class, FakeAdapterView.class);
        DroidSugarAndroidTestRunner.addProxy(ListView.class, FakeListView.class);

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

}
