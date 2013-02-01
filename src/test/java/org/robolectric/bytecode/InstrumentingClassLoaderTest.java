package org.robolectric.bytecode;

import org.robolectric.internal.DoNotInstrument;
import org.robolectric.internal.Instrument;
import org.robolectric.util.Transcript;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.Loader;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

abstract public class InstrumentingClassLoaderTest {

    private ClassLoader classLoader;
    private MyClassHandler classHandler;
    private Transcript transcript = new Transcript();

    @Before
    public void setUp() throws Exception {
//        transformWithJavassist();
        classLoader = createClassLoader(new Setup());
        classHandler = new MyClassHandler(transcript);
        injectClassHandler(classLoader, classHandler);
    }

    private ClassLoader transformWithJavassist() throws NotFoundException, CannotCompileException, ClassNotFoundException {
        ClassPool cp = new ClassPool();
        cp.appendClassPath(new ClassClassPath(getClass()));
        Loader loader = new Loader();
        loader.delegateLoadingOf(ClassHandler.class.getName());
        loader.addTranslator(cp, new AndroidTranslator(new ZipClassCache("tmp/zzz.jar", -1), new Setup()));
        return loader;

    }

    abstract protected ClassLoader createClassLoader(Setup setup) throws ClassNotFoundException;

    @Test public void shouldAddDefaultConstructorIfMissing() throws Exception {
        Constructor<?> defaultCtor = loadClass(ClassWithNoDefaultConstructor.class).getConstructor();
        assertTrue(Modifier.isPublic(defaultCtor.getModifiers()));
        defaultCtor.setAccessible(true);
        defaultCtor.newInstance();
        transcript.assertNoEventsSoFar();
    }

    @SuppressWarnings("UnusedDeclaration")
    @Instrument static class ClassWithNoDefaultConstructor {
        private String name;

        ClassWithNoDefaultConstructor(String name) {
            this.name = name;
        }
    }

    @Test public void shouldDelegateToHandlerForConstructors() throws Exception {
        Class<?> clazz = loadClass(ClassWithNoDefaultConstructor.class);
        Constructor<?> ctor = clazz.getDeclaredConstructor(String.class);
        assertTrue(Modifier.isPublic(ctor.getModifiers()));
        ctor.setAccessible(true);
        Object instance = ctor.newInstance("new one");
        transcript.assertEventsSoFar("methodInvoked: ClassWithNoDefaultConstructor.__constructor__(java.lang.String new one)");

        Field nameField = clazz.getDeclaredField("name");
        nameField.setAccessible(true);
        assertNull(nameField.get(instance));
    }

    @Test public void shouldDelegateClassLoadForUnacquiredClasses() throws Exception {
        ClassLoader classLoader = createClassLoader(new MySetup(false, false));
        Class<?> exampleClass = classLoader.loadClass(ExampleClass.class.getName());
        assertSame(getClass().getClassLoader(), exampleClass.getClassLoader());
    }

    @Test public void shouldPerformClassLoadForAcquiredClasses() throws Exception {
        ClassLoader classLoader = createClassLoader(new MySetup(true, false));
        Class<?> exampleClass = classLoader.loadClass(NotInstrumentedClass.class.getName());
        assertSame(classLoader, exampleClass.getClassLoader());
        try {
            exampleClass.getField(AsmInstrumentingClassLoader.CLASS_HANDLER_DATA_FIELD_NAME);
            fail("class shouldn't be instrumented!");
        } catch (Exception e) {
            // expected
        }
    }

    public static class NotInstrumentedClass {
    }

    @Test public void shouldPerformClassLoadAndInstrumentLoadForInstrumentedClasses() throws Exception {
        ClassLoader classLoader = createClassLoader(new MySetup(true, true));
        Class<?> exampleClass = classLoader.loadClass(ExampleClass.class.getName());
        assertSame(classLoader, exampleClass.getClassLoader());
        assertNotNull(exampleClass.getField(AsmInstrumentingClassLoader.CLASS_HANDLER_DATA_FIELD_NAME));
    }

    @Test
    public void callingNormalMethodShouldInvokeClassHandler() throws Exception {
        Class<?> exampleClass = loadClass(ExampleClass.class);
        Method normalMethod = exampleClass.getMethod("normalMethod", String.class, int.class);

        Object exampleInstance = exampleClass.newInstance();
        assertEquals("response from methodInvoked: ExampleClass.normalMethod(java.lang.String value1, int 123)",
                normalMethod.invoke(exampleInstance, "value1", 123));
        transcript.assertEventsSoFar("methodInvoked: ExampleClass.__constructor__()",
                "methodInvoked: ExampleClass.normalMethod(java.lang.String value1, int 123)");
    }

    @Test public void shouldGenerateClassSpecificDirectAccessMethod() throws Exception {
        Class<?> exampleClass = loadClass(ExampleClass.class);
        String methodName = RobolectricInternals.directMethodName(ExampleClass.class.getName(), "normalMethod");
        Method directMethod = exampleClass.getDeclaredMethod(methodName, String.class, int.class);
        directMethod.setAccessible(true);
        Object exampleInstance = exampleClass.newInstance();
        assertEquals("normalMethod(value1, 123)", directMethod.invoke(exampleInstance, "value1", 123));
        transcript.assertEventsSoFar("methodInvoked: ExampleClass.__constructor__()");
    }

    @SuppressWarnings("UnusedDeclaration")
    @Instrument
    public static class ExampleClass {
        static int foo = 123;

        public String normalMethod(String stringArg, int intArg) {
            return "normalMethod(" + stringArg + ", " + intArg + ")";
        }

        //        abstract void abstractMethod(); todo
    }

    @Test
    public void callingStaticMethodShouldInvokeClassHandler() throws Exception {
        Class<?> exampleClass = loadClass(ClassWithStaticMethod.class);
        Method normalMethod = exampleClass.getMethod("staticMethod", String.class);

        assertEquals("response from methodInvoked: ClassWithStaticMethod.staticMethod(java.lang.String value1)",
                normalMethod.invoke(null, "value1"));
        transcript.assertEventsSoFar("methodInvoked: ClassWithStaticMethod.staticMethod(java.lang.String value1)");
    }

    @Test
    public void callingStaticDirectAccessMethodShouldWork() throws Exception {
        Class<?> exampleClass = loadClass(ClassWithStaticMethod.class);
        String methodName = RobolectricInternals.directMethodName(
            ClassWithStaticMethod.class.getName(), "staticMethod");
        Method directMethod = exampleClass.getDeclaredMethod(methodName, String.class);

        assertEquals("staticMethod(value1)", directMethod.invoke(null, "value1"));
    }

    @SuppressWarnings("UnusedDeclaration")
    @Instrument
    public static class ClassWithStaticMethod {
        public static String staticMethod(String stringArg) {
            return "staticMethod(" + stringArg + ")";
        }
    }

    @Test
    public void callingNormalMethodReturningIntegerShouldInvokeClassHandler() throws Exception {
        Class<?> exampleClass = loadClass(ClassWithMethodReturningInteger.class);
        classHandler.valueToReturn = 456;

        Method normalMethod = exampleClass.getMethod("normalMethodReturningInteger", int.class);
        Object exampleInstance = exampleClass.newInstance();
        assertEquals(456, normalMethod.invoke(exampleInstance, 123));
        transcript.assertEventsSoFar("methodInvoked: ClassWithMethodReturningInteger.__constructor__()",
                "methodInvoked: ClassWithMethodReturningInteger.normalMethodReturningInteger(int 123)");
    }
    
    @Test
    public void whenClassHandlerReturnsNull_callingNormalMethodReturningIntegerShouldWork() throws Exception {
        Class<?> exampleClass = loadClass(ClassWithMethodReturningInteger.class);
        classHandler.valueToReturn = null;

        Method normalMethod = exampleClass.getMethod("normalMethodReturningInteger", int.class);
        Object exampleInstance = exampleClass.newInstance();
        assertEquals(0, normalMethod.invoke(exampleInstance, 123));
        transcript.assertEventsSoFar("methodInvoked: ClassWithMethodReturningInteger.__constructor__()",
                "methodInvoked: ClassWithMethodReturningInteger.normalMethodReturningInteger(int 123)");
    }

    @SuppressWarnings("UnusedDeclaration")
    @Instrument
    public static class ClassWithMethodReturningInteger {
        public int normalMethodReturningInteger(int intArg) {
            return intArg + 1;
        }
    }
    
    @Test
    public void callingMethodReturningDoubleShouldInvokeClassHandler() throws Exception {
        Class<?> exampleClass = loadClass(ClassWithMethodReturningDouble.class);
        classHandler.valueToReturn = 456;

        Method normalMethod = exampleClass.getMethod("normalMethodReturningDouble", double.class);
        Object exampleInstance = exampleClass.newInstance();
        assertEquals(456.0, normalMethod.invoke(exampleInstance, 123d));
        transcript.assertEventsSoFar("methodInvoked: ClassWithMethodReturningDouble.__constructor__()",
                "methodInvoked: ClassWithMethodReturningDouble.normalMethodReturningDouble(double 123.0)");
    }

    @SuppressWarnings("UnusedDeclaration")
    @Instrument
    public static class ClassWithMethodReturningDouble {
        public double normalMethodReturningDouble(double doubleArg) {
            return doubleArg + 1;
        }
    }

    @Test
    public void callingNativeMethodShouldInvokeClassHandler() throws Exception {
        Class<?> exampleClass = loadClass(ClassWithNativeMethod.class);
        Method normalMethod = exampleClass.getDeclaredMethod("nativeMethod", String.class, int.class);
        Object exampleInstance = exampleClass.newInstance();
        assertEquals("response from methodInvoked: ClassWithNativeMethod.nativeMethod(java.lang.String value1, int 123)",
                normalMethod.invoke(exampleInstance, "value1", 123));
        transcript.assertEventsSoFar("methodInvoked: ClassWithNativeMethod.__constructor__()",
                "methodInvoked: ClassWithNativeMethod.nativeMethod(java.lang.String value1, int 123)");
    }

    @SuppressWarnings("UnusedDeclaration")
    @Instrument
    public static class ClassWithNativeMethod {
        public native String nativeMethod(String stringArg, int intArg);
    }

    @Test public void shouldHandleMethodsReturningBoolean() throws Exception {
        Class<?> exampleClass = loadClass(ClassWithMethodReturningBoolean.class);
        classHandler.valueToReturn = true;

        Method directMethod = exampleClass.getMethod("normalMethodReturningBoolean", boolean.class, boolean[].class);
        directMethod.setAccessible(true);
        Object exampleInstance = exampleClass.newInstance();
        assertEquals(true, directMethod.invoke(exampleInstance, true, new boolean[0]));
        transcript.assertEventsSoFar("methodInvoked: ClassWithMethodReturningBoolean.__constructor__()",
                "methodInvoked: ClassWithMethodReturningBoolean.normalMethodReturningBoolean(boolean true, boolean[] {})");
    }

    @SuppressWarnings("UnusedDeclaration")
    @Instrument
    public static class ClassWithMethodReturningBoolean {
        public boolean normalMethodReturningBoolean(boolean boolArg, boolean[] boolArrayArg) {
            return true;
        }
    }

    @Test public void shouldHandleMethodsReturningArray() throws Exception {
        Class<?> exampleClass = loadClass(ClassWithMethodReturningArray.class);
        classHandler.valueToReturn = new String[] { "miao, mieuw" };

        Method directMethod = exampleClass.getMethod("normalMethodReturningArray");
        directMethod.setAccessible(true);
        Object exampleInstance = exampleClass.newInstance();
        transcript.assertEventsSoFar("methodInvoked: ClassWithMethodReturningArray.__constructor__()");
        assertArrayEquals(new String[]{"miao, mieuw"}, (String[]) directMethod.invoke(exampleInstance));
        transcript.assertEventsSoFar("methodInvoked: ClassWithMethodReturningArray.normalMethodReturningArray()");
    }

    @SuppressWarnings("UnusedDeclaration")
    @Instrument
    public static class ClassWithMethodReturningArray {
        public String[] normalMethodReturningArray() {
            return new String[] { "hello, working!" };
        }
    }

    @Test public void shouldInvokeShadowForEachConstructorInInheritanceTree() throws Exception {
        loadClass(Child.class).newInstance();
        transcript.assertEventsSoFar(
                "methodInvoked: Grandparent.__constructor__()",
                "methodInvoked: Parent.__constructor__()",
                "methodInvoked: Child.__constructor__()");
    }

    @Instrument
    public static class Child extends Parent {
    }

    @Instrument
    public static class Parent extends Grandparent {
    }

    @Instrument
    public static class Grandparent {
    }

    @Test public void shouldRetainSuperCallInConstructor() throws Exception {
        Class<?> aClass = loadClass(InstrumentedChild.class);
        Object o = aClass.getDeclaredConstructor(String.class).newInstance("hortense");
        assertEquals("HORTENSE's child", aClass.getSuperclass().getDeclaredField("parentName").get(o));
        assertNull(aClass.getDeclaredField("childName").get(o));
    }

    @Instrument
    public static class InstrumentedChild extends UninstrumentedParent {
        public final String childName;

        public InstrumentedChild(String name) {
            super(name.toUpperCase() + "'s child");
            this.childName = name;
        }
    }

    @DoNotInstrument
    public static class UninstrumentedParent {
        public final String parentName;

        public UninstrumentedParent(String name) {
            this.parentName = name;
        }

        @Override
        public String toString() {
            return "UninstrumentedParent{parentName='" + parentName + '\'' + '}';
        }
    }

    @Test public void shouldCorrectlySplitStaticPrepFromConstructorChaining() throws Exception {
        Class<?> aClass = loadClass(ClassWithFunnyConstructors.class);
        Object o = aClass.getDeclaredConstructor(String.class).newInstance("hortense");
        transcript.assertEventsSoFar(
                "methodInvoked: ClassWithFunnyConstructors.__constructor__(org.robolectric.bytecode.InstrumentingClassLoaderTest$UninstrumentedParent UninstrumentedParent{parentName='hortense'}, java.lang.String foo)",
                "methodInvoked: ClassWithFunnyConstructors.__constructor__(java.lang.String hortense)");

        // should not run constructor bodies...
        assertEquals(null, getDeclaredFieldValue(aClass, o, "name"));
        assertEquals(null, getDeclaredFieldValue(aClass, o, "uninstrumentedParent"));
    }

    @Test public void shouldGenerateClassSpecificDirectAccessMethodForConstructorWhichDoesNotCallSuper() throws Exception {
        Class<?> aClass = loadClass(ClassWithFunnyConstructors.class);
        Object instance = aClass.getConstructor(String.class).newInstance("horace");
        transcript.assertEventsSoFar(
                "methodInvoked: ClassWithFunnyConstructors.__constructor__(org.robolectric.bytecode.InstrumentingClassLoaderTest$UninstrumentedParent UninstrumentedParent{parentName='horace'}, java.lang.String foo)",
                "methodInvoked: ClassWithFunnyConstructors.__constructor__(java.lang.String horace)");

        // each directly-accessible constructor body will need to be called explicitly, with the correct args...

        Class<?> uninstrumentedParentClass = loadClass(UninstrumentedParent.class);
        Method directMethod = findDirectMethod(aClass, "__constructor__", uninstrumentedParentClass, String.class);
        Object uninstrumentedParentIn = uninstrumentedParentClass.getDeclaredConstructor(String.class).newInstance("hortense");
        assertEquals(null, directMethod.invoke(instance, uninstrumentedParentIn, "foo"));
        transcript.assertNoEventsSoFar();

        assertEquals(null, getDeclaredFieldValue(aClass, instance, "name"));
        Object uninstrumentedParentOut = getDeclaredFieldValue(aClass, instance, "uninstrumentedParent");
        assertEquals("hortense", getDeclaredFieldValue(uninstrumentedParentClass, uninstrumentedParentOut, "parentName"));

        Method directMethod2 = findDirectMethod(aClass, "__constructor__", String.class);
        assertEquals(null, directMethod2.invoke(instance, "hortense"));
        transcript.assertNoEventsSoFar();

        assertEquals("hortense", getDeclaredFieldValue(aClass, instance, "name"));
    }

    private Method findDirectMethod(Class<?> declaringClass, String methodName, Class<?>... argClasses) throws NoSuchMethodException {
        String directMethodName = RobolectricInternals.directMethodName(declaringClass.getName(), methodName);
        Method directMethod = declaringClass.getDeclaredMethod(directMethodName, argClasses);
        directMethod.setAccessible(true);
        return directMethod;
    }

    @SuppressWarnings("UnusedDeclaration")
    @Instrument
    public static class ClassWithFunnyConstructors {
        private final UninstrumentedParent uninstrumentedParent;
        private String name;

        public ClassWithFunnyConstructors(String name) {
            this(new UninstrumentedParent(name), "foo");
            this.name = name;
        }

        public ClassWithFunnyConstructors(UninstrumentedParent uninstrumentedParent, String fooString) {
            this.uninstrumentedParent = uninstrumentedParent;
        }
    }

    @Test public void shouldInstrumentEqualsAndHashCodeAndToStringEvenWhenUndeclared() throws Exception {
        Class<?> theClass = loadClass(ClassWithoutEqualsHashCodeToString.class);
        Object instance = theClass.newInstance();
        transcript.assertEventsSoFar("methodInvoked: ClassWithoutEqualsHashCodeToString.__constructor__()");

        instance.toString();
        transcript.assertEventsSoFar("methodInvoked: ClassWithoutEqualsHashCodeToString.toString()");

        classHandler.valueToReturn = true;
        //noinspection ResultOfMethodCallIgnored,ObjectEqualsNull
        instance.equals(null);
        transcript.assertEventsSoFar("methodInvoked: ClassWithoutEqualsHashCodeToString.equals(java.lang.Object null)");

        classHandler.valueToReturn = 42;
        //noinspection ResultOfMethodCallIgnored
        instance.hashCode();
        transcript.assertEventsSoFar("methodInvoked: ClassWithoutEqualsHashCodeToString.hashCode()");
    }

    @Instrument
    public static class ClassWithoutEqualsHashCodeToString {
    }

    @Test public void shouldAlsoInstrumentEqualsAndHashCodeAndToStringWhenDeclared() throws Exception {
        Class<?> theClass = loadClass(ClassWithEqualsHashCodeToString.class);
        Object instance = theClass.newInstance();
        transcript.assertEventsSoFar("methodInvoked: ClassWithEqualsHashCodeToString.__constructor__()");

        instance.toString();
        transcript.assertEventsSoFar("methodInvoked: ClassWithEqualsHashCodeToString.toString()");

        classHandler.valueToReturn = true;
        //noinspection ResultOfMethodCallIgnored,ObjectEqualsNull
        instance.equals(null);
        transcript.assertEventsSoFar("methodInvoked: ClassWithEqualsHashCodeToString.equals(java.lang.Object null)");

        classHandler.valueToReturn = 42;
        //noinspection ResultOfMethodCallIgnored
        instance.hashCode();
        transcript.assertEventsSoFar("methodInvoked: ClassWithEqualsHashCodeToString.hashCode()");
    }

    @Instrument
    public static class ClassWithEqualsHashCodeToString {
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object obj) {
            return true;
        }

        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public String toString() {
            return "baaaaaah";
        }
    }

    @Test public void shouldProperlyCallSuperWhenForcingDeclarationOfEqualsHashCodeToString() throws Exception {
        Class<?> theClass = loadClass(InstrumentedClassWithoutToStringWithSuperToString.class);
        Object instance = theClass.newInstance();
        transcript.assertEventsSoFar("methodInvoked: InstrumentedClassWithoutToStringWithSuperToString.__constructor__()");

        instance.toString();
        transcript.assertEventsSoFar("methodInvoked: InstrumentedClassWithoutToStringWithSuperToString.toString()");

        assertEquals("baaaaaah", findDirectMethod(theClass, "toString").invoke(instance));
    }

    public static class UninstrumentedClassWithToString {
        @Override
        public String toString() {
            return "baaaaaah";
        }
    }

    @Instrument
    public static class InstrumentedClassWithoutToStringWithSuperToString extends UninstrumentedClassWithToString {
    }

    @Test public void directMethodName_shouldGetSimpleName() throws Exception {
        assertEquals("$$robo$$SomeName_5c63_method", RobolectricInternals.directMethodName("a.b.c.SomeName", "method"));
        assertEquals("$$robo$$SomeName_3b43_method", RobolectricInternals.directMethodName("a.b.c.SomeClass$SomeName", "method"));
    }

    @Test public void shouldWorkWithEnums() throws Exception {
        loadClass(SomeEnum.class);
    }

    @Instrument
    public static enum SomeEnum {
        ONE, TWO, MANY
    }

    /////////////////////////////

    private Object getDeclaredFieldValue(Class<?> aClass, Object o, String fieldName) throws Exception {
        Field field = aClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(o);
    }

    public static class MyClassHandler implements ClassHandler {
        private static Object GENERATE_YOUR_OWN_VALUE = new Object();
        private Transcript transcript;
        private Object valueToReturn = GENERATE_YOUR_OWN_VALUE;

        public MyClassHandler(Transcript transcript) {
            this.transcript = transcript;
        }

        @Override
        public void reset() {
        }

        @Override
        public void classInitializing(Class clazz) {
        }

        @Override
        public Object methodInvoked(Class clazz, String methodName, Object instance, String[] paramTypes, Object[] params) throws Throwable {
            StringBuilder buf = new StringBuilder();
            buf.append("methodInvoked: ").append(clazz.getSimpleName()).append(".").append(methodName).append("(");
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) buf.append(", ");
                Object param = params[i];
                Object display = param == null ? "null" : param.getClass().isArray() ? "{}" : param;
                buf.append(paramTypes[i]).append(" ").append(display);
            }
            buf.append(")");
            transcript.add(buf.toString());

            if (valueToReturn != GENERATE_YOUR_OWN_VALUE) return valueToReturn;
            return "response from " + buf.toString();
        }

        @Override
        public Object intercept(Class clazz, String methodName, Object instance, Object[] paramTypes, Object[] params) throws Throwable {
            return null;
        }

        @Override
        public void setStrictI18n(boolean strictI18n) {
        }
    }

    private static class MySetup extends Setup {
        private final boolean shouldAcquire;
        private final boolean shouldInstrument;

        private MySetup(boolean shouldAcquire, boolean shouldInstrument) {
            this.shouldAcquire = shouldAcquire;
            this.shouldInstrument = shouldInstrument;
        }

        @Override
        public boolean shouldAcquire(String name) {
            return shouldAcquire && !name.startsWith("java.");
        }

        @Override
        public boolean shouldInstrument(ClassInfo classInfo) {
            return shouldInstrument;
        }
    }

    private Class<?> loadClass(Class<?> clazz) throws ClassNotFoundException {
        return classLoader.loadClass(clazz.getName());
    }

    private static void injectClassHandler(ClassLoader classLoader, ClassHandler classHandler) {
        try {
            Field field = classLoader.loadClass(RobolectricInternals.class.getName()).getDeclaredField("classHandler");
            field.setAccessible(true);
            field.set(null, classHandler);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
