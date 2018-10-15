package org.robolectric.shadows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowBaseAdapterTest {
  @Test
  public void shouldRecordNotifyDataSetChanged() throws Exception {
    BaseAdapter adapter = new TestBaseAdapter();
    adapter.notifyDataSetChanged();
    assertTrue(shadowOf(adapter).wasNotifyDataSetChangedCalled());
  }

  @Test
  public void canResetNotifyDataSetChangedFlag() throws Exception {
    BaseAdapter adapter = new TestBaseAdapter();
    adapter.notifyDataSetChanged();
    shadowOf(adapter).clearWasDataSetChangedCalledFlag();
    assertFalse(shadowOf(adapter).wasNotifyDataSetChangedCalled());
  }

  private static class TestBaseAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      return 0;
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return null;
    }
  }
}
