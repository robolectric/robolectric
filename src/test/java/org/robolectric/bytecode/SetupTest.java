package org.robolectric.bytecode;

import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.util.Function;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
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

    @Test
    public void getInterceptionHandler_whenCallIsNotRecognized_shouldReturnDoNothingHandler() throws Exception {
        Function<Object,Object> handler = setup.getInterceptionHandler("unrecognizedClass", "nonMethod");

        assertThat(handler)
                .isNotNull()
                .isSameAs(Setup.DO_NOTHING_HANDLER);
        assertThat(handler.call(null)).isNull();
    }

    @Test
    public void getInterceptionHandler_whenInterceptingElderOnLinkedHashMap_shouldReturnNonDoNothingHandler() throws Exception {
        Function<Object,Object> handler = setup.getInterceptionHandler("java/util/LinkedHashMap", "eldest");

        assertThat(handler).isNotSameAs(Setup.DO_NOTHING_HANDLER);
    }

    @Test
    public void elderOnLinkedHashMapHandler_shouldReturnEldestMemberOfLinkedHashMap() throws Exception {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>(2);
        map.put(1, "one");
        map.put(2, "two");

        Function<Object,Object> handler = setup.getInterceptionHandler("java/util/LinkedHashMap", "eldest");

        Map.Entry<Integer, String> eldestMember = map.entrySet().iterator().next();
        assertThat(handler.call(map)).isEqualTo(eldestMember);
        assertThat(eldestMember.getKey()).isEqualTo(1);
        assertThat(eldestMember.getValue()).isEqualTo("one");
    }
}
