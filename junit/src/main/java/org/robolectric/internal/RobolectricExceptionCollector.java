package org.robolectric.android.internal;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RobolectricExceptionCollector implements Thread.UncaughtExceptionHandler {

    private static final RobolectricExceptionCollector INSTANCE = new RobolectricExceptionCollector();
    private final List<Throwable> unhandled = new CopyOnWriteArrayList<>();

    private SuppressedHandler suppressedHandler;

    public static void setSuppressedHandler(SuppressedHandler suppressedHandler) {
        INSTANCE.suppressedHandler = suppressedHandler;
    }

    private RobolectricExceptionCollector() {}

    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        INSTANCE.unhandled.add(e);
        this.suppressedHandler.onExceptionAdded(INSTANCE.unhandled);
    }

    public interface SuppressedHandler {
        void onExceptionAdded(List<Throwable> unhandled);
    }
}



