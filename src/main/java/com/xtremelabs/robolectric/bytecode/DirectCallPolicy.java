package com.xtremelabs.robolectric.bytecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Policy that defined how direct calls are handled. Policy is thread local.
 */
public interface DirectCallPolicy {

    /** NoDirectCallPolicy instance. */
    DirectCallPolicy NOP = new NoDirectCallPolicy();

    /**
     * Decide whether call must be performed directly. 
     * @param target target object
     * @return true if call must be direct
     */
    boolean shouldCallDirectly(Object target);

    /**
     * Called after each object method invocation. 
     * It's executed in <code>finally</code> block.
     * @param target object which method has been invoked on
     * @return policy to replace current instance
     */
    DirectCallPolicy onMethodInvocationFinished(Object target);

    /**
     * Called when direct call request appears. This method can either throw {@link DirectCallException} or return false 
     * if policy change is not appropriate. 
     * @param previousPolicy previous policy instance
     * @return true if policy change should be applied
     */
    boolean checkForChange(DirectCallPolicy previousPolicy);

    /** Throw by policy instances in case of illegal state exception. */
    public static class DirectCallException extends IllegalStateException {

        private static final long serialVersionUID = 4326926182637261742L;

        public DirectCallException(String msg) {
            super(msg);
        }

    }

    /** Direct call is not performed. */
    public static class NoDirectCallPolicy implements DirectCallPolicy  {

        private NoDirectCallPolicy() { /* hidden */ }

        @Override
        public boolean shouldCallDirectly(Object target) {
            return false;
        }
        @Override
        public DirectCallPolicy onMethodInvocationFinished(Object target) {
            return this;
        }
        @Override
        public boolean checkForChange(DirectCallPolicy previousPolicy) {
            return true;
        }
    }

    abstract static class SafeDirectCallPolicy implements DirectCallPolicy {
        /** Direct instance. */
        /* package */ Object expectedInstance;

        public SafeDirectCallPolicy(Object directInstance) {
            if (directInstance == null) { throw new DirectCallException("Direct call target cannot be null, use class instance for static calls"); }
            this.expectedInstance = directInstance;
        }

        @Override
        public boolean shouldCallDirectly(Object target) {
            if (expectedInstance == null) { return false; }

            if (expectedInstance != target) {
                Object expected = expectedInstance;
                expectedInstance = null;
                throw new DirectCallException("expected to perform direct call on <" + expected + "> but got <" + target + ">");
            }

            return true;
        }
    }

    /** Direct call is performed once only. */
    public static class OneShotDirectCallPolicy extends SafeDirectCallPolicy {

        public OneShotDirectCallPolicy(Object directInstance) {
            super(directInstance);
        }

        @Override
        public boolean shouldCallDirectly(Object target) {
            boolean result = super.shouldCallDirectly(target);
            expectedInstance = null;
            return result;
        }

        @Override
        public DirectCallPolicy onMethodInvocationFinished(Object target) {
            return NOP;
        }

        @Override
        public boolean checkForChange(DirectCallPolicy previousPolicy) {
            // first setup
            if (previousPolicy == NOP) { return true; }

            // twice setup
            if (previousPolicy instanceof OneShotDirectCallPolicy) {
                throw new DirectCallException("already expecting a direct call on <" + ((OneShotDirectCallPolicy) previousPolicy).expectedInstance + "> but here's a new request for <" + expectedInstance + ">");
            }

            // we are inside full stack direct call => do not change anything
            if (previousPolicy instanceof FullStackDirectCallPolicy && ((FullStackDirectCallPolicy) previousPolicy).checkWeAreDeepInsideStack()) {
                return false;
            }

            // unexpected
            throw new DirectCallException("Direct call policy is already set to " + previousPolicy);
        }
    }

    /** Direct call is performed within the invocation full stack. */
    public static class FullStackDirectCallPolicy extends SafeDirectCallPolicy {

        /** Include static calls. */
        private final boolean withStatics;
        /** Include list. */
        private final List<String> includeList;
        
        /** Stack depth. */
        private int depth = -1;

        public static class Builder<T> {
            private boolean statics;
            private List<String> includeList;
            private final T target;
            
            private Builder(final T target) {
                if (target == null) { throw new IllegalArgumentException("Direct target is null"); }
                this.target = target;
            }
            
            public Builder<T> withStatics(boolean statics) {
                this.statics = statics;
                return this;
            }
            
            private void ensureIncludeList() {
                if (includeList == null) {
                    includeList = new ArrayList<String>();
                }
            }
            
            public Builder<T> include(final List<String> packages) {
                if (packages != null && !packages.isEmpty()) {
                    ensureIncludeList();
                    includeList.addAll(packages);
                }
                return this;
            }
            
            public Builder<T> include(final Class<?>... classes) {
                if (classes != null && classes.length != 0) {
                    ensureIncludeList();
                    for (Class<?> clazz : classes) {
                        includeList.add(clazz.getName());
                    }
                }
                return this;
            }
            
            public T getTarget() {
                return target;
            }
            
            public FullStackDirectCallPolicy create() {
                return new FullStackDirectCallPolicy(target, statics, includeList);
            }
            
        }
        
        private FullStackDirectCallPolicy(final Object directInstance, final boolean withStatics, final List<String> includeList) {
            super(directInstance);
            this.includeList = includeList;
            this.withStatics = withStatics;
        }

        public static FullStackDirectCallPolicy withTarget(final Object target) {
            return build(target).create();
        }
        
        public static <T> Builder<T> build(final T target) {
            return new Builder<T>(target);
        }
        
        private boolean isClassIncluded(final String name) {
            for (String pckg : includeList) {
                if (name.startsWith(pckg)) { return true; }
            }
            return false;
        }
        
        private boolean shouldBeDirect(final Object target) {
            return expectedInstance == target
                    || (target instanceof Class<?> && withStatics) // static call, we do not instrument Class
                    || (includeList != null && isClassIncluded(target.getClass().getName())); 
        }
        
        @Override
        public boolean shouldCallDirectly(Object target) {
            boolean result = depth == -1 ? super.shouldCallDirectly(target) : true;
            result &= shouldBeDirect(target);
            if (result) { ++depth; }
            return result;
        }

        @Override
        public DirectCallPolicy onMethodInvocationFinished(Object target) {
            if (expectedInstance == null) {
                // error happened
                return NOP;
            }

            if (depth < 0) {
                throw new DirectCallException("Stack depth is negative: " + depth + ", target: " + expectedInstance);
            }
            if (shouldBeDirect(target) && depth-- == 0) {
                return NOP;
            }
            return this;
        }

        boolean checkWeAreDeepInsideStack() {
            return depth >= 0;
        }

        @Override
        public boolean checkForChange(DirectCallPolicy previousPolicy) {
            // first setup
            if (previousPolicy == NOP) { return true; }

            // we are inside full stack direct call => do not change anything
            if (previousPolicy instanceof FullStackDirectCallPolicy && ((FullStackDirectCallPolicy) previousPolicy).checkWeAreDeepInsideStack()) {
                return false;
            }

            // unexpected, bad setup
            throw new DirectCallException("Direct call policy is already set to " + previousPolicy);
        }

    }

}
