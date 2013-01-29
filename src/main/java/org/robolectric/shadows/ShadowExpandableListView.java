package org.robolectric.shadows;

import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@Implements(ExpandableListView.class)
public class ShadowExpandableListView extends ShadowListView {
    @RealObject private ExpandableListView mExpandable;
    private OnChildClickListener mChildClickListener;

    @Implementation
    @Override
    public boolean performItemClick(View view, int position, long id) {
        if (mChildClickListener != null) {
            mChildClickListener.onChildClick(mExpandable, null, 0, position, id);
            return true;
        }
        return false;
    }

    @Implementation
    public void setOnChildClickListener(OnChildClickListener clildListener) {
        mChildClickListener = clildListener;
    }
}