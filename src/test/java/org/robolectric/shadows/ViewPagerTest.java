package org.robolectric.shadows;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class ViewPagerTest {

  private ViewPager pager;
  private TestPagerAdapter adapter;

  @Before
  public void setUp() throws Exception {
    pager = new ViewPager(Robolectric.application);
    adapter = new TestPagerAdapter();
  }

  @Test
  public void shouldSetAndGetAdapter() throws Exception {
    assertNull(pager.getAdapter());

    pager.setAdapter(adapter);
    assertSame(adapter, pager.getAdapter());
  }

  @Test
  public void test_getAndSetCurrentItem() throws Exception {
    pager.setAdapter(adapter);
    pager.setCurrentItem(2);
    assertEquals(2, pager.getCurrentItem());
  }

  @Test
  public void setCurrentItem_shouldInvokeListener() throws Exception {
    pager.setAdapter(adapter);
    TestOnPageChangeListener listener = new TestOnPageChangeListener();
    pager.setOnPageChangeListener(listener);
    assertFalse(listener.onPageSelectedCalled);
    pager.setCurrentItem(2);
    assertTrue(listener.onPageSelectedCalled);
  }

  @Test
  public void setCurrentItem_shouldntInvokeListenerWhenSettingRedundantly() throws Exception {
    TestOnPageChangeListener listener = new TestOnPageChangeListener();
    pager.setOnPageChangeListener(listener);
    assertFalse(listener.onPageSelectedCalled);
    pager.setCurrentItem(pager.getCurrentItem());
    assertFalse(listener.onPageSelectedCalled);
  }

  private static class TestPagerAdapter extends PagerAdapter {
    @Override
    public int getCount() {
      return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return false;
    }
  }

  private static class TestOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
    public boolean onPageSelectedCalled;

    @Override
    public void onPageSelected(int position) {
      onPageSelectedCalled = true;
    }
  }
}
