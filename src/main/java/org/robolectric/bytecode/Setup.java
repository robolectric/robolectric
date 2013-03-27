package org.robolectric.bytecode;

import android.R;
import android.net.Uri__FromAndroid;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricBase;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.SdkEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.DisableStrictI18n;
import org.robolectric.annotation.EnableStrictI18n;
import org.robolectric.internal.DoNotInstrument;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.Instrument;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.RealObject;
import org.robolectric.internal.TestLifecycle;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;
import org.robolectric.util.DatabaseConfig;
import org.robolectric.util.I18nException;
import org.robolectric.util.Transcript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

public class Setup {
    public static final List<String> CLASSES_TO_ALWAYS_DELEGATE = stringify(
            RobolectricBase.class,
            Uri__FromAndroid.class,
            TestLifecycle.class,
            RealObject.class,
            ShadowWrangler.class,
            AndroidManifest.class,
            DatabaseConfig.DatabaseMap.class,
            R.class,

            org.robolectric.bytecode.InstrumentingClassLoader.class,
            org.robolectric.bytecode.JavassistInstrumentingClassLoader.class,
            org.robolectric.bytecode.AsmInstrumentingClassLoader.class,
            SdkEnvironment.class,
            RobolectricTestRunner.class,
            RobolectricTestRunner.HelperTestRunner.class,
            ResourcePath.class,
            ResourceLoader.class,
            AndroidTranslator.class,
            ClassHandler.class,
            ClassHandler.Plan.class,
            Implements.class,
            Implementation.class,
            Instrument.class,
            DoNotInstrument.class,
            Config.class,
            EnableStrictI18n.class,
            DisableStrictI18n.class,
            I18nException.class,
            Transcript.class,
            org.robolectric.bytecode.DirectObjectMarker.class,
            ParallelUniverseInterface.class
    );

    private static List<String> stringify(Class... classes) {
        ArrayList<String> strings = new ArrayList<String>();
        for (Class aClass : classes) {
            strings.add(aClass.getName());
        }
        return strings;
    }

    public List<String> getClassesToDelegateFromRcl() {
        //noinspection unchecked
        return CLASSES_TO_ALWAYS_DELEGATE;
    }

    public boolean shouldInstrument(ClassInfo classInfo) {
        if (classInfo.isInterface() || classInfo.isAnnotation() || classInfo.hasAnnotation(DoNotInstrument.class)) {
            return false;
        }

        if (isFromAndroidSdk(classInfo)) {
            return true;
        }

        return false;
    }

    public boolean isFromAndroidSdk(ClassInfo classInfo) {
        // allow explicit control with @Instrument, mostly for tests
        return classInfo.hasAnnotation(Instrument.class) || isFromAndroidSdk(classInfo.getName());
    }

    public boolean isFromAndroidSdk(Class clazz) {
        // allow explicit control with @Instrument, mostly for tests
        //noinspection unchecked
        return clazz.getAnnotation(Instrument.class) != null || isFromAndroidSdk(clazz.getName());
    }

    public boolean isFromAndroidSdk(String className) {
        return className.startsWith("android.")
                || className.startsWith("libcore.")
                || className.startsWith("com.google.android.maps.")
                || className.startsWith("org.apache.http.impl.client.DefaultRequestDirector");
    }

    public boolean shouldAcquire(String name) {
        // the org.robolectric.res package lives in the base classloader, but not its tests; yuck.
        int lastDot = name.lastIndexOf('.');
        String pkgName = name.substring(0, lastDot == -1 ? 0 : lastDot);
        if (pkgName.equals("org.robolectric.res")) {
            return name.contains("Test");
        }

        if (name.matches("com\\.android\\.internal\\.R(\\$.*)?")) return true;

        return !(
                name.matches(".*\\.R(|\\$[a-z]+)$")
                        || CLASSES_TO_ALWAYS_DELEGATE.contains(name)
                        || name.startsWith("java.")
                        || name.startsWith("javax.")
                        || name.startsWith("sun.")
                        || name.startsWith("com.sun.")
                        || name.startsWith("org.w3c.")
                        || name.startsWith("org.xml.")
                        || name.startsWith("org.junit")
                        || name.startsWith("org.hamcrest")
                        || name.startsWith("org.specs2") // allows for android projects with mixed scala\java tests to be
                        || name.startsWith("scala.")     //  run with Maven Surefire (see the RoboSpecs project on github)
                        || name.startsWith("org.sqlite.") // ugh, javassist is barfing while loading org.sqlite now for some reason?!?
        );
    }

    public Set<MethodRef> methodsToIntercept() {
        return Collections.unmodifiableSet(new HashSet<MethodRef>(asList(
                new MethodRef(LinkedHashMap.class, "eldest"),
                new MethodRef(System.class, "loadLibrary"),
                new MethodRef("android.os.StrictMode", "trackActivity"),
                new MethodRef("com.android.i18n.phonenumbers.Phonenumber$PhoneNumber", "*"),
                new MethodRef("com.android.i18n.phonenumbers.PhoneNumberUtil", "*"),
                new MethodRef("dalvik.system.CloseGuard", "get")
        )));
    }

    /**
     * Map from a requested class to an alternate stand-in, or not.
     *
     * @return
     */
    public Map<String, String> classNameTranslations() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("com.android.i18n.phonenumbers.NumberParseException", Exception.class.getName());
        map.put("com.android.i18n.phonenumbers.PhoneNumberUtil", FakeClass.class.getName());
        map.put("com.android.i18n.phonenumbers.PhoneNumberUtil$PhoneNumberFormat", FakeClass.FakeInnerClass.class.getName());
        map.put("com.android.i18n.phonenumbers.Phonenumber$PhoneNumber", FakeClass.class.getName());
        map.put("dalvik.system.CloseGuard", Object.class.getName());
        return map;
    }

    public static class FakeClass {
        public static class FakeInnerClass {}
    }

    public boolean containsStubs(ClassInfo classInfo) {
        return classInfo.getName().startsWith("com.google.android.maps.");
    }

    public static class MethodRef {
        public final String className;
        public final String methodName;

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

        @Override
        public String toString() {
            return "MethodRef{" +
                    "className='" + className + '\'' +
                    ", methodName='" + methodName + '\'' +
                    '}';
        }
    }
}
