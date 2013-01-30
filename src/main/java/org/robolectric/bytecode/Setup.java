package com.xtremelabs.robolectric.bytecode;

import android.R;
import android.net.Uri__FromAndroid;
import com.xtremelabs.robolectric.AndroidManifest;
import com.xtremelabs.robolectric.RobolectricContext;
import com.xtremelabs.robolectric.annotation.DisableStrictI18n;
import com.xtremelabs.robolectric.annotation.EnableStrictI18n;
import com.xtremelabs.robolectric.annotation.Values;
import com.xtremelabs.robolectric.internal.DoNotInstrument;
import com.xtremelabs.robolectric.internal.Instrument;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.internal.RobolectricTestRunnerInterface;
import com.xtremelabs.robolectric.res.ResourcePath;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import com.xtremelabs.robolectric.util.I18nException;
import javassist.CtClass;

import java.util.*;

import static java.util.Arrays.asList;

public class Setup {
    public List<Class<?>> getClassesToDelegateFromRcl() {
        //noinspection unchecked
        return asList(
                Uri__FromAndroid.class,
                RobolectricTestRunnerInterface.class,
                RealObject.class,
                ShadowWrangler.class,
                Vars.class,
                AndroidManifest.class,
                DatabaseConfig.DatabaseMap.class,
                R.class,

                RobolectricClassLoader.class,
                RobolectricContext.class,
                RobolectricContext.Factory.class,
                ResourcePath.class,
                AndroidTranslator.class,
                ClassHandler.class,
                Instrument.class,
                DoNotInstrument.class,
                Values.class,
                EnableStrictI18n.class,
                DisableStrictI18n.class,
                I18nException.class
        );
    }


    public boolean invokeApiMethodBodiesWhenShadowMethodIsMissing(Class clazz, String methodName, Class<?>[] paramClasses) {
        return !isFromAndroidSdk(clazz);
    }

    public boolean shouldInstrument(CtClass ctClass) {
        if (ctClass.isInterface() || ctClass.isAnnotation() || ctClass.hasAnnotation(DoNotInstrument.class)) {
            return false;
        }

        if (isFromAndroidSdk(ctClass)) {
            return true;
        }

        return false;

    }

    public boolean isFromAndroidSdk(CtClass ctClass) {
        // allow explicit control with @Instrument, mostly for tests
        return ctClass.hasAnnotation(Instrument.class) || isFromAndroidSdk(ctClass.getName());

    }

    public boolean isFromAndroidSdk(Class clazz) {
        // allow explicit control with @Instrument, mostly for tests
        //noinspection unchecked
        return clazz.getAnnotation(Instrument.class) != null || isFromAndroidSdk(clazz.getName());
    }

    public boolean isFromAndroidSdk(String className) {
        return className.startsWith("android")
                || className.startsWith("libcore.")
                || className.startsWith("com.google.android.maps")
                || className.startsWith("org.apache.http.impl.client.DefaultRequestDirector");
    }

    public boolean shouldPerformStaticInitializationIfShadowIsMissing() {
        return true;
    }

    public boolean shouldAcquire(String name) {
        return !(
                name.matches(".*\\.R(|\\$[a-z]+)$")
                        || name.startsWith("org.junit")
                        || name.startsWith("org.hamcrest")
                        || name.startsWith("org.specs2") // allows for android projects with mixed scala\java tests to be
                        || name.startsWith("scala.")     //  run with Maven Surefire (see the RoboSpecs project on github)
                        || name.startsWith("org.sqlite.") // ugh, javassist is barfing while loading org.sqlite now for some reason?!?
        );
    }

    public Set<MethodRef> methodsToIntercept() {
        return Collections.unmodifiableSet(new HashSet<MethodRef>(asList(
                new MethodRef(System.class, "loadLibrary")
        )));
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

    public static class MethodRef {
        private final String className;
        private final String methodName;

        public MethodRef(Class<?> clazz, String methodName) {
            this(clazz.getName(), methodName);
        }

        public MethodRef(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodRef methodRef = (MethodRef) o;

            if (!className.equals(methodRef.className)) return false;
            if (!methodName.equals(methodRef.methodName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = className.hashCode();
            result = 31 * result + methodName.hashCode();
            return result;
        }
    }
}
