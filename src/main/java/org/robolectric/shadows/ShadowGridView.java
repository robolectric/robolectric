package org.robolectric.shadows;

import android.widget.GridView;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(GridView.class)
public class ShadowGridView extends ShadowAbsListView {
}
