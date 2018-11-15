package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.app.TabActivity;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowTabHostTest {

  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void newTabSpec_shouldMakeATabSpec() throws Exception {
    TabHost tabHost = new TabHost(context);
    TabHost.TabSpec tabSpec = tabHost.newTabSpec("Foo");
    assertThat(tabSpec.getTag()).isEqualTo("Foo");
  }

  @Test
  public void shouldAddTabsToLayoutWhenAddedToHost() {
    TabHost tabHost = new TabHost(context);

    View fooView = new View(context);
    TabHost.TabSpec foo = tabHost.newTabSpec("Foo").setIndicator(fooView);

    View barView = new View(context);
    TabHost.TabSpec bar = tabHost.newTabSpec("Bar").setIndicator(barView);

    tabHost.addTab(foo);
    tabHost.addTab(bar);

    assertThat(tabHost.getChildAt(0)).isSameAs(fooView);
    assertThat(tabHost.getChildAt(1)).isSameAs(barView);
  }

  @Test
  public void shouldReturnTabSpecsByTag() throws Exception {
    TabHost tabHost = new TabHost(context);
    TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
    TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
    TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

    tabHost.addTab(foo);
    tabHost.addTab(bar);
    tabHost.addTab(baz);

    assertThat(shadowOf(tabHost).getSpecByTag("Bar")).isSameAs(bar);
    assertThat(shadowOf(tabHost).getSpecByTag("Baz")).isSameAs(baz);
    assertNull(shadowOf(tabHost).getSpecByTag("Whammie"));
  }

  @Test
  public void shouldFireTheTabChangeListenerWhenCurrentTabIsSet() throws Exception {
    TabHost tabHost = new TabHost(context);

    TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
    TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
    TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

    tabHost.addTab(foo);
    tabHost.addTab(bar);
    tabHost.addTab(baz);

    TestOnTabChangeListener listener = new TestOnTabChangeListener();
    tabHost.setOnTabChangedListener(listener);

    tabHost.setCurrentTab(2);

    assertThat(listener.tag).isEqualTo("Baz");
  }

  @Test
  public void shouldFireTheTabChangeListenerWhenTheCurrentTabIsSetByTag() throws Exception {
    TabHost tabHost = new TabHost(context);

    TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
    TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
    TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

    tabHost.addTab(foo);
    tabHost.addTab(bar);
    tabHost.addTab(baz);

    TestOnTabChangeListener listener = new TestOnTabChangeListener();
    tabHost.setOnTabChangedListener(listener);

    tabHost.setCurrentTabByTag("Bar");

    assertThat(listener.tag).isEqualTo("Bar");
  }

  @Test
  public void shouldRetrieveTheCurrentViewFromTabContentFactory() {
    TabHost tabHost = new TabHost(context);

    TabHost.TabSpec foo =
        tabHost
            .newTabSpec("Foo")
            .setContent(
                tag -> {
                  TextView tv = new TextView(context);
                  tv.setText("The Text of " + tag);
                  return tv;
                });

    tabHost.addTab(foo);
    tabHost.setCurrentTabByTag("Foo");
    TextView textView = (TextView) tabHost.getCurrentView();

    assertThat(textView.getText().toString()).isEqualTo("The Text of Foo");
  }
  @Test
  public void shouldRetrieveTheCurrentViewFromViewId() {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    activity.setContentView(org.robolectric.R.layout.main);
    TabHost tabHost = new TabHost(activity);
    TabHost.TabSpec foo = tabHost.newTabSpec("Foo")
    .setContent(org.robolectric.R.id.title);

     tabHost.addTab(foo);
     tabHost.setCurrentTabByTag("Foo");
     TextView textView = (TextView) tabHost.getCurrentView();

    assertThat(textView.getText().toString()).isEqualTo("Main Layout");
  }

  private static class TestOnTabChangeListener implements TabHost.OnTabChangeListener {
    private String tag;

    @Override
    public void onTabChanged(String tag) {
      this.tag = tag;
    }
  }

  @Test
  public void canGetCurrentTabTag() throws Exception {
    TabHost tabHost = new TabHost(context);

    TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
    TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
    TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

    tabHost.addTab(foo);
    tabHost.addTab(bar);
    tabHost.addTab(baz);

    tabHost.setCurrentTabByTag("Bar");

    assertThat(tabHost.getCurrentTabTag()).isEqualTo("Bar");
  }

  @Test
  public void canGetCurrentTab() throws Exception {
    TabHost tabHost = new TabHost(context);

    TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
    TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
    TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

    tabHost.addTab(foo);
    tabHost.addTab(bar);
    tabHost.addTab(baz);
    assertThat(shadowOf(tabHost).getCurrentTabSpec()).isEqualTo(foo);
    assertThat(tabHost.getCurrentTab()).isEqualTo(0);
    tabHost.setCurrentTabByTag("Bar");
    assertThat(tabHost.getCurrentTab()).isEqualTo(1);
    assertThat(shadowOf(tabHost).getCurrentTabSpec()).isEqualTo(bar);
    tabHost.setCurrentTabByTag("Foo");
    assertThat(tabHost.getCurrentTab()).isEqualTo(0);
    assertThat(shadowOf(tabHost).getCurrentTabSpec()).isEqualTo(foo);
    tabHost.setCurrentTabByTag("Baz");
    assertThat(tabHost.getCurrentTab()).isEqualTo(2);
    assertThat(shadowOf(tabHost).getCurrentTabSpec()).isEqualTo(baz);
  }

  @Test
  public void setCurrentTabByTagShouldAcceptNullAsParameter() throws Exception {
    TabHost tabHost = new TabHost(context);
    TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
    tabHost.addTab(foo);

    tabHost.setCurrentTabByTag(null);
    assertThat(tabHost.getCurrentTabTag()).isEqualTo("Foo");
  }

  @Test
  public void shouldGetTabWidget() throws Exception {
    TabActivity activity = Robolectric.buildActivity(TabActivity.class).create().get();
    activity.setContentView(R.layout.tab_activity);
    TabHost host = new TabHost(activity);
    assertThat(host.getTabWidget()).isInstanceOf(TabWidget.class);
  }
}
