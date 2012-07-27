package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;
import static junit.framework.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class BaseAdapterTest {
    @Test
    public void shouldRecordNotifyDataSetChanged() throws Exception {
        BaseAdapter adapter = new TestBaseAdapter();
        adapter.notifyDataSetChanged();
        assertTrue(((ShadowBaseAdapter) shadowOf_(adapter)).notifyDataSetChangedWasCalled());
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
