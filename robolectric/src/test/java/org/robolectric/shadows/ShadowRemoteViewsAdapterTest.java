package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.RemoteViewsAdapter;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowRemoteViewsAdapterTest {

  private static final ImmutableList<String> LIST_ITEMS = ImmutableList.of("one", "two", "three");
  private static final String TEST_EXTRA_KEY = "test_extra_key";
  private static final String TEST_EXTRA_VALUE = "test_extra_value";

  private Context context;
  private ViewGroup parent;

  @SuppressWarnings("NonFinalStaticField")
  private static Intent capturedIntent = null;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    parent = new FrameLayout(context);
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void getViewApi26AndLater_populatedWithExpectedItems() {
    RemoteViewsAdapter adapter = new RemoteViewsAdapter(context, createTestIntent(), mock(), false);

    assertThat(adapter.getCount()).isEqualTo(3);
    assertThat(((TextView) adapter.getView(0, null, parent)).getText().toString()).isEqualTo("one");
    assertThat(((TextView) adapter.getView(1, null, parent)).getText().toString()).isEqualTo("two");
    assertThat(((TextView) adapter.getView(2, null, parent)).getText().toString())
        .isEqualTo("three");
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void constructorApi26AndLater_intentPassedToService() {
    RemoteViewsAdapter unused = new RemoteViewsAdapter(context, createTestIntent(), mock(), false);

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
              android.R.layout.simple_list_item_1);
      remoteViews.setTextViewText(android.R.id.text1, LIST_ITEMS.get(position));
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
