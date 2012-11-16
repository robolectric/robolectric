package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.internal.DoNotInstrument;
import com.xtremelabs.robolectric.internal.Instrument;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.ArrayList;
import java.util.List;

public class Setup {
    private final List<String> androidPackages = new ArrayList<String>();
    private final List<String> instrumentingExcludeList = new ArrayList<String>();

    public Setup() {
        // Initialize lists
        androidPackages.add("android.");
        androidPackages.add("libcore.");
        androidPackages.add("com.google.android.maps");
        androidPackages.add("org.apache.http.impl.client.DefaultRequestDirector");

        instrumentingExcludeList.add("android.support.v4.app.NotificationCompat");
        instrumentingExcludeList.add("android.support.v4.util.LruCache");
    }

    public boolean invokeApiMethodBodiesWhenShadowMethodIsMissing(Class clazz) {
        return !isFromAndroidSdk(clazz);
    }

    public boolean shouldInstrument(CtClass ctClass) throws NotFoundException {
        String name = ctClass.getName();

        for (String klassName : instrumentingExcludeList) {
            if (name.startsWith(klassName)) {
                return false;
            }
        }

        if (ctClass.isInterface() || ctClass.isAnnotation() || ctClass.hasAnnotation(DoNotInstrument.class)) {
            return false;
        }

        if (isFromAndroidSdk(ctClass)) {
            return true;
        }

        return false;

    }

    private boolean parentIsInstrumented(CtClass ctClass) throws NotFoundException {
        CtClass superclass = ctClass.getSuperclass();
        return superclass != null && shouldInstrument(superclass);
    }

    public boolean isFromAndroidSdk(CtClass ctClass) {
        if (ctClass.hasAnnotation(Instrument.class)) { // fakey for tests
            return true;
        }

        for (String klassName : androidPackages) {
            if (ctClass.getName().startsWith(klassName)) {
                return true;
            }
        }

        return false;
    }

    public boolean isFromAndroidSdk(Class clazz) {
        if (clazz.getAnnotation(Instrument.class) != null) { // fakey for tests
            return true;
        }

        for (String klassName : androidPackages) {
            if (clazz.getName().startsWith(klassName)) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldPerformStaticInitializationIfShadowIsMissing() {
        return false;
    }
}
