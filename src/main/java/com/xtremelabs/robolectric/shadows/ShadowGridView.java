package com.xtremelabs.robolectric.shadows;

import android.widget.GridView;
import android.widget.ListAdapter;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(GridView.class)
public class ShadowGridView extends ShadowAdapterView {
    @RealObject private GridView realGridView;

    @Implementation
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
    }
}
