package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

    // As on a device, each row comes wrapped in a frame.
    View view0 = reflector(RemoteViewsAdapterReflector.class, adapter).getView(0, null, parent);
    assertThat(rowText(view0)).isEqualTo("one");

    View view1 = reflector(RemoteViewsAdapterReflector.class, adapter).getView(1, null, parent);
    assertThat(rowText(view1)).isEqualTo("two");

    View view2 = reflector(RemoteViewsAdapterReflector.class, adapter).getView(2, null, parent);
    assertThat(rowText(view2)).isEqualTo("three");
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void constructorApi26AndLater_intentPassedToService() {
    assertThat(capturedIntent.getComponent().getClassName())
        .isEqualTo(TestRemoteViewsService.class.getName());
    assertThat(capturedIntent.getStringExtra(TEST_EXTRA_KEY)).isEqualTo(TEST_EXTRA_VALUE);
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void getCount_factoryLoadsInOnDataSetChanged_hasItems() throws Exception {
    LoadingRemoteViewsFactory.source = LIST_ITEMS;
    Object adapter = newAdapter(LoadingRemoteViewsService.class);

    assertThat(reflector(RemoteViewsAdapterReflector.class, adapter).getCount()).isEqualTo(3);
    assertThat(LoadingRemoteViewsFactory.dataSetChangedCalls).isEqualTo(1);
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void notifyDataSetChanged_reloadsFactoryAndNotifiesObservers() throws Exception {
    LoadingRemoteViewsFactory.source = LIST_ITEMS;
    BaseAdapter adapter = (BaseAdapter) newAdapter(LoadingRemoteViewsService.class);
    assertThat(adapter.getCount()).isEqualTo(3);
    AtomicInteger changes = new AtomicInteger();
    adapter.registerDataSetObserver(
        new DataSetObserver() {
          @Override
          public void onChanged() {
            changes.incrementAndGet();
          }
        });

    LoadingRemoteViewsFactory.source = ImmutableList.of("four");
    adapter.notifyDataSetChanged();

    assertThat(LoadingRemoteViewsFactory.dataSetChangedCalls).isEqualTo(2);
    assertThat(changes.get()).isEqualTo(1);
    assertThat(adapter.getCount()).isEqualTo(1);
  }

  private Object newAdapter(Class<? extends RemoteViewsService> service) throws Exception {
    Class<?> callbackClass =
        Class.forName("android.widget.RemoteViewsAdapter$RemoteAdapterConnectionCallback");
    return reflector(RemoteViewsAdapterReflector.class)
        .newRemoteViewsAdapter(context, new Intent(context, service), mock(callbackClass), false);
  }

  /** A service whose factory loads its items in {@code onDataSetChanged}, as apps typically do. */
  private static class LoadingRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
      LoadingRemoteViewsFactory.dataSetChangedCalls = 0;
      return new LoadingRemoteViewsFactory();
    }
  }

  private static class LoadingRemoteViewsFactory extends TestRemoteViewsFactory {
    @SuppressWarnings("NonFinalStaticField")
    static List<String> source = ImmutableList.of();

    @SuppressWarnings("NonFinalStaticField")
    static int dataSetChangedCalls;

    private List<String> items = ImmutableList.of();

    @Override
    public void onDataSetChanged() {
      dataSetChangedCalls++;
      items = source;
    }

    @Override
    public int getCount() {
      return items.size();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void getView_convertView_reappliesRowOfSameLayout() throws Exception {
    Object adapter = newAdapter(TestRemoteViewsService.class);
    View first = reflector(RemoteViewsAdapterReflector.class, adapter).getView(0, null, parent);
    View row = ((ViewGroup) first).getChildAt(0);

    View second = reflector(RemoteViewsAdapterReflector.class, adapter).getView(1, first, parent);

    assertThat(second).isSameInstanceAs(first);
    assertThat(((ViewGroup) second).getChildAt(0)).isSameInstanceAs(row);
    assertThat(rowText(second)).isEqualTo("two");
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void itemClick_fillInIntent_sendsPendingIntentTemplate() {
    RemoteViews widget = new RemoteViews(context.getPackageName(), android.R.layout.list_content);
    widget.setRemoteAdapter(android.R.id.list, new Intent(context, FillInRemoteViewsService.class));
    widget.setPendingIntentTemplate(
        android.R.id.list,
        PendingIntent.getActivity(
            context,
            0,
            new Intent(context, Activity.class),
            PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
    // setRemoteAdapter only applies inside a widget host.
    View root = widget.apply(context, new AppWidgetHostView(context));
    ShadowLooper.idleMainLooper();
    ListView list = root.findViewById(android.R.id.list);
    assertThat(list.getAdapter().getCount()).isEqualTo(3);
    list.measure(
        MeasureSpec.makeMeasureSpec(200, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(400, MeasureSpec.EXACTLY));
    list.layout(0, 0, 200, 400);
    View item = list.getChildAt(1);

    list.performItemClick(item, 1, list.getAdapter().getItemId(1));

    Intent started = shadowOf((Application) context).getNextStartedActivity();
    assertThat(started.getComponent().getClassName()).isEqualTo(Activity.class.getName());
    assertThat(started.getStringExtra("item")).isEqualTo("two");
  }

  private static String rowText(View row) {
    return ((TextView) ((ViewGroup) row).getChildAt(0)).getText().toString();
  }

  /** Rows whose text carries a fill-in intent, for the collection's pending intent template. */
  private static class FillInRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
      return new TestRemoteViewsFactory() {
        @Override
        public RemoteViews getViewAt(int position) {
          RemoteViews row = super.getViewAt(position);
          row.setOnClickFillInIntent(
              android.R.id.text1, new Intent().putExtra("item", LIST_ITEMS.get(position)));
          return row;
        }
      };
    }
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
