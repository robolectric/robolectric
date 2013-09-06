package org.robolectric.bytecode;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.SdkConfig;
import org.robolectric.util.Function;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

public class ShadowWranglerUnitTest {
  private ShadowWrangler shadowWrangler;

  @Before
  public void setup() throws Exception {
    shadowWrangler = new ShadowWrangler(ShadowMap.EMPTY, SdkConfig.getDefaultSdk());
  }

  @Test
  public void getInterceptionHandler_whenCallIsNotRecognized_shouldReturnDoNothingHandler() throws Exception {
    MethodSignature methodSignature = MethodSignature.parse("java/lang/Object/unknownMethod()V");
    Function<Object,Object> handler = shadowWrangler.getInterceptionHandler(methodSignature);

    assertThat(handler)
        .isNotNull()
        .isSameAs(ShadowWrangler.DO_NOTHING_HANDLER);
    assertThat(handler.call(null, null, new Object[0])).isNull();
  }

  @Test
  public void getInterceptionHandler_whenInterceptingElderOnLinkedHashMap_shouldReturnNonDoNothingHandler() throws Exception {
    MethodSignature methodSignature = MethodSignature.parse("java/util/LinkedHashMap/eldest()Ljava/lang/Object");
    Function<Object,Object> handler = shadowWrangler.getInterceptionHandler(methodSignature);

    assertThat(handler).isNotSameAs(ShadowWrangler.DO_NOTHING_HANDLER);
  }

  @Test
  public void intercept_elderOnLinkedHashMapHandler_shouldReturnEldestMemberOfLinkedHashMap() throws Throwable {
    LinkedHashMap<Integer, String> map = new LinkedHashMap<Integer, String>(2);
    map.put(1, "one");
    map.put(2, "two");

    @SuppressWarnings("unchecked")
    Map.Entry<Integer, String> result = (Map.Entry<Integer, String>)
        shadowWrangler.intercept("java/util/LinkedHashMap/eldest()Ljava/lang/Object;", map, null, getClass());

    Map.Entry<Integer, String> eldestMember = map.entrySet().iterator().next();
    assertThat(result).isEqualTo(eldestMember);
    assertThat(result.getKey()).isEqualTo(1);
    assertThat(result.getValue()).isEqualTo("one");
  }
}
