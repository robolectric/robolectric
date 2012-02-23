package com.xtremelabs.robolectric.tester.android.view;

import android.R;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class TestWindowTest {

    @Test
    public void windowManager__shouldNotBeNull() throws Exception {
        TestWindow window = new TestWindow(null);
        Assert.assertNotNull(window.getWindowManager());
    }

    @Test
    public void decorViewFindViewById__shouldReturnContentWrapper() throws Exception {
        TestWindow window = new TestWindow(null);
        View contentView = new View(null);
        contentView.setTag("content view");
        window.setContentView(contentView);

        // This is the real meat of the test. ActionBarSherlock relies on this code:
        //   window.getDecorView().findViewById(R.id.content)
        ViewGroup contentWrapper = (ViewGroup) window.getDecorView().findViewById(R.id.content);
        assertThat("child count", contentWrapper.getChildCount(), equalTo(1));
        assertThat(contentWrapper.getChildAt(0).getTag(), equalTo(contentView.getTag()));
    }
}
