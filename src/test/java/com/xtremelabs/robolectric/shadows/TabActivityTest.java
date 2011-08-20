package com.xtremelabs.robolectric.shadows;

import android.app.TabActivity;
import android.widget.TabHost;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TabActivityTest {

    @Test
    public void tabActivityShouldNotMakeNewTabHostEveryGet() throws Exception {
        TabActivity activity = new TabActivity();
        TabHost tabHost1 = activity.getTabHost();
        TabHost tabHost2 = activity.getTabHost();
        
        assertThat(tabHost1, equalTo(tabHost2));
    }
}
