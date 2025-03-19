package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class ShadowRemoteViewsTest {

  private static final int LAYOUT_ID = R.layout.remote_views;
  private static final ImmutableList<String> LIST_ITEMS = ImmutableList.of("one", "two", "three");
  private static final String TEST_EXTRA_KEY = "test_extra_key";
  private static final String TEST_EXTRA_VALUE = "test_extra_value";

  @SuppressWarnings("NonFinalStaticField")
  private static Intent capturedIntent = null;

  private Context context;
  private RemoteViews remoteViews;
  private ViewGroup parent;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    parent = new FrameLayout(context);
    remoteViews = new RemoteViews(context.getPackageName(), LAYOUT_ID);
    capturedIntent = null;
  }

  @Test
  public void setRemoteAdapter_populatesList() {
    remoteViews.setRemoteAdapter(R.id.list_view, createTestIntent());
    View view = remoteViews.apply(context, parent);
    remoteViews.reapply(context, view);

    ListView listView = view.findViewById(R.id.list_view);
    ListAdapter adapter = listView.getAdapter();
    assertThat(adapter).isNotNull();
    assertThat(adapter.getCount()).isEqualTo(LIST_ITEMS.size());
    assertThat(((TextView) adapter.getView(0, null, listView)).getText().toString())
        .isEqualTo(LIST_ITEMS.get(0));
    assertThat(((TextView) adapter.getView(1, null, listView)).getText().toString())
        .isEqualTo(LIST_ITEMS.get(1));
    assertThat(((TextView) adapter.getView(2, null, listView)).getText().toString())
        .isEqualTo(LIST_ITEMS.get(2));
  }

  @Test
  public void setRemoteAdapter_intentPassedToService() {
    Intent intent = createTestIntent();
    remoteViews.setRemoteAdapter(R.id.list_view, intent);
    View view = remoteViews.apply(context, parent);
    remoteViews.reapply(context, view);

    assertThat(capturedIntent.getComponent().getClassName())
        .isEqualTo(TestRemoteViewsService.class.getName());
    assertThat(capturedIntent.getStringExtra(TEST_EXTRA_KEY)).isEqualTo(TEST_EXTRA_VALUE);
  }

  private Intent createTestIntent() {
    return new Intent(context, TestRemoteViewsService.class)
        .putExtra(TEST_EXTRA_KEY, TEST_EXTRA_VALUE);
  }

  private static class TestRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
      capturedIntent = intent;
      return new TestRemoteViewsFactory();
    }
  }

  private static class TestRemoteViewsFactory implements RemoteViewsFactory {

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() {}

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
      return LIST_ITEMS.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
      RemoteViews remoteViews =
          new RemoteViews(
              ApplicationProvider.getApplicationContext().getPackageName(),
              R.layout.remote_views_list_item);
      remoteViews.setTextViewText(R.id.text_view, LIST_ITEMS.get(position));
      return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
      return null;
    }

    @Override
    public int getViewTypeCount() {
      return 1;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }
  }
}
