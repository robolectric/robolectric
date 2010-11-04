package com.xtremelabs.robolectric;

import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AndroidTranslatorUnitTest {
    private ClassPool classPool;
    private AndroidTranslator androidTranslator;

    @Before public void setUp() throws Exception {
        classPool = new ClassPool(true);
        androidTranslator = new AndroidTranslator(null) {
            @Override protected int getIndex() {
                return 0;
            }
        };
    }

    @Test
    public void whenMethodReturnsObject_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.String");
        String methodBody = androidTranslator.generateMethodBody(
                ctClass, ctClass.getDeclaredMethod("substring", new CtClass[]{CtClass.intType}),
                ctClass, Type.OBJECT, false);
        assertEquals("if (!java.lang.String.___bypassShadow___) {\n" +
                "Object x = com.xtremelabs.robolectric.AndroidTranslator.get(0).methodInvoked(\n" +
                "  java.lang.String.class, \"substring\", this, new String[] {\"int\"}, new Object[] {com.xtremelabs.robolectric.AndroidTranslator.autobox($1)});\n" +
                "if (x != null) return ((java.lang.String) x);\n" +
                "return null;\n" +
                "}\n" +
                "java.lang.String.___bypassShadow___ = false;\n", methodBody);
    }

    @Test
    public void whenMethodReturnsPrimitive_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.String");
        String methodBody = androidTranslator.generateMethodBody(
                ctClass, ctClass.getDeclaredMethod("length"),
                ctClass, Type.OBJECT, false);
        assertEquals("if (!java.lang.String.___bypassShadow___) {\n" +
                "Object x = com.xtremelabs.robolectric.AndroidTranslator.get(0).methodInvoked(\n" +
                "  java.lang.String.class, \"length\", this, new String[0], new Object[0]);\n" +
                "if (x != null) return ((java.lang.String) x);\n" +
                "return null;\n" +
                "}\n" +
                "java.lang.String.___bypassShadow___ = false;\n", methodBody);
    }

    @Test
    public void whenMethodReturnsVoid_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.Object");
        String methodBody = androidTranslator.generateMethodBody(
                ctClass, ctClass.getDeclaredMethod("wait"),
                ctClass, Type.VOID, false);
        assertEquals("if (!java.lang.Object.___bypassShadow___) {\n" +
                "com.xtremelabs.robolectric.AndroidTranslator.get(0).methodInvoked(\n" +
                "  java.lang.Object.class, \"wait\", this, new String[] {\"long\"}, new Object[] {com.xtremelabs.robolectric.AndroidTranslator.autobox($1)});\n" +
                "return;\n" +
                "}\n" +
                "java.lang.Object.___bypassShadow___ = false;\n", methodBody);
    }

    @Test
    public void whenMethodIsStatic_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.String");
        String methodBody = androidTranslator.generateMethodBody(
                ctClass, ctClass.getDeclaredMethod("valueOf", new CtClass[]{CtClass.intType}),
                ctClass, Type.OBJECT, false);
        assertEquals("if (!java.lang.String.___bypassShadow___) {\n" +
                "Object x = com.xtremelabs.robolectric.AndroidTranslator.get(0).methodInvoked(\n" +
                "  java.lang.String.class, \"valueOf\", this, new String[] {\"int\"}, new Object[] {com.xtremelabs.robolectric.AndroidTranslator.autobox($1)});\n" +
                "if (x != null) return ((java.lang.String) x);\n" +
                "return null;\n" +
                "}\n" +
                "java.lang.String.___bypassShadow___ = false;\n", methodBody);
    }
}
