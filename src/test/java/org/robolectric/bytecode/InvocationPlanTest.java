package org.robolectric.bytecode;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.internal.Implements;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.method;

public class InvocationPlanTest {

    private ShadowMap shadowMap;

    @Before
    public void setUp() throws Exception {
        shadowMap = new ShadowMap.Builder()
                .addShadowClass(View.class, ShadowView.class, true)
                .addShadowClass(SubView.class, ShadowSubView.class, true)
                .build();
    }

    @Test
    public void shouldOnlyInvokeShadowMethodsOnShadowImplementingTheCorrectClass() throws Exception {
        // an overriding subclass method has been invoked, and there's not a corresponding shadow method at the same level in the class hierarchy
        InvocationPlan plan = new InvocationPlan(shadowMap, SubView.class, ShadowSubView.class, "show", false);
        assertThat(plan.hasShadowImplementation()).isFalse();

        // the super method has been invoked, and we find a corresponding shadow method at the same level in the class hierarchy
        plan = new InvocationPlan(shadowMap, View.class, ShadowView.class, "show", false);
        assertThat(plan.hasShadowImplementation()).isTrue();
        assertThat(plan.getMethod()).isEqualTo(method("show").withParameterTypes().in(ShadowView.class).info());
    }

    @Test
    public void whenCallThroughIsOffForAClass_shouldInvokeShadowMethodsOnSuperclass() throws Exception {
        ShadowMap shadowMap = new ShadowMap.Builder()
                .addShadowClass(View.class, ShadowView.class, false)
                .addShadowClass(SubView.class, ShadowSubView.class, false)
                .build();
        InvocationPlan plan = new InvocationPlan(shadowMap, SubView.class, ShadowSubView.class, "show", false);
        assertThat(plan.hasShadowImplementation()).isTrue();
        assertThat(plan.getMethod()).isEqualTo(method("show").withParameterTypes().in(ShadowView.class).info());
    }

    @Test
    public void forStaticMethods_shouldOnlyInvokeShadowMethodsOnShadowClassesForTheSameGeneration() throws Exception {
        InvocationPlan plan = new InvocationPlan(shadowMap, View.class, ShadowView.class, "getSystem", true);
        assertThat(plan.hasShadowImplementation()).isTrue();
        assertThat(plan.getMethod()).isEqualTo(method("getSystem").withParameterTypes().in(ShadowView.class).info());

        // a custom shadow extending the original shadow is used; we should still recognize getSystem from its superclass
        ShadowMap modifiedShadowMap = shadowMap.newBuilder().addShadowClass(View.class, MyShadowView.class, false).build();

        plan = new InvocationPlan(modifiedShadowMap, View.class, MyShadowView.class, "getSystem", true);
        assertThat(plan.hasShadowImplementation()).isTrue();
        assertThat(plan.getMethod()).isEqualTo(method("getSystem").withParameterTypes().in(ShadowView.class).info());
    }

    @Test public void shouldNeverConsiderMethodsOnObjectToBeShadowMethods() throws Exception {
        ShadowMap shadowMap = new ShadowMap.Builder()
                .addShadowClass(View.class, ShadowView.class, false)
                .addShadowClass(SubView.class, ShadowSubView.class, false)
                .build();
        assertThat(new InvocationPlan(shadowMap, View.class, ShadowView.class, "equals", false, Object.class.getName())
                .hasShadowImplementation()).isFalse();
        assertThat(new InvocationPlan(shadowMap, View.class, ShadowView.class, "hashCode", false)
                .hasShadowImplementation()).isFalse();
        assertThat(new InvocationPlan(shadowMap, View.class, ShadowView.class, "toString", false)
                .hasShadowImplementation()).isFalse();
    }

    public static class View {
        public void show() {
        }

        @Override public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override public int hashCode() {
            return super.hashCode();
        }

        @Override public String toString() {
            return super.toString();
        }

        public static void getSystem() {
        }
    }

    public static class SubView extends View {
        @Override
        public void show() {
            super.show();
        }
    }

    @Implements(View.class)
    public static class ShadowView {
        public void show() {
        }

        public static void getSystem() {
        }
    }

    @Implements(SubView.class)
    public static class ShadowSubView extends ShadowView {
        public static void getSystem() {
        }
    }

    @Implements(View.class)
    public static class MyShadowView extends ShadowView {
    }
}
