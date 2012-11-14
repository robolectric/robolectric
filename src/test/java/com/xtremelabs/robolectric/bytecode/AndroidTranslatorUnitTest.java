package com.xtremelabs.robolectric.bytecode;

import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AndroidTranslatorUnitTest {
    private ClassPool classPool;

    @Before public void setUp() throws Exception {
        classPool = new ClassPool(true);
    }

    @Test
    public void testOnLoadWithNonInstrumentedClass() throws Exception {
        ClassHandler handler = mock(ClassHandler.class);
        ClassCache cache = mock(ClassCache.class);

        AndroidTranslator translator = new AndroidTranslator(handler, cache, new Setup());

        translator.onLoad(classPool, "java.lang.Object");
        verify(cache).isWriting();
        verifyNoMoreInteractions(cache);
        verifyZeroInteractions(handler);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateIfClassCacheIsWriting() throws Exception {
        ClassCache cache = mock(ClassCache.class);
        when(cache.isWriting()).thenReturn(true);
        new AndroidTranslator(null, cache, new Setup()).onLoad(classPool, "java.lang.Object");
    }
}
