package com.xtremelabs.robolectric.bytecode;

import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AndroidTranslatorUnitTest {
    private ClassPool classPool;
    private AndroidTranslator androidTranslator;

    @Before public void setUp() throws Exception {
        classPool = new ClassPool(true);
        androidTranslator = new AndroidTranslator(null, null);
    }

    @Test
    public void whenMethodReturnsObject_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.String");
        String methodBody = androidTranslator.generateMethodBody(
                ctClass, ctClass.getDeclaredMethod("substring", new CtClass[]{CtClass.intType}),
                ctClass, Type.OBJECT, false, false);
        assertEquals("if (!com.xtremelabs.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(this)) {\n" +
                "Object x = com.xtremelabs.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  java.lang.String.class, \"substring\", this, new String[] {\"int\"}, new Object[] {com.xtremelabs.robolectric.bytecode.RobolectricInternals.autobox($1)});\n" +
                "if (x != null) return ((java.lang.String) x);\n" +
                "return null;\n" +
                "}\n", methodBody);
    }

    @Test
    public void whenMethodReturnsPrimitive_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.String");
        String methodBody = androidTranslator.generateMethodBody(
                ctClass, ctClass.getDeclaredMethod("length"),
                ctClass, Type.OBJECT, false, false);
        assertEquals("if (!com.xtremelabs.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(this)) {\n" +
                "Object x = com.xtremelabs.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  java.lang.String.class, \"length\", this, new String[0], new Object[0]);\n" +
                "if (x != null) return ((java.lang.String) x);\n" +
                "return null;\n" +
                "}\n", methodBody);
    }

    @Test
    public void whenMethodReturnsVoid_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.Object");
        String methodBody = androidTranslator.generateMethodBody(
                ctClass, ctClass.getDeclaredMethod("wait"),
                ctClass, Type.VOID, false, false);
        assertEquals("if (!com.xtremelabs.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(this)) {\n" +
                "com.xtremelabs.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  java.lang.Object.class, \"wait\", this, new String[] {\"long\"}, new Object[] {com.xtremelabs.robolectric.bytecode.RobolectricInternals.autobox($1)});\n" +
                "return;\n" +
                "}\n", methodBody);
    }

    @Test
    public void whenMethodIsStatic_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.String");
        String methodBody = androidTranslator.generateMethodBody(
                ctClass, ctClass.getDeclaredMethod("valueOf", new CtClass[]{CtClass.intType}),
                ctClass, Type.OBJECT, true, false);
        assertEquals("if (!com.xtremelabs.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(java.lang.String.class)) {\n" +
                "Object x = com.xtremelabs.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  java.lang.String.class, \"valueOf\", null, new String[] {\"int\"}, new Object[] {com.xtremelabs.robolectric.bytecode.RobolectricInternals.autobox($1)});\n" +
                "if (x != null) return ((java.lang.String) x);\n" +
                "return null;\n" +
                "}\n", methodBody);
    }

    @Test
    public void shouldGenerateParameterList() throws Exception {
        assertEquals(androidTranslator.makeParameterReplacementList(0), "");
        assertEquals(androidTranslator.makeParameterReplacementList(1), "$1");
        assertEquals(androidTranslator.makeParameterReplacementList(2), "$1, $2");
    }

    @Test
    public void shouldGenerateMethodBodyForEquals() throws Exception {
        CtClass ctClass = classPool.get("java.lang.Object");
        String methodBody = androidTranslator.generateMethodBody(
                ctClass, ctClass.getDeclaredMethod("equals", new CtClass[]{ctClass}),
                ctClass, Type.BOOLEAN, false, true);
        assertEquals("if (!com.xtremelabs.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(this)) {\n" +
                "Object x = com.xtremelabs.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  java.lang.Object.class, \"equals\", this, new String[] {\"java.lang.Object\"}, new Object[] {com.xtremelabs.robolectric.bytecode.RobolectricInternals.autobox($1)});\n" +
                "if (x != null) return ((java.lang.Boolean) x).booleanValue();\n" +
                "return super.equals($1);}\n", methodBody);
    }

    @Test
    public void shouldInstrumentDefaultRequestDirector() throws Exception {
        assertTrue(androidTranslator.shouldInstrument(classPool.makeClass("org.apache.http.impl.client.DefaultRequestDirector")));
    }

    @Test
    public void shouldInstrumentGoogleMapsClasses() throws Exception {
        assertTrue(androidTranslator.shouldInstrument(classPool.makeClass("com.google.android.maps.SomeMapsClass")));
    }

    @Test
    public void shouldNotInstrumentCoreJdkClasses() throws Exception {
        assertFalse(androidTranslator.shouldInstrument(classPool.get("java.lang.Object")));
        assertFalse(androidTranslator.shouldInstrument(classPool.get("java.lang.String")));
    }

    @Test
    public void shouldInstumentAndroidCoreClasses() throws Exception {
        assertTrue(androidTranslator.shouldInstrument(classPool.makeClass("android.content.Intent")));
        assertTrue(androidTranslator.shouldInstrument(classPool.makeClass("android.and.now.for.something.completely.different")));

    }

    @Test
    public void shouldNotInstrumentLocalBroadcastManager() throws Exception {
        assertFalse(androidTranslator.shouldInstrument(classPool.makeClass("android.support.v4.content.LocalBroadcastManager")));
    }

    @Test
    public void shouldAddCustomShadowClass() throws Exception {
        androidTranslator.addCustomShadowClass("my.custom.Klazz");
        assertTrue(androidTranslator.shouldInstrument(classPool.makeClass("my.custom.Klazz")));
    }

    @Test
    public void testOnLoadWithNonInstrumentedClass() throws Exception {
        ClassHandler handler = mock(ClassHandler.class);
        ClassCache cache = mock(ClassCache.class);

        AndroidTranslator translator = new AndroidTranslator(handler, cache);

        translator.onLoad(classPool, "java.lang.Object");
        verify(cache).isWriting();
        verifyNoMoreInteractions(cache);
        verifyZeroInteractions(handler);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateIfClassCacheIsWriting() throws Exception {
        ClassCache cache = mock(ClassCache.class);
        when(cache.isWriting()).thenReturn(true);
        new AndroidTranslator(null, cache).onLoad(classPool, "java.lang.Object");
    }
}
