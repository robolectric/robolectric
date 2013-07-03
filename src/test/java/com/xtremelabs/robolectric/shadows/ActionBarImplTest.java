package com.xtremelabs.robolectric.shadows;

import android.app.ActionBar;
import android.app.Activity;
import android.view.View;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.WithoutTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.app.ActionBar.*;
import static android.app.ActionBar.DISPLAY_USE_LOGO;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

@RunWith(WithTestDefaultsRunner.class)
public class ActionBarImplTest {

    private Activity activity;
    private ActionBar actionBar;

    @Before
    public void setUp() throws Exception {
        activity = new Activity();
        actionBar = new ActionBarImpl(activity);
    }

    @Test
    public void implementsGetSetCustomView() throws Exception {
        View customView = new View(activity);
        actionBar.setCustomView(customView);
        assertThat(actionBar.getCustomView(), sameInstance(customView));
    }

    @Test
    public void implementsGetSetDisplayOptions() throws Exception {
        int options = DISPLAY_SHOW_HOME | DISPLAY_SHOW_TITLE | DISPLAY_USE_LOGO | DISPLAY_SHOW_CUSTOM;
        actionBar.setDisplayOptions(options);
        assertThat(actionBar.getDisplayOptions(), equalTo(options));
    }

    @Test
    public void implementsGetThemedContext() throws Exception {
        assertSame(activity, actionBar.getThemedContext());
    }
}
