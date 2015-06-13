package org.robolectric.shadows;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.widget.ResourceCursorAdapter}.
 */
@Implements(ResourceCursorAdapter.class)
public class ShadowResourceCursorAdapter extends ShadowCursorAdapter {
  private int mLayout;
  private int mDropDownLayout;
  private LayoutInflater mInflater;

  public void __constructor__(Context context, int layout, Cursor c) {
    super.__constructor__(context, c);
    mLayout = mDropDownLayout = layout;
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  public void __constructor__(Context context, int layout, Cursor c, boolean autoRequery) {
    super.__constructor__(context, c, autoRequery);
    mLayout = mDropDownLayout = layout;
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  public void __constructor__(Context context, int layout, Cursor c, int flags) {
    super.__constructor__(context, c, flags);
    mLayout = mDropDownLayout = layout;
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Implementation
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return mInflater.inflate(mLayout, parent, false);
  }

  @Implementation
  public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
    return mInflater.inflate(mDropDownLayout, parent, false);
  }

  @Implementation
  public void setViewResource(int layout) {
    mLayout = layout;
  }

  @Implementation
  public void setDropDownViewResource(int dropDownLayout) {
    mDropDownLayout = dropDownLayout;
  }
}