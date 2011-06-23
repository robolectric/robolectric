package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.TabHost;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TabSpecTest {

    @Test
    public void shouldGetAndSetTheIndicator() throws Exception {
        TabHost.TabSpec spec = new TabHost(null).newTabSpec("foo");
        View view = new View(null);
        TabHost.TabSpec self = spec.setIndicator(view);
        assertThat(self, is(spec));
        assertThat(shadowOf(spec).getIndicatorAsView(), is(view));
    }
}
