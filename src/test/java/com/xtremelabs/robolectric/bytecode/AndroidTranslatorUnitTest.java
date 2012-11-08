package com.xtremelabs.robolectric.bytecode;

import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AndroidTranslatorUnitTest {
    private ClassPool classPool;
    private AndroidTranslator androidTranslator;

    @Before public void setUp() throws Exception {
        classPool = new ClassPool(true);
        androidTranslator = new AndroidTranslator(null, null);
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
