package org.robolectric.bytecode;

import org.robolectric.internal.Implements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShadowMap {
    public static final ShadowMap EMPTY = new ShadowMap(Collections.<String, ShadowConfig>emptyMap());

    private final Map<String, ShadowConfig> map;

    ShadowMap(Map<String, ShadowConfig> map) {
        this.map = new HashMap<String, ShadowConfig>(map);
    }

    public ShadowConfig get(String className) {
        return map.get(className);
    }

    public ShadowConfig get(Class<?> clazz) {
        return get(clazz.getName());
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    String getShadowClassName(Class clazz) {
        ShadowConfig shadowConfig = null;
        while (shadowConfig == null && clazz != null) {
            shadowConfig = get(clazz.getName());
            clazz = clazz.getSuperclass();
        }
        return shadowConfig == null ? null : shadowConfig.shadowClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShadowMap shadowMap = (ShadowMap) o;

        if (!map.equals(shadowMap.map)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    public static class Builder {
        private static final Set<String> unloadableClassNames = new HashSet<String>();

        private static void warnAbout(String unloadableClassName) {
            boolean alreadyReported;
            synchronized (unloadableClassNames) {
                alreadyReported = unloadableClassNames.add(unloadableClassName);
            }
            if (alreadyReported) {
                System.out.println("Warning: an error occurred while binding shadow class: " + unloadableClassName);
            }
        }

        private final Map<String, ShadowConfig> map;

        public Builder() {
            map = new HashMap<String, ShadowConfig>();
        }

        public Builder(ShadowMap shadowMap) {
            this.map = new HashMap<String, ShadowConfig>(shadowMap.map);
        }

        public Builder addShadowClasses(Class<?>... shadowClasses) {
            for (Class<?> shadowClass : shadowClasses) {
                addShadowClass(shadowClass);
            }
            return this;
        }

        public Builder addShadowClasses(Collection<Class<?>> shadowClasses) {
            for (Class<?> shadowClass : shadowClasses) {
                addShadowClass(shadowClass);
            }
            return this;
        }

        public Builder addShadowClass(Class<?> shadowClass) {
            Implements implementsAnnotation = shadowClass.getAnnotation(Implements.class);
            if (implementsAnnotation == null) {
                throw new IllegalArgumentException(shadowClass + " is not annotated with @Implements");
            }

            try {
                String className = implementsAnnotation.className();
                if (className.isEmpty()) {
                    className = implementsAnnotation.value().getName();
                }
                addShadowClass(className, shadowClass, implementsAnnotation.callThroughByDefault());
            } catch (TypeNotPresentException typeLoadingException) {
                String unloadableClassName = shadowClass.getSimpleName();
                if (typeLoadingException.typeName().startsWith("com.google.android.maps")) {
                    warnAbout(unloadableClassName);
                    return this;
                } else if (isIgnorableClassLoadingException(typeLoadingException)) {
                    //this allows users of the robolectric.jar file to use the non-Google APIs version of the api
                    warnAbout(unloadableClassName);
                } else {
                    throw typeLoadingException;
                }
            }
            return this;
        }

        public Builder addShadowClass(String realClassName, Class<?> shadowClass, boolean callThroughByDefault) {
            addShadowClass(realClassName, shadowClass.getName(), callThroughByDefault);
            return this;
        }

        public Builder addShadowClass(Class<?> realClass, Class<?> shadowClass, boolean callThroughByDefault) {
            addShadowClass(realClass.getName(), shadowClass.getName(), callThroughByDefault);
            return this;
        }

        public Builder addShadowClass(String realClassName, String shadowClassName, boolean callThroughByDefault) {
            map.put(realClassName, new ShadowConfig(shadowClassName, callThroughByDefault));
            return this;
        }

        public ShadowMap build() {
            return new ShadowMap(map);
        }

        private static boolean isIgnorableClassLoadingException(Throwable typeLoadingException) {
            if (typeLoadingException != null) {
                // instanceof doesn't work here. Are we in different classloaders?
                if (typeLoadingException.getClass().getName().equals(IgnorableClassNotFoundException.class.getName())) {
                    return true;
                }

                if (typeLoadingException instanceof NoClassDefFoundError
                        || typeLoadingException instanceof ClassNotFoundException
                        || typeLoadingException instanceof TypeNotPresentException) {
                    return isIgnorableClassLoadingException(typeLoadingException.getCause());
                }
            }
            return false;
        }
    }
}
