package org.robolectric.shadows.support.v4;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.*;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.util.TestRunnerWithManifest;

import static junit.framework.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunnerWithManifest.class)
public class ShadowViewPagerTest {

  private ViewPager pager;
  private TestPagerAdapter adapter;

  @Before
  public void setUp() throws Exception {
    pager = new ViewPager(RuntimeEnvironment.application);
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

  @Test
  public void viewPager_withFragmentPagerAdapter_canBeCalledAsynchronously() {
    Activity activity = Robolectric.setupActivity(TestActivity.class);
    org.robolectric.Shadows.shadowOf(TestActivity.sHandler.getLooper()).runToEndOfTasks();
    assertThat(((TextView) activity.findViewById(R.id.burritos)).getText()).isEqualTo("BURRITOS");
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

  public static class TestActivity extends FragmentActivity {
    static final HandlerThread sHandlerThread = new HandlerThread("handlerThread");
    static final Handler sHandler;

    private TestFragmentPagerAdapter mAdapter;
    private ViewPager mPager;

    static {
      sHandlerThread.start();
      sHandler = new Handler(sHandlerThread.getLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_view_pager);
      mAdapter = new TestFragmentPagerAdapter(getSupportFragmentManager());
      sHandler.post(
          () ->
              runOnUiThread(
                  () -> {
                    mPager = findViewById(R.id.view_pager);
                    mPager.setAdapter(mAdapter);
                    mPager.setCurrentItem(0);
                    mPager.setOffscreenPageLimit(3);
                  }));
    }
  }

  public static class TestFragmentPagerAdapter extends FragmentPagerAdapter {
    public TestFragmentPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public int getCount() {
      return 5;
    }

    @Override
    public Fragment getItem(int position) {
      return new TestViewPagerFragment();
    }
  }

  public static class TestViewPagerFragment extends Fragment {
    public TestViewPagerFragment() {
      // required empty public constructor
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_contents, container, false);
    }
  }
}
