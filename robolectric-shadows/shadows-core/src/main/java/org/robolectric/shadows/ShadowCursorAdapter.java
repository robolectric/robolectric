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
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.Config;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.ArrayList;
import java.util.List;

import static android.widget.CursorAdapter.FLAG_AUTO_REQUERY;
import static android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;

/**
 * Shadow for {@link android.widget.CursorAdapter}.
 */
@Implements(CursorAdapter.class)
public class ShadowCursorAdapter extends ShadowBaseAdapter {
  @RealObject CursorAdapter realCursorAdapter;

  private List<View> views = new ArrayList<>();

  @Implementation
  public View getView(int position, View convertView, ViewGroup parent) {
    // if the cursor is null OR there are no views to dispense return null
    if (this.mCursor == null || views.size() == 0 ) {
      return null;
    }

    if (convertView != null) {
      return convertView;
    }

    return views.get(position);
  }

  /**
   * Non-Android API.  Set a list of views to be returned for successive
   * calls to getView().
   *
   * @param views The list of views
   */
  public void setViews(List<View> views) {
    this.views = views;
  }

  protected boolean mDataValid;
  protected boolean mAutoRequery;
  protected Cursor mCursor;
  protected Context mContext;
  protected int mRowIDColumn;
  protected ChangeObserver mChangeObserver;
  protected DataSetObserver mDataSetObserver;
  protected FilterQueryProvider mFilterQueryProvider;

  @Deprecated
  public void __constructor__(Context context, Cursor c) {
    initialize(context, c, FLAG_AUTO_REQUERY);
  }

  @Deprecated
  public void __constructor__(Context context, Cursor c, boolean autoRequery) {
    initialize(context, c, autoRequery ? FLAG_AUTO_REQUERY : FLAG_REGISTER_CONTENT_OBSERVER);
  }

  // Recommended constructor for API level 11+
  public void __constructor__(Context context, Cursor c, int flags) {
    initialize(context, c, flags);
  }

  // renamed from Android source so as not to conflict with RobolectricWiringTest
  private void initialize(Context context, Cursor c, int flags) {
    boolean cursorPresent = c != null;
    if ((flags & FLAG_AUTO_REQUERY) == FLAG_AUTO_REQUERY) {
      flags |= FLAG_REGISTER_CONTENT_OBSERVER;
      mAutoRequery = true;
    }

    mCursor = c;
    mDataValid = cursorPresent;
    mContext = context;
    mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;

    if ((flags & FLAG_REGISTER_CONTENT_OBSERVER) == FLAG_REGISTER_CONTENT_OBSERVER) {
      mChangeObserver = new ChangeObserver();
      mDataSetObserver = new MyDataSetObserver();
    }

    if (cursorPresent) {
      if (mChangeObserver != null) c.registerContentObserver(mChangeObserver);
      if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
    }
  }

  @Implementation
  public Cursor getCursor() {
    return mCursor;
  }

  @Implementation
  public int getCount() {
    if (mDataValid && mCursor != null) {
      return mCursor.getCount();
    } else {
      return 0;
    }
  }

  @Implementation
  public Object getItem(int position) {
    if (mDataValid && mCursor != null) {
      mCursor.moveToPosition(position);
      return mCursor;
    } else {
      return null;
    }
  }

  @Implementation
  public long getItemId(int position) {
    if (mDataValid && mCursor != null) {
      this.mCursor.getColumnIndexOrThrow("_id");
      if (mCursor.moveToPosition(position)) {
        return mCursor.getLong(mRowIDColumn);
      } else {
        return 0;
      }
    } else {
      return 0;
    }
  }

  @Implementation
  public boolean hasStableIds() {
    return true;
  }

  @Implementation
  public Cursor swapCursor(Cursor cursor) {
    if (cursor == mCursor) {
      return null;
    }
    Cursor old = mCursor;
    if (mCursor != null) {
      if (mChangeObserver != null) mCursor.unregisterContentObserver(mChangeObserver);
      if (mDataSetObserver != null) mCursor.unregisterDataSetObserver(mDataSetObserver);
    }
    mCursor = cursor;
    if (cursor != null) {
      if (mChangeObserver != null) cursor.registerContentObserver(mChangeObserver);
      if (mDataSetObserver != null) cursor.registerDataSetObserver(mDataSetObserver);
      mRowIDColumn = cursor.getColumnIndexOrThrow("_id");
      mDataValid = true;
      // notify the observers about the new cursor
      realCursorAdapter.notifyDataSetChanged();
    } else {
      mRowIDColumn = -1;
      mDataValid = false;
      // notify the observers about the lack of a data set
      realCursorAdapter.notifyDataSetInvalidated();
    }
    return old;
  }

  @Implementation
  public void changeCursor(Cursor newCursor) {
    Cursor old = swapCursor(newCursor);
    if (old != null) {
      old.close();
    }
  }

  @Implementation
  public CharSequence convertToString(Cursor cursor) {
    return cursor == null ? "" : cursor.toString();
  }

  @Implementation
  public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
    if (mFilterQueryProvider != null) {
      return mFilterQueryProvider.runQuery(constraint);
    }

    return mCursor;
  }

  @Implementation
  public FilterQueryProvider getFilterQueryProvider() {
    return mFilterQueryProvider;
  }

  @Implementation
  public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
    mFilterQueryProvider = filterQueryProvider;
  }

  protected void onContentChangedInternal() {
    if (mAutoRequery && mCursor != null && !mCursor.isClosed()) {
      mDataValid = mCursor.requery();
    }
  }

  private class ChangeObserver extends ContentObserver {
    public ChangeObserver() {
      super(new Handler());
    }

    @Override
    public boolean deliverSelfNotifications() {
      return true;
    }

    @Override
    public void onChange(boolean selfChange) {
      onContentChangedInternal();
    }
  }

  private class MyDataSetObserver extends DataSetObserver {
    @Override
    public void onChanged() {
      mDataValid = true;
      realCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInvalidated() {
      mDataValid = false;
      realCursorAdapter.notifyDataSetInvalidated();
    }
  }
}
