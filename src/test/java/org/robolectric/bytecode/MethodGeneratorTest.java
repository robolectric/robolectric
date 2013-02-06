package org.robolectric.bytecode;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MethodGeneratorTest {
    private ClassPool classPool;
    private Setup setup;

    @Before public void setUp() throws Exception {
        classPool = new ClassPool(true);
        setup = new Setup();
    }

    @Test
    public void whenMethodReturnsObject_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.String");
        String methodBody = new MethodGenerator(ctClass, setup).generateMethodBody(
                ctClass.getDeclaredMethod("substring", new CtClass[]{CtClass.intType}),
                ctClass, RoboType.OBJECT, false, false);
        assertEquals("if (!org.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(this)) {\n" +
                "Object x = org.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  java.lang.String.class, \"substring\", this, new String[] {\"int\"}, new Object[] {org.robolectric.bytecode.RobolectricInternals.autobox($1)});\n" +
                "if (x != null) return ((java.lang.String) x);\n" +
                "return null;\n" +
                "}\n", methodBody);
    }

    @Test
    public void whenMethodReturnsPrimitive_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.String");
        String methodBody = new MethodGenerator(ctClass, setup).generateMethodBody(
                ctClass.getDeclaredMethod("length"),
                ctClass, RoboType.OBJECT, false, false);
        assertEquals("if (!org.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(this)) {\n" +
                "Object x = org.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  java.lang.String.class, \"length\", this, new String[0], new Object[0]);\n" +
                "if (x != null) return ((java.lang.String) x);\n" +
                "return null;\n" +
                "}\n", methodBody);
    }

    @Test
    public void whenMethodReturnsVoid_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.Object");
        String methodBody = new MethodGenerator(ctClass, setup).generateMethodBody(
                ctClass.getDeclaredMethod("wait"),
                ctClass, RoboType.VOID, false, false);
        assertEquals("if (!org.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(this)) {\n" +
                "org.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  java.lang.Object.class, \"wait\", this, new String[] {\"long\"}, new Object[] {org.robolectric.bytecode.RobolectricInternals.autobox($1)});\n" +
                "return;\n" +
                "}\n", methodBody);
    }

    @Test
    public void whenMethodIsStatic_shouldGenerateMethodBody() throws Exception {
        CtClass ctClass = classPool.get("java.lang.String");
        String methodBody = new MethodGenerator(ctClass, setup).generateMethodBody(
                ctClass.getDeclaredMethod("valueOf", new CtClass[]{CtClass.intType}),
                ctClass, RoboType.OBJECT, true, false);
        assertEquals("if (!org.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(java.lang.String.class)) {\n" +
                "Object x = org.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  java.lang.String.class, \"valueOf\", null, new String[] {\"int\"}, new Object[] {org.robolectric.bytecode.RobolectricInternals.autobox($1)});\n" +
                "if (x != null) return ((java.lang.String) x);\n" +
                "return null;\n" +
                "}\n", methodBody);
    }

    @Test
    public void shouldGenerateParameterList() throws Exception {
        MethodGenerator methodGenerator = new MethodGenerator(classPool.getCtClass(Object.class.getName()), setup);
        assertEquals(methodGenerator.makeParameterList(0), "");
        assertEquals(methodGenerator.makeParameterList(1), "$1");
        assertEquals(methodGenerator.makeParameterList(2), "$1, $2");
    }

    @Test
    public void shouldGenerateMethodBodyForEquals() throws Exception {
        CtClass subCtClass = classPool.get(TextView.class.getName());
        CtClass objectCtClass = classPool.get(Object.class.getName());
        String methodBody = new MethodGenerator(subCtClass, setup).generateMethodBody(
                objectCtClass.getDeclaredMethod("equals", new CtClass[]{objectCtClass}),
                subCtClass, RoboType.BOOLEAN, false, true);
        assertEquals("if (!org.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(this)) {\n" +
                "Object x = org.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  android.widget.TextView.class, \"equals\", this, new String[] {\"java.lang.Object\"}, new Object[] {org.robolectric.bytecode.RobolectricInternals.autobox($1)});\n" +
                "if (x != null) return ((java.lang.Boolean) x).booleanValue();\n" +
                "org.robolectric.bytecode.RobolectricInternals.directlyOn($0);\n" +
                "return super.equals($1);}\n", methodBody);
    }

    @Test
    public void shouldGenerateMethodBodyForEqualsWithoutDirectBypassIfSuperclassIsNotInstrumented() throws Exception {
        CtClass subCtClass = classPool.get(View.class.getName());
        CtClass objectCtClass = classPool.get(Object.class.getName());
        String methodBody = new MethodGenerator(subCtClass, setup).generateMethodBody(
                objectCtClass.getDeclaredMethod("equals", new CtClass[]{objectCtClass}),
                subCtClass, RoboType.BOOLEAN, false, true);
        assertEquals("if (!org.robolectric.bytecode.RobolectricInternals.shouldCallDirectly(this)) {\n" +
                "Object x = org.robolectric.bytecode.RobolectricInternals.methodInvoked(\n" +
                "  android.view.View.class, \"equals\", this, new String[] {\"java.lang.Object\"}, new Object[] {org.robolectric.bytecode.RobolectricInternals.autobox($1)});\n" +
                "if (x != null) return ((java.lang.Boolean) x).booleanValue();\n" +
                "return super.equals($1);}\n", methodBody);
    }

    @Test
    public void shouldGenerateConstructorMethodBodyWhichCallsShadowWranglerInitAndThisConstructor() throws Exception {
        CtClass ctClass = classPool.get(View.class.getName());
        String methodBody = new MethodGenerator(ctClass, setup).generateConstructorBody(
                new CtClass[]{classPool.get(Context.class.getName())});
        assertEquals("{\n" +
                "__constructor__($1);\n" +
                "}\n", methodBody);
    }
}
