package com.xtremelabs.droidsugar.fakes;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.test.mock.MockContentResolver;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Application.class)
public class FakeApplication extends ContextWrapper {
    private MockContentResolver contentResolver = new MockContentResolver();

    public FakeApplication(Application base) {
        super(base);
    }

    public ContentResolver getContentResolver() {
        return contentResolver;
    }
}
