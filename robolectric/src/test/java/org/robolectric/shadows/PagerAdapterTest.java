package org.robolectric.shadows;

import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.lang.reflect.Method;

import static junit.framework.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class PagerAdapterTest {
  @Test
  public void shouldNotifyDataSetChanged() throws Exception {
    NullPagerAdapter pagerAdapter = new NullPagerAdapter();
    TestDataSetObserver datasetObserver = new TestDataSetObserver();
    Method method = PagerAdapter.class.getDeclaredMethod("registerDataSetObserver", DataSetObserver.class);
    method.setAccessible(true);
    method.invoke(pagerAdapter, datasetObserver);
    pagerAdapter.notifyDataSetChanged();

    assertTrue(datasetObserver.onChangedWasCalled);
  }

  private static class NullPagerAdapter extends PagerAdapter {
    @Override
    public int getCount() {
      return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return false;
    }


  }

  private class TestDataSetObserver extends DataSetObserver {
    boolean onChangedWasCalled;

    @Override
    public void onChanged() {
      onChangedWasCalled = true;
    }
  }
}
