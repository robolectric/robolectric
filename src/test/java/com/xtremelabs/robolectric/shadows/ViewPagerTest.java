package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestFragmentManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ViewPagerTest {
    private Context context;
    private ViewPager viewPager;
    private FragmentManager fragmentManager;
    private MockFragmentActivity fragmentActivity;

    @Before
    public void setup() throws Exception {
        context = new Application();
        viewPager = new ViewPager(context);
        viewPager.setId(8934);
        fragmentActivity = new MockFragmentActivity(viewPager);
        fragmentActivity.onCreate(null);
        fragmentManager = new TestFragmentManager(fragmentActivity);
    }

    @Test
    public void setAdapter_shouldCreateFirstView_ifThereIsData() throws Exception {
        OneItemPagerAdapter adapter = new OneItemPagerAdapter(fragmentManager);
        viewPager.setAdapter(adapter);

        Assert.assertSame(viewPager.getChildAt(0), adapter.createdFragment.getView());
    }

    @Test
    public void setAdapter_shouldNotBlowUp_ifThereIsNoData() throws Exception {
        PagerAdapter adapter = new MockEmptyPagerAdapter(fragmentManager);
        viewPager.setAdapter(adapter);
        //pass
    }

    @Test
    public void setAdapter_shouldInvokeTheRealPagerAdaptersRegisterDataSetObserver() throws Exception {
        ItemAddingPagerAdapter adapter = new ItemAddingPagerAdapter(fragmentManager);
        viewPager.setAdapter(adapter);
    }

    @Test
    public void shouldUpdateWhenAdapterNotifiesDataSetChanged() throws Exception {
        ItemAddingPagerAdapter adapter = new ItemAddingPagerAdapter(fragmentManager);
        viewPager.setAdapter(adapter);

        assertEquals(0, viewPager.getChildCount());
        adapter.addItem();
        adapter.notifyDataSetChanged();

        Assert.assertSame(viewPager.getChildAt(0), adapter.createdFragment.getView());
    }

    private static class MockFragmentActivity extends FragmentActivity {
        private View view;

        public MockFragmentActivity(View view) {
            this.view = view;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(view);
        }
    }

    private class OneItemPagerAdapter extends FragmentPagerAdapter {
        private Fragment createdFragment;

        public OneItemPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Fragment getItem(int position) {
            createdFragment = new MockFragment();
            return createdFragment;
        }
    }

    private class ItemAddingPagerAdapter extends FragmentPagerAdapter {
        private Fragment createdFragment;
        int itemCount = 0;

        public ItemAddingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return itemCount;
        }

        @Override
        public Fragment getItem(int position) {
            createdFragment = new MockFragment();
            return createdFragment;
        }

        public void addItem() {
            ++itemCount;
        }
    }

    private class MockEmptyPagerAdapter extends FragmentPagerAdapter {
        public MockEmptyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }
    }

    private class MockFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return new Button(context);
        }
    }
}
