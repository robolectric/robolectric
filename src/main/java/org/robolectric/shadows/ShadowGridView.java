package org.robolectric.shadows;

import android.widget.GridView;
import android.widget.ListAdapter;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = GridView.class, inheritImplementationMethods = true)
public class ShadowGridView extends ShadowAdapterView {
    @RealObject private GridView realGridView;

    @Implementation
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
    }
}
