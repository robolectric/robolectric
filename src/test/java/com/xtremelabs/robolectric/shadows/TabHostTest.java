package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.TabActivity;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TabHostTest {

    @Test
    public void newTabSpec_shouldMakeATabSpec() throws Exception {
        TabHost tabHost = new TabHost(null);
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Foo");
        assertThat(tabSpec.getTag(), equalTo("Foo"));
    }

    @Test
    public void shouldAddTabsToLayoutWhenAddedToHost() {
        TabHost tabHost = new TabHost(null);

        View fooView = new View(null);
        TabHost.TabSpec foo = tabHost.newTabSpec("Foo").setIndicator(fooView);

        View barView = new View(null);
        TabHost.TabSpec bar = tabHost.newTabSpec("Bar").setIndicator(barView);

        tabHost.addTab(foo);
        tabHost.addTab(bar);

        assertThat(tabHost.getChildAt(0), is(fooView));
        assertThat(tabHost.getChildAt(1), is(barView));
    }

    @Test
    public void shouldReturnTabSpecsByTag() throws Exception {
        TabHost tabHost = new TabHost(null);
        TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
        TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
        TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

        tabHost.addTab(foo);
        tabHost.addTab(bar);
        tabHost.addTab(baz);

        assertThat(shadowOf(tabHost).getSpecByTag("Bar"), is(bar));
        assertThat(shadowOf(tabHost).getSpecByTag("Baz"), is(baz));
        assertNull(shadowOf(tabHost).getSpecByTag("Whammie"));
    }

    @Test
    public void shouldFireTheTabChangeListenerWhenCurrentTabIsSet() throws Exception {
        TabHost tabHost = new TabHost(null);

        TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
        TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
        TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

        tabHost.addTab(foo);
        tabHost.addTab(bar);
        tabHost.addTab(baz);

        TestOnTabChangeListener listener = new TestOnTabChangeListener();
        tabHost.setOnTabChangedListener(listener);

        tabHost.setCurrentTab(2);

        assertThat(listener.tag, equalTo("Baz"));
    }

    @Test
    public void shouldFireTheTabChangeListenerWhenTheCurrentTabIsSetByTag() throws Exception {
        TabHost tabHost = new TabHost(null);

        TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
        TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
        TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

        tabHost.addTab(foo);
        tabHost.addTab(bar);
        tabHost.addTab(baz);

        TestOnTabChangeListener listener = new TestOnTabChangeListener();
        tabHost.setOnTabChangedListener(listener);

        tabHost.setCurrentTabByTag("Bar");

        assertThat(listener.tag, equalTo("Bar"));
    }

    @Test
    public void shouldRetrieveTheCurrentViewFromTabContentFactory() {
    	TabHost tabHost = new TabHost(null);

        TabHost.TabSpec foo = tabHost.newTabSpec("Foo").setContent(
		new TabContentFactory() {
			public View createTabContent(String tag) {
				TextView tv = new TextView(null);
				tv.setText("The Text of " + tag);
				return tv;
			}
		});

        tabHost.addTab(foo);
        tabHost.setCurrentTabByTag("Foo");
        TextView textView = (TextView) tabHost.getCurrentView();

        assertThat(textView.getText().toString(), equalTo("The Text of Foo"));
    }
    @Test
    public void shouldRetrieveTheCurrentViewFromViewId() {
    	Activity a = new Activity();
    	a.setContentView(com.xtremelabs.robolectric.R.layout.main);
    	TabHost tabHost = new TabHost(a);
    	TabHost.TabSpec foo = tabHost.newTabSpec("Foo")
    	.setContent(com.xtremelabs.robolectric.R.id.title);

    	 tabHost.addTab(foo);
         tabHost.setCurrentTabByTag("Foo");
         TextView textView = (TextView) tabHost.getCurrentView();

         assertThat(textView.getText().toString(), equalTo("Main Layout"));
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
        TabHost tabHost = new TabHost(null);

        TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
        TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
        TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

        tabHost.addTab(foo);
        tabHost.addTab(bar);
        tabHost.addTab(baz);

        tabHost.setCurrentTabByTag("Bar");

        assertThat(tabHost.getCurrentTabTag(), equalTo("Bar"));
    }

    @Test
    public void canGetCurrentTab() throws Exception {
        TabHost tabHost = new TabHost(null);

        TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
        TabHost.TabSpec bar = tabHost.newTabSpec("Bar");
        TabHost.TabSpec baz = tabHost.newTabSpec("Baz");

        tabHost.addTab(foo);
        tabHost.addTab(bar);
        tabHost.addTab(baz);
        assertThat(shadowOf(tabHost).getCurrentTabSpec(), equalTo(foo));
        assertThat(tabHost.getCurrentTab(), equalTo(0));
        tabHost.setCurrentTabByTag("Bar");
        assertThat(tabHost.getCurrentTab(), equalTo(1));
        assertThat(shadowOf(tabHost).getCurrentTabSpec(), equalTo(bar));
        tabHost.setCurrentTabByTag("Foo");
        assertThat(tabHost.getCurrentTab(), equalTo(0));
        assertThat(shadowOf(tabHost).getCurrentTabSpec(), equalTo(foo));
        tabHost.setCurrentTabByTag("Baz");
        assertThat(tabHost.getCurrentTab(), equalTo(2));
        assertThat(shadowOf(tabHost).getCurrentTabSpec(), equalTo(baz));
    }

    @Test
    public void setCurrentTabByTagShouldAcceptNullAsParameter() throws Exception {
        TabHost tabHost = new TabHost(null);
        TabHost.TabSpec foo = tabHost.newTabSpec("Foo");
        tabHost.addTab(foo);

        tabHost.setCurrentTabByTag(null);
        assertThat(tabHost.getCurrentTabTag(), equalTo("Foo"));
    }

    @Test
    public void shouldGetTabWidget() throws Exception {
        TabActivity activity = new TabActivity();
        activity.setContentView(R.layout.tab_activity);
        TabHost host = new TabHost(activity);
        assertThat(host.getTabWidget(), instanceOf(TabWidget.class));
    }
}
