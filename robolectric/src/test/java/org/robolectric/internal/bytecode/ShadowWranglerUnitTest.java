package org.robolectric.internal.bytecode;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.internal.SdkConfig;
import org.robolectric.util.Function;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class ShadowWranglerUnitTest {
  private ShadowWrangler shadowWrangler;

  @Before
  public void setup() throws Exception {
    shadowWrangler = new ShadowWrangler(ShadowMap.EMPTY);
  }

  @Test
  public void getInterceptionHandler_whenCallIsNotRecognized_shouldReturnDoNothingHandler() throws Exception {
    MethodSignature methodSignature = MethodSignature.parse("java/lang/Object/unknownMethod()V");
    Function<Object,Object> handler = shadowWrangler.getInterceptionHandler(methodSignature);

    assertThat(handler.call(null, null, new Object[0])).isNull();
  }

  @Test
  public void getInterceptionHandler_whenInterceptingElderOnLinkedHashMap_shouldReturnNonDoNothingHandler() throws Exception {
    MethodSignature methodSignature = MethodSignature.parse("java/util/LinkedHashMap/eldest()Ljava/lang/Object;");
    Function<Object,Object> handler = shadowWrangler.getInterceptionHandler(methodSignature);

    assertThat(handler).isNotSameAs(ShadowWrangler.DO_NOTHING_HANDLER);
  }

  @Test
  public void intercept_elderOnLinkedHashMapHandler_shouldReturnEldestMemberOfLinkedHashMap() throws Throwable {
    LinkedHashMap<Integer, String> map = new LinkedHashMap<>(2);
    map.put(1, "one");
    map.put(2, "two");

    Map.Entry<Integer, String> result = (Map.Entry<Integer, String>)
        shadowWrangler.intercept("java/util/LinkedHashMap/eldest()Ljava/lang/Object;", map, null, getClass());

    Map.Entry<Integer, String> eldestMember = map.entrySet().iterator().next();
    assertThat(result).isEqualTo(eldestMember);
    assertThat(result.getKey()).isEqualTo(1);
    assertThat(result.getValue()).isEqualTo("one");
  }

  @Test
  public void intercept_elderOnLinkedHashMapHandler_shouldReturnNullForEmptyMap() throws Throwable {
    LinkedHashMap<Integer, String> map = new LinkedHashMap<>();

    Map.Entry<Integer, String> result = (Map.Entry<Integer, String>)
        shadowWrangler.intercept("java/util/LinkedHashMap/eldest()Ljava/lang/Object;", map, null, getClass());

    assertThat(result).isNull();
  }
}
