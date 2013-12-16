package org.robolectric.shadows;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class RemoteViewsTest {
  private final String packageName = Robolectric.application.getPackageName();
  private final Activity activity = buildActivity(Activity.class).create().get();

  @Test
  public void apply_shouldInflateLayout() {
    RemoteViews views = new RemoteViews(packageName, R.layout.remote_views);
    assertThat(views.apply(activity, null)).isNotNull();
  }

  @Test
  public void setTextViewText_shouldUpdateView() {
    RemoteViews views = new RemoteViews(packageName, R.layout.remote_views);
    views.setTextViewText(R.id.remote_view_1, "Foo");

    View layout = views.apply(activity, null);
    TextView text = (TextView) layout.findViewById(R.id.remote_view_1);
    assertThat(text.getText().toString()).isEqualTo("Foo");
  }

  @Test
  public void setImageViewBitmap_shouldUpdateView() {
    Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    RemoteViews views = new RemoteViews(packageName, R.layout.remote_views);
    views.setImageViewBitmap(R.id.remote_view_2, bitmap);

    View layout = views.apply(activity, null);
    ImageView image = (ImageView) layout.findViewById(R.id.remote_view_2);
    assertThat(shadowOf(image).getImageBitmap()).isEqualTo(bitmap);
  }

  @Test
  public void setViewVisibility_shouldUpdateView() {
    RemoteViews views = new RemoteViews(packageName, R.layout.remote_views);
    views.setViewVisibility(R.id.remote_view_1, View.INVISIBLE);

    View layout = views.apply(activity, null);
    assertThat(layout.findViewById(R.id.remote_view_1).getVisibility()).isEqualTo(View.INVISIBLE);
  }

  @Test
  public void setOnClickPendingIntent_shouldFireSuppliedIntent() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    PendingIntent pendingIntent = PendingIntent.getActivity(Robolectric.application, 0, intent, 0);

    RemoteViews views = new RemoteViews(packageName, R.layout.remote_views);
    views.setOnClickPendingIntent(R.id.remote_view_3, pendingIntent);

    View layout = views.apply(activity, null);
    layout.findViewById(R.id.remote_view_3).performClick();
    assertThat(shadowOf(Robolectric.application).getNextStartedActivity()).isEqualTo(intent);
  }
}
