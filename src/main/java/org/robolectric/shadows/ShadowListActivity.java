package org.robolectric.shadows;

import android.app.ListActivity;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@code ListActivity} that supports the retrieval of {@code ListViews}
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListActivity.class)
public class ShadowListActivity extends ShadowActivity { }