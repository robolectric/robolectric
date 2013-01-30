package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

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
