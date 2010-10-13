package com.xtremelabs.droidsugar.fakes;

import android.app.Application;
import android.app.Service;
import com.xtremelabs.droidsugar.util.FakeHelper;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

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
}
