package org.robolectric.shadows;

import android.widget.ExpandableListView;
import org.robolectric.annotation.Implements;

@Implements(value = ExpandableListView.class)
public class ShadowExpandableListView extends ShadowListView {
}