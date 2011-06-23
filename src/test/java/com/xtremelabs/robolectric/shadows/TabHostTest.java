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

}
