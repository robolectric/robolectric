/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robolectric.shadows;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * Shadow for {@link android.widget.SimpleCursorAdapter}.
 */
@Implements(SimpleCursorAdapter.class)
public class ShadowSimpleCursorAdapter extends ShadowResourceCursorAdapter {
  @RealObject private SimpleCursorAdapter realSimpleCursorAdapter;

  protected int[] mFrom;
  protected int[] mTo;

  private int mStringConversionColumn = -1;
  private CursorToStringConverter mCursorToStringConverter;
  private ViewBinder mViewBinder;
  private String[] mOriginalFrom;

  /**
   * Constructor.
   *
   * @param context The context where the ListView associated with this
   *            SimpleListItemFactory is running
   * @param layout resource identifier of a layout file that defines the views
   *            for this list item. The layout file should include at least
   *            those named views defined in "to"
   * @param c The database cursor.  Can be null if the cursor is not available yet.
   * @param from A list of column names representing the data to bind to the UI.  Can be null
   *            if the cursor is not available yet.
   * @param to The views that should display column in the "from" parameter.
   *            These should all be TextViews. The first N views in this list
   *            are given the values of the first N columns in the from
   *            parameter.  Can be null if the cursor is not available yet.
   */
  public void __constructor__(Context context, int layout, Cursor c, String[] from, int[] to) {
    super.__constructor__(context, layout, c);
    mTo = to;
    mOriginalFrom = from;
    findColumns(from);
  }

  public void __constructor__(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
    this.__constructor__(context, layout, c, from, to);
  }

  @Implementation
  public void bindView(View view, Context context, Cursor cursor) {
    final ViewBinder binder = mViewBinder;
    final int count = mTo.length;
    final int[] from = mFrom;
    final int[] to = mTo;

    for (int i = 0; i < count; i++) {
      final View v = view.findViewById(to[i]);
      if (v != null) {
        boolean bound = false;
        if (binder != null) {
          bound = binder.setViewValue(v, cursor, from[i]);
        }

        if (!bound) {
          String text = cursor.getString(from[i]);
          if (text == null) {
            text = "";
          }

          if (v instanceof TextView) {
            setViewText((TextView) v, text);
          } else if (v instanceof ImageView) {
            setViewImage((ImageView) v, text);
          } else {
            throw new IllegalStateException(v.getClass().getName() + " is not a " +
                " view that can be bounds by this SimpleCursorAdapter");
          }
        }
      }
    }
  }

  @Implementation
  public ViewBinder getViewBinder() {
    return mViewBinder;
  }

  @Implementation
  public void setViewBinder(ViewBinder viewBinder) {
    mViewBinder = viewBinder;
  }

  @Implementation
  public void setViewImage(ImageView v, String value) {
    try {
      v.setImageResource(Integer.parseInt(value));
    } catch (NumberFormatException nfe) {
      v.setImageURI(Uri.parse(value));
    }
  }

  @Implementation
  public void setViewText(TextView v, String text) {
    v.setText(text);
  }

  @Implementation
  public int getStringConversionColumn() {
    return mStringConversionColumn;
  }

  @Implementation
  public void setStringConversionColumn(int stringConversionColumn) {
    mStringConversionColumn = stringConversionColumn;
  }

  @Implementation
  public CursorToStringConverter getCursorToStringConverter() {
    return mCursorToStringConverter;
  }

  @Implementation
  public void setCursorToStringConverter(CursorToStringConverter cursorToStringConverter) {
    mCursorToStringConverter = cursorToStringConverter;
  }

  @Implementation
  public CharSequence convertToString(Cursor cursor) {
    if (mCursorToStringConverter != null) {
      return mCursorToStringConverter.convertToString(cursor);
    } else if (mStringConversionColumn > -1) {
      return cursor.getString(mStringConversionColumn);
    }

    return realSimpleCursorAdapter.convertToString(cursor);
  }

  /**
   * Create a map from an array of strings to an array of column-id integers in mCursor.
   * If mCursor is null, the array will be discarded.
   *
   * @param from the Strings naming the columns of interest
   */
  private void findColumns(String[] from) {
    if (mCursor != null) {
      findColumnsFromCursor(mCursor, from);
    } else {
      mFrom = null;
    }
  }

  private void findColumnsFromCursor(Cursor c, String[] from) {
    // By convention, calling LoaderManager.LoaderCallbacks#onLoaderReset will swap
    // the current cursor for null. In that case, the current mapping is removed.
    if(c != null) {
      int i;
      int count = from.length;
      if (mFrom == null || mFrom.length != count) {
        mFrom = new int[count];
      }
      for (i = 0; i < count; i++) {
        mFrom[i] = c.getColumnIndexOrThrow(from[i]);
      }
    } else {
      mFrom = null;
    }
  }

  @Implementation
  public Cursor swapCursor(Cursor c) {
    // super.swapCursor() will notify observers, so make sure we have a mapping before 
    // this happens
      findColumnsFromCursor(c, mOriginalFrom);
      return super.swapCursor(c);
  }

  @Implementation
  public void changeCursor(Cursor c) {
    findColumnsFromCursor(c, mOriginalFrom);
    super.changeCursor(c);
  }

  @Implementation
  public void changeCursorAndColumns(Cursor c, String[] from, int[] to) {
    mOriginalFrom = from;
    mTo = to;
    realSimpleCursorAdapter.changeCursor(c);
    findColumns(mOriginalFrom);
  }

  @Implementation
  public View getView(int position, View convertView, ViewGroup parent) {
    if (!mDataValid) {
      throw new IllegalStateException("this should only be called when the cursor is valid");
    }
    if (!mCursor.moveToPosition(position)) {
      throw new IllegalStateException("couldn't move cursor to position " + position);
    }
    View v;
    if (convertView == null) {
      v = newView(mContext, mCursor, parent);
    } else {
      v = convertView;
    }
    bindView(v, mContext, mCursor);
    return v;
  }

  @Implementation
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    if (mDataValid) {
      mCursor.moveToPosition(position);
      View v;
      if (convertView == null) {
        v = newDropDownView(mContext, mCursor, parent);
      } else {
        v = convertView;
      }
      bindView(v, mContext, mCursor);
      return v;
    } else {
      return null;
    }
  }
}