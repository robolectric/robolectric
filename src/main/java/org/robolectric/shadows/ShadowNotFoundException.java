package org.robolectric.shadows;

import android.content.res.Resources;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@Implements(Resources.NotFoundException.class)
public class ShadowNotFoundException {
    @RealObject Resources.NotFoundException realObject;

    private String message;

    public void __constructor__() {
    }

    public void __constructor__(String name) {
        this.message = name;
    }

    @Implementation
    public String toString() {
        return realObject.getClass().getName() + ": " + message;
    }
}
