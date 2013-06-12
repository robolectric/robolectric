package org.robolectric.shadows;

import android.app.ListActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ListActivityTest {

  private ListActivity listActivity;
  private FrameLayout content;
  private ListView listView;

  @Before
  public void setUp() throws Exception {
    listActivity = new ListActivity();
    listView = new ListView(listActivity);
    listView.setId(android.R.id.list);

    content = new FrameLayout(listActivity);
    content.addView(listView);

    listActivity.setContentView(content);
  }

  @Test
  public void shouldSupportSettingAndGettingListAdapter(){
    ListAdapter adapter = new CountingAdapter(5);
    listActivity.setListAdapter(adapter);

    assertThat(listActivity.getListAdapter()).isNotNull();
  }

  @Test
  public void shouldSupportOnItemClick() throws Exception {
    listActivity.setContentView(null);

    final boolean[] clicked = new boolean[1];
    ListActivity listActivity = new ListActivity() {
      @Override
      protected void onListItemClick(ListView l, View v, int position, long id) {
        clicked[0] = true;
      }
    };
    listActivity.setContentView(content);
    listActivity.setListAdapter(new CountingAdapter(5));
    Robolectric.shadowOf(listActivity.getListView()).performItemClick(0);
    assertTrue(clicked[0]);
  }

  @Test
  public void shouldSetAdapterOnListView() throws Exception {
    ListAdapter adapter = new CountingAdapter(5);
    listActivity.setListAdapter(adapter);
    assertThat(listView.getAdapter()).isSameAs(adapter);
  }

  @Test(expected = RuntimeException.class)
  public void whenNoViewWithListIdExists_shouldRaiseException(){
    ListActivity listActivity = new ListActivity();
    listActivity.setListAdapter(new CountingAdapter(5));
  }
}
