package org.robolectric.bytecode;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.util.Function;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

public class ShadowWranglerUnitTest {
    private ShadowWrangler shadowWrangler;

    @Before
    public void setup() throws Exception {
        shadowWrangler = new ShadowWrangler(null);
    }

    @Test
    public void getInterceptionHandler_whenCallIsNotRecognized_shouldReturnDoNothingHandler() throws Exception {
        Function<Object,Object> handler = shadowWrangler.getInterceptionHandler("unrecognizedClass", "nonMethod");

        assertThat(handler)
                .isNotNull()
                .isSameAs(ShadowWrangler.DO_NOTHING_HANDLER);
        assertThat(handler.call(null)).isNull();
    }

    @Test
    public void getInterceptionHandler_whenInterceptingElderOnLinkedHashMap_shouldReturnNonDoNothingHandler() throws Exception {
        Function<Object,Object> handler = shadowWrangler.getInterceptionHandler("java/util/LinkedHashMap", "eldest");

        assertThat(handler).isNotSameAs(ShadowWrangler.DO_NOTHING_HANDLER);
    }

    @Test
    public void intercept_elderOnLinkedHashMapHandler_shouldReturnEldestMemberOfLinkedHashMap() throws Throwable {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>(2);
        map.put(1, "one");
        map.put(2, "two");

        Map.Entry<Integer, String> result = (Map.Entry<Integer, String>) shadowWrangler.intercept("java/util/LinkedHashMap", "eldest", map, null, null);

        Map.Entry<Integer, String> eldestMember = map.entrySet().iterator().next();
        assertThat(result).isEqualTo(eldestMember);
        assertThat(result.getKey()).isEqualTo(1);
        assertThat(result.getValue()).isEqualTo("one");
    }
}
