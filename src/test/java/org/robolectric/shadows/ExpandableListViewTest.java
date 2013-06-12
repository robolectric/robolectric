package org.robolectric.shadows;

import android.R;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

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

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
  @Test
  public void testPerformItemClick_ShouldFireOnItemClickListener() throws Exception {
    SimpleExpandableListAdapter adapter = holyCrapYouHaveGotToBeKidding();
    expandableListView.setAdapter(adapter);
    expandableListView.setOnChildClickListener(myOnChildClickListener);
    expandableListView.expandGroup(1);
    shadowOf(expandableListView).populateItems();
    expandableListView.performItemClick(null, 0, -1); // open the group...
    expandableListView.performItemClick(null, 6, -1);
    transcript.assertEventsSoFar("item was clicked: 6");
  }

  private SimpleExpandableListAdapter holyCrapYouHaveGotToBeKidding() {
    List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
    List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
    for (int i = 0; i < 20; i++) {
      Map<String, String> curGroupMap = new HashMap<String, String>();
      groupData.add(curGroupMap);
      curGroupMap.put("NAME", "Item " + i);
      curGroupMap.put("IS_EVEN", (i % 2 == 0) ? "This group is even" : "This group is odd");

      List<Map<String, String>> children = new ArrayList<Map<String, String>>();
      for (int j = 0; j < 5; j++) {
        Map<String, String> curChildMap = new HashMap<String, String>();
        children.add(curChildMap);
        // curChildMap.put(NAME, "Child " + j);
        curChildMap.put("IS_EVEN", (j % 2 == 0) ? "Hello " + j : "Good Morning " + j);
      }
      childData.add(children);
    }

    return new SimpleExpandableListAdapter(
        Robolectric.application,
        groupData,
        R.layout.simple_expandable_list_item_1,
        new String[] {"NAME", "IS_EVEN"},
        new int[] {R.id.text1, R.id.text2},
        childData,
        R.layout.simple_expandable_list_item_2,
        new String[] {"NAME", "IS_EVEN"},
        new int[] {R.id.text1, R.id.text2}
    );
  }

  @Test
  public void shouldTolerateNullChildClickListener() throws Exception {
    expandableListView.performItemClick(null, 6, -1);
  }

  @Ignore("not yet working in 2.0, sorry :-(") // todo 2.0-cleanup
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
