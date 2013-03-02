package org.robolectric.shadows;

import android.database.Observable;
import org.robolectric.internal.Implements;

@Implements(value = Observable.class, callThroughByDefault = true)
public class ShadowObservable {
}
