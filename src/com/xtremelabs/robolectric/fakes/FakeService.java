package com.xtremelabs.robolectric.fakes;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import com.xtremelabs.robolectric.util.FakeHelper;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Service.class)
public class FakeService extends FakeContextWrapper {
    public FakeService(Service realService) {
        super(realService);
    }

    @Implementation
    public final Application getApplication() {
        return FakeHelper.application;
    }

    @Implementation
    public Context getApplicationContext() {
        return FakeHelper.application;
    }
}
