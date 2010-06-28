package com.xtremelabs.droidsugar.view;

import android.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import com.xtremelabs.droidsugar.*;

import java.io.*;

public class ViewAssist {
    public static final ViewLoader VIEW_LOADER;

    static {
        try {
            VIEW_LOADER = new ViewLoader(R.class, new File("res/layout"));
            FakeActivity.viewLoader = VIEW_LOADER;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void prepare() {
        DroidSugarAndroidTestRunner.addProxy(Activity.class, FakeActivity.class);
        DroidSugarAndroidTestRunner.addProxy(View.class, FakeView.class);
        DroidSugarAndroidTestRunner.addProxy(ViewGroup.class, FakeViewGroup.class);
        DroidSugarAndroidTestRunner.addProxy(TextView.class, FakeTextView.class);
        DroidSugarAndroidTestRunner.addProxy(ImageView.class, FakeImageView.class);
    }

    public static FakeView proxyFor(View instance) {
        return (FakeView) DroidSugarAndroidTestRunner.proxyFor(instance);
    }

    public static FakeViewGroup proxyFor(ViewGroup instance) {
        return (FakeViewGroup) DroidSugarAndroidTestRunner.proxyFor(instance);
    }

    public static FakeTextView proxyFor(TextView instance) {
        return (FakeTextView) DroidSugarAndroidTestRunner.proxyFor(instance);
    }

    public static FakeImageView proxyFor(ImageView instance) {
        return (FakeImageView) DroidSugarAndroidTestRunner.proxyFor(instance);
    }
}
