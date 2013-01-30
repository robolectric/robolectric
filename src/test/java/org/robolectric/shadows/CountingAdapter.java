package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class CountingAdapter extends BaseAdapter {
    private int itemCount;

    public CountingAdapter(int itemCount) {
        this.itemCount = itemCount;
    }
    
    public void setCount(int itemCount) {
    	this.itemCount = itemCount;
    	notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemCount;
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
        TextView textView = new TextView(null);
        textView.setText("Item " + position);
        return textView;
    }
}
