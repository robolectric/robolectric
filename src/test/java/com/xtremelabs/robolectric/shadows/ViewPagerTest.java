package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;

@RunWith(WithTestDefaultsRunner.class)
public class ViewPagerTest {
    @Test
    public void shouldSetAndGetAdapter() throws Exception {
        ViewPager pager = new ViewPager(new Activity());
        TestPagerAdapter adapter = new TestPagerAdapter();

        assertNull(pager.getAdapter());

        pager.setAdapter(adapter);
        assertSame(adapter, pager.getAdapter());
    }

    private static class TestPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return false;
        }
    }
}
