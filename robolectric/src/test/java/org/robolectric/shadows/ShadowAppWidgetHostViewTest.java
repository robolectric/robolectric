package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.SizeF;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RemoteViews;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowAppWidgetHostViewTest {
  private AppWidgetHostView appWidgetHostView;
  private ShadowAppWidgetHostView shadowAppWidgetHostView;

  @Before
  public void setUp() throws Exception {
    appWidgetHostView = new AppWidgetHostView(ApplicationProvider.getApplicationContext());
    shadowAppWidgetHostView = shadowOf(appWidgetHostView);
  }

  @Test
  public void shouldKnowItsWidgetId() {
    appWidgetHostView.setAppWidget(789, null);
    assertThat(appWidgetHostView.getAppWidgetId()).isEqualTo(789);
  }

  @Test
  public void shouldKnowItsAppWidgetProviderInfo() {
    AppWidgetProviderInfo providerInfo = new AppWidgetProviderInfo();
    appWidgetHostView.setAppWidget(0, providerInfo);
    assertThat(appWidgetHostView.getAppWidgetInfo()).isSameInstanceAs(providerInfo);
  }

  @Test
  public void shouldHaveNullHost() {
    assertThat(shadowAppWidgetHostView.getHost()).isNull();
  }

  @Test
  public void shouldBeAbleToHaveHostSet() {
    AppWidgetHost host = new AppWidgetHost(ApplicationProvider.getApplicationContext(), 0);
    shadowAppWidgetHostView.setHost(host);
    assertThat(shadowAppWidgetHostView.getHost()).isSameInstanceAs(host);
  }

  @Test
  public void shouldBeAbleToAddRemoteViews() {
    RemoteViews remoteViews =
        new RemoteViews(
            ApplicationProvider.getApplicationContext().getPackageName(), R.layout.remote_views);
    remoteViews.setTextViewText(R.id.subtitle, "Hola");
    appWidgetHostView.updateAppWidget(remoteViews);
    assertThat(((TextView) appWidgetHostView.findViewById(R.id.subtitle)).getText().toString())
        .isEqualTo("Hola");
  }

  @Test
  public void updateAppWidget_sameLayout_reappliesToExistingViews() {
    String packageName = ApplicationProvider.getApplicationContext().getPackageName();
    RemoteViews first = new RemoteViews(packageName, R.layout.remote_views);
    first.setTextViewText(R.id.subtitle, "first");
    appWidgetHostView.updateAppWidget(first);
    TextView subtitle = appWidgetHostView.findViewById(R.id.subtitle);

    RemoteViews second = new RemoteViews(packageName, R.layout.remote_views);
    second.setTextViewText(R.id.subtitle, "second");
    appWidgetHostView.updateAppWidget(second);

    assertThat((TextView) appWidgetHostView.findViewById(R.id.subtitle)).isSameInstanceAs(subtitle);
    assertThat(subtitle.getText().toString()).isEqualTo("second");
  }

  @Test
  @Config(minSdk = S)
  public void updateAppWidget_responsiveRemoteViews_appliesLayoutForSize() {
    String packageName = ApplicationProvider.getApplicationContext().getPackageName();
    RemoteViews small = new RemoteViews(packageName, R.layout.remote_views_alt);
    RemoteViews large = new RemoteViews(packageName, R.layout.remote_views);
    RemoteViews responsive =
        new RemoteViews(ImmutableMap.of(new SizeF(50, 50), small, new SizeF(300, 300), large));
    appWidgetHostView.updateAppWidget(responsive);
    int size = (int) (400 * appWidgetHostView.getResources().getDisplayMetrics().density);
    appWidgetHostView.measure(
        MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
    appWidgetHostView.layout(0, 0, size, size);

    assertThat((View) appWidgetHostView.findViewById(R.id.remote_views_root)).isNotNull();
    assertThat((View) appWidgetHostView.findViewById(R.id.remote_views_alt_root)).isNull();
  }

  @Test
  public void updateAppWidget_null_showsProvidersInitialLayout() {
    Context context = ApplicationProvider.getApplicationContext();
    AppWidgetProviderInfo info = new AppWidgetProviderInfo();
    info.provider = new ComponentName(context, "org.robolectric.Widget");
    info.initialLayout = R.layout.remote_views_alt;
    info.providerInfo = new ActivityInfo();
    info.providerInfo.packageName = context.getPackageName();
    info.providerInfo.applicationInfo = context.getApplicationInfo();
    appWidgetHostView.setAppWidget(1, info);

    appWidgetHostView.updateAppWidget(null);

    assertThat((View) appWidgetHostView.findViewById(R.id.remote_views_alt_root)).isNotNull();
  }

  @Test
  public void updateAppWidget_layoutWithoutRemoteViewsSupport_showsErrorView() {
    RemoteViews remoteViews =
        new RemoteViews(
            ApplicationProvider.getApplicationContext().getPackageName(), R.layout.main);

    appWidgetHostView.updateAppWidget(remoteViews);

    assertThat((View) appWidgetHostView.findViewById(R.id.subtitle)).isNull();
    assertThat(appWidgetHostView.getChildCount()).isEqualTo(1);
  }

  @Test
  public void updateAppWidget_clickOnViewWithPendingIntent_sendsIt() {
    Context context = ApplicationProvider.getApplicationContext();
    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_views);
    remoteViews.setOnClickPendingIntent(
        R.id.remote_view_3,
        PendingIntent.getActivity(
            context, 0, new Intent(context, Activity.class), PendingIntent.FLAG_IMMUTABLE));
    appWidgetHostView.updateAppWidget(remoteViews);

    appWidgetHostView.findViewById(R.id.remote_view_3).performClick();

    Intent started = shadowOf((Application) context).getNextStartedActivity();
    assertThat(started.getComponent().getClassName()).isEqualTo(Activity.class.getName());
  }
}
