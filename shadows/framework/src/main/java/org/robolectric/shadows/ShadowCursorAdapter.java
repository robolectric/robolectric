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

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * @deprecated This class will be removed in Robolectric 3.5. The real
 * Android CursorAdapter will be used instead.
 */
@Implements(CursorAdapter.class)
@Deprecated
public class ShadowCursorAdapter extends ShadowBaseAdapter {
  private @RealObject CursorAdapter realCursorAdapter;

  private List<View> views = null;

  /**
   * If {@link #setViews(List)} has been called, a corresponding view from that list will be returned.
   *
   * Otherwise, behaves like {@link CursorAdapter#getView(int, View, ViewGroup)}.
   */
  @Implementation
  @Deprecated
  public View getView(int position, View convertView, ViewGroup parent) {
    if (views != null) {
      // if the cursor is null OR there are no views to dispense return null
      if (realCursorAdapter.getCursor() == null || views.size() == 0 ) {
        return null;
      }

      if (convertView != null) {
        return convertView;
      }

      return views.get(position);
    } else {
      return directlyOn(realCursorAdapter, CursorAdapter.class).getView(position, convertView, parent);
    }
  }

  /**
   * Sets a list of views to be returned for successive calls to {@link #getView(int, View, ViewGroup)}.
   *
   * @param views The list of views
   * @deprecated This method will be removed in Robolectric 3.5. The normal Android {@link #getView(int, View, ViewGroup)} behavior will be used instead.
   */
  @Deprecated
  public void setViews(List<View> views) {
    this.views = views;
  }

}
