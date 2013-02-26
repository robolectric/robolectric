package org.robolectric.bytecode;

import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SetupTest {
    private ClassPool classPool;
    private Setup setup;

    @Before
    public void setUp() throws Exception {
        classPool = new ClassPool(true);
        setup = new Setup();
    }

    @Test
    public void shouldInstrumentDefaultRequestDirector() throws Exception {
        assertTrue(setup.shouldInstrument(wrap(classPool.makeClass("org.apache.http.impl.client.DefaultRequestDirector"))));
    }

    @Test
    public void shouldInstrumentGoogleMapsClasses() throws Exception {
        assertTrue(setup.shouldInstrument(wrap(classPool.makeClass("com.google.android.maps.SomeMapsClass"))));
    }

    @Test
    public void shouldNotInstrumentCoreJdkClasses() throws Exception {
        assertFalse(setup.shouldInstrument(wrap(classPool.get("java.lang.Object"))));
        assertFalse(setup.shouldInstrument(wrap(classPool.get("java.lang.String"))));
    }

    @Test
    public void shouldInstrumentAndroidCoreClasses() throws Exception {
        assertTrue(setup.shouldInstrument(wrap(classPool.makeClass("android.content.Intent"))));
        assertTrue(setup.shouldInstrument(wrap(classPool.makeClass("android.and.now.for.something.completely.different"))));
    }

    @Test
    public void shouldNotAcquireRClasses() throws Exception {
        assertTrue(setup.shouldAcquire("com.whatever.Rfoo"));
        assertTrue(setup.shouldAcquire("com.whatever.fooR"));
        assertFalse(setup.shouldAcquire("com.whatever.R"));
        assertFalse(setup.shouldAcquire("com.whatever.R$anything"));
        assertTrue(setup.shouldAcquire("com.whatever.R$anything$else"));
    }

    ClassInfo wrap(CtClass ctClass) {
        return new AndroidTranslator.JavassistClassInfo(ctClass);
    }
}
