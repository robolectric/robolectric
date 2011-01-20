// Copyright 2010 Google Inc. All Rights Reserved.

package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.res.ResourceLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ArrayAdapter.class)
public class ShadowArrayAdapter<T> extends ShadowBaseAdapter {

  private Context context;
  private List<T> list;
  private int resource;
  private int textViewResourceId;

  public void __constructor__(Context context, int textViewResourceId) {
    init(context, 0, textViewResourceId, new ArrayList<T>());
  }

    public void __constructor__(Context context, int resource, int textViewResourceId) {
        init(context, resource, textViewResourceId, new ArrayList<T>());
    }

  public void __constructor__(Context context, int textViewResourceId, T[] objects) {
    init(context, 0, textViewResourceId, Arrays.asList(objects));
  }

    public void __constructor__(Context context, int resource, int textViewResourceId, T[] objects) {
        init(context, resource, textViewResourceId, Arrays.asList(objects));
    }

  public void __constructor__(Context context, int textViewResourceId, List<T> objects) {
    init(context, 0, textViewResourceId, objects);
  }

    public void __constructor__(Context context, int resource, int textViewResourceId, List<T> objects) {
        init(context, resource, textViewResourceId, objects);
    }

  private void init(Context context, int resource, int textViewResourceId, List<T> objects) {
    this.context = context;
    this.list = objects;
	this.resource = resource;
	this.textViewResourceId = textViewResourceId;
  }

    @Implementation
    public void add(T object) {
        list.add(object);
    }

    @Implementation
    public Context getContext() {
        return context;
    }

    @Implementation
    public int getCount() {
        return list.size();
    }

    @Implementation
    public T getItem(int position) {
        return list.get(position);
    }

  @Implementation
  public int getPosition(T item) {
    return list.indexOf(item);
  }

  @Implementation
  public View getView(int position, View convertView, ViewGroup parent) {
    T item = list.get(position);
    return getResourceLoader().inflateView(context, textViewResourceId, parent);
  }

  private ResourceLoader getResourceLoader() {
    return shadowOf(Robolectric.application).getResourceLoader();
  }
}
