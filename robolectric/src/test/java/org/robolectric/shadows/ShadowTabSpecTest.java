package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class ShadowTabSpecTest {
  Drawable icon1;
  private Application context;

  @Before
  public void init() {
    icon1 = new TestIcon();
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void shouldGetAndSetTheIndicator() throws Exception {
    TabHost.TabSpec spec = new TabHost(context).newTabSpec("foo");
    View view = new View(context);
    TabHost.TabSpec self = spec.setIndicator(view);
    assertThat(self).isSameAs(spec);
    assertThat(shadowOf(spec).getIndicatorAsView()).isSameAs(view);
  }

  @Test
  public void shouldGetAndSetTheIntentContent() throws Exception {
    TabHost.TabSpec spec = new TabHost(context).newTabSpec("foo");
    Intent intent = new Intent();
    TabHost.TabSpec self = spec.setContent(intent);
    assertThat(self).isSameAs(spec);
    assertThat(shadowOf(spec).getContentAsIntent()).isSameAs(intent);
  }

  @Test
  public void shouldGetAndSetTheIndicatorLabel() throws Exception {
    TabHost.TabSpec spec =
        new TabHost(context).newTabSpec("foo").setContent(R.layout.main).setIndicator("labelText");

    assertThat(shadowOf(spec).getIndicatorLabel()).isEqualTo("labelText");
    assertThat(shadowOf(spec).getText()).isEqualTo("labelText");
  }

  @Test
  public void shouldGetAndSetTheIndicatorLabelAndIcon() throws Exception {
    TabHost.TabSpec spec =
        new TabHost(context)
            .newTabSpec("foo")
            .setContent(R.layout.main)
            .setIndicator("labelText", icon1);

    assertThat(shadowOf(spec).getIndicatorLabel()).isEqualTo("labelText");
    assertThat(shadowOf(spec).getText()).isEqualTo("labelText");
    assertThat(shadowOf(spec).getIndicatorIcon()).isSameAs(icon1);
  }

  @Test
  public void shouldSetTheContentView() throws Exception {
    TabHost.TabSpec foo =
        new TabHost(context)
            .newTabSpec("Foo")
            .setContent(
                tag -> {
                  TextView tv = new TextView(context);
                  tv.setText("The Text of " + tag);
                  return tv;
                });

    ShadowTabHost.ShadowTabSpec shadowFoo = shadowOf(foo);
    TextView textView = (TextView) shadowFoo.getContentView();


    assertThat(textView.getText().toString()).isEqualTo("The Text of Foo");
  }

  @Test
  public void shouldSetTheContentViewId() throws Exception {
    TabHost.TabSpec foo = new TabHost(context).newTabSpec("Foo").setContent(R.id.title);

    ShadowTabHost.ShadowTabSpec shadowFoo = shadowOf(foo);
    int viewId = shadowFoo.getContentViewId();

    assertThat(viewId).isEqualTo(R.id.title);
  }

  private static class TestIcon extends Drawable {

    @Override
    public void draw(Canvas canvas) {
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
      return 0;
    }

  }

}
