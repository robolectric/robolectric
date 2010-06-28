package com.xtremelabs.droidsugar.view;

import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeIntent {
    private Bundle extras;

    public Bundle getExtras() {
        return extras;
    }

    public void putExtra(String key, long value) {
        init();
        extras.putLong(key, value);
    }

    public void putExtra(String key, Serializable value) {
        init();
        extras.putSerializable(key, value);
    }

    public void putExtra(String key, Parcelable value) {
        init();
        extras.putParcelable(key, value);
    }

    public Parcelable getParcelableExtra(String name) {
        return (Parcelable) extras.get(name);
    }

    private void init() {
        if (extras == null) {
            extras = new Bundle();
        }
    }
}
