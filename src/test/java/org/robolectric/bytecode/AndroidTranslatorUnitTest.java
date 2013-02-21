package org.robolectric.bytecode;

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
        ClassCache cache = mock(ZipClassCache.class);

        AndroidTranslator translator = new AndroidTranslator(cache, new Setup());

        translator.onLoad(classPool, "java.lang.Object");
        verify(cache).isWriting();
        verifyNoMoreInteractions(cache);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateIfClassCacheIsWriting() throws Exception {
        ClassCache cache = mock(ZipClassCache.class);
        when(cache.isWriting()).thenReturn(true);
        new AndroidTranslator(cache, new Setup()).onLoad(classPool, "java.lang.Object");
    }
}
