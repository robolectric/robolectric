package com.xtremelabs.robolectric.shadows;

import android.widget.TabHost;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.IsEqual.equalTo;
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

    private static class TestOnTabChangeListener implements TabHost.OnTabChangeListener {
        private String tag;

        @Override
        public void onTabChanged(String tag) {
            this.tag = tag;
        }
    }
}
