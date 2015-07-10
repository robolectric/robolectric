package org.robolectric.shadows;

import android.widget.ExpandableListView;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.widget.ExpandableListView}.
 */
@Implements(ExpandableListView.class)
public class ShadowExpandableListView extends ShadowListView {
}
