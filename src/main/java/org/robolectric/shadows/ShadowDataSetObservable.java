package org.robolectric.shadows;

import android.database.DataSetObservable;
import org.robolectric.internal.Implements;

@Implements(value = DataSetObservable.class, callThroughByDefault = true)
public class ShadowDataSetObservable extends ShadowObservable {
}
