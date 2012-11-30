package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.RobolectricContext;
import com.xtremelabs.robolectric.annotation.DisableStrictI18n;
import com.xtremelabs.robolectric.annotation.EnableStrictI18n;
import com.xtremelabs.robolectric.annotation.Values;
import com.xtremelabs.robolectric.internal.DoNotInstrument;
import com.xtremelabs.robolectric.internal.Instrument;
import com.xtremelabs.robolectric.res.ResourcePath;
import com.xtremelabs.robolectric.util.I18nException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
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

    public List<Class<?>> getClassesToDelegateFromRcl() {
        //noinspection unchecked
        return Arrays.asList(
                RobolectricClassLoader.class, RobolectricContext.class, RobolectricContext.Factory.class, ResourcePath.class,
                AndroidTranslator.class, ClassHandler.class, Instrument.class, DoNotInstrument.class, Values.class,
                EnableStrictI18n.class, DisableStrictI18n.class, I18nException.class
        );
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
        return true;
    }

    public boolean shouldAcquire(String name) {
        return !(
                name.startsWith("org.junit")
                        || name.startsWith("org.hamcrest")
                        || name.startsWith("org.specs2") // allows for android projects with mixed scala\java tests to be
                        || name.startsWith("scala.")     //  run with Maven Surefire (see the RoboSpecs project on github)
                        || name.startsWith("org.sqlite.") // ugh, javassist is barfing while loading org.sqlite now for some reason?!?
        );
    }

    public static class FakeSubclass {}

    /**
     * Map from a requested class to an alternate stand-in, or not.
     * @param className
     * @return
     */
    public String translateClassName(String className) {
        if (className.equals("com.android.i18n.phonenumbers.NumberParseException")) {
            return Exception.class.getName();
        } else if (className.equals("com.android.i18n.phonenumbers.Phonenumber$PhoneNumber")) {
            return FakeSubclass.class.getName();
        } else {
            return className;
        }
    }
}
