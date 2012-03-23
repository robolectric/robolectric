package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.Fragment;

import java.io.Serializable;

public class SerializedFragmentState implements Serializable {
    public final String tag;
    public final int id;
    public final Class<? extends Fragment> fragmentClass;
    public final int containerId;

    public SerializedFragmentState(Integer containerId, Fragment fragment) {
        this.containerId = containerId;
        id = fragment.getId();
        tag = fragment.getTag();
        fragmentClass = fragment.getClass();
    }
}
