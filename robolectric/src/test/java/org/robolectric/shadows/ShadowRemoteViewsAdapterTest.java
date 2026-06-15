package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.util.reflector.Reflector.reflector;
import static org.robolectric.versioning.VersionCalculator.CINNAMON_BUN;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

@RunWith(AndroidJUnit4.class)
@Config(maxSdk = CINNAMON_BUN)
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
  public void getViewApi26AndLater_populatedWithExpectedItems() throws Exception {
    Class<?> callbackClass =
        Class.forName("android.widget.RemoteViewsAdapter$RemoteAdapterConnectionCallback");

    Object adapter =
        reflector(RemoteViewsAdapterReflector.class)
            .newRemoteViewsAdapter(context, createTestIntent(), mock(callbackClass), false);

    int count = reflector(RemoteViewsAdapterReflector.class, adapter).getCount();
    assertThat(count).isEqualTo(3);

    View view0 = reflector(RemoteViewsAdapterReflector.class, adapter).getView(0, null, parent);
    assertThat(((TextView) view0).getText().toString()).isEqualTo("one");

    View view1 = reflector(RemoteViewsAdapterReflector.class, adapter).getView(1, null, parent);
    assertThat(((TextView) view1).getText().toString()).isEqualTo("two");

    View view2 = reflector(RemoteViewsAdapterReflector.class, adapter).getView(2, null, parent);
    assertThat(((TextView) view2).getText().toString()).isEqualTo("three");
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void constructorApi26AndLater_intentPassedToService() {
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

  @ForType(className = "android.widget.RemoteViewsAdapter")
  private interface RemoteViewsAdapterReflector {
    @Constructor
    Object newRemoteViewsAdapter(
        Context context,
        Intent intent,
        @WithType("android.widget.RemoteViewsAdapter$RemoteAdapterConnectionCallback")
            Object callback,
        boolean useIpc);

    int getCount();

    View getView(int position, View convertView, ViewGroup parent);
  }
}
