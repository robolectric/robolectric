package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.android.AndroidSdkShadowMatcher;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.interceptors.AndroidInterceptors;
import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.util.Function;

@SuppressWarnings("unchecked")
@RunWith(JUnit4.class)
public class ShadowWranglerUnitTest {
  private ShadowWrangler shadowWrangler;
  private Interceptors interceptors;
  private ShadowMatcher sdk18 = new AndroidSdkShadowMatcher(18);
  private ShadowMatcher sdk19 = new AndroidSdkShadowMatcher(19);
  private ShadowMatcher sdk20 = new AndroidSdkShadowMatcher(20);
  private ShadowMatcher sdk21 = new AndroidSdkShadowMatcher(21);
  private ShadowMatcher sdk22 = new AndroidSdkShadowMatcher(22);
  private ShadowMatcher sdk23 = new AndroidSdkShadowMatcher(23);

  @Before
  public void setup() throws Exception {
    interceptors = new Interceptors(AndroidInterceptors.all());
    shadowWrangler = new ShadowWrangler(ShadowMap.EMPTY, sdk23, interceptors);
  }

  @Test
  public void getInterceptionHandler_whenCallIsNotRecognized_shouldReturnDoNothingHandler()
      throws Exception {
    MethodSignature methodSignature = MethodSignature.parse("java/lang/Object/unknownMethod()V");
    Function<Object, Object> handler = interceptors.getInterceptionHandler(methodSignature);

    assertThat(handler.call(null, null, new Object[0])).isNull();
  }

  @Test
  public void
      getInterceptionHandler_whenInterceptingElderOnLinkedHashMap_shouldReturnNonDoNothingHandler()
          throws Exception {
    MethodSignature methodSignature =
        MethodSignature.parse("java/util/LinkedHashMap/eldest()Ljava/lang/Object;");
    Function<Object, Object> handler = interceptors.getInterceptionHandler(methodSignature);

    assertThat(handler).isNotSameInstanceAs(ShadowWrangler.DO_NOTHING_HANDLER);
  }

  @Test
  public void intercept_elderOnLinkedHashMapHandler_shouldReturnEldestMemberOfLinkedHashMap()
      throws Throwable {
    LinkedHashMap<Integer, String> map = new LinkedHashMap<>(2);
    map.put(1, "one");
    map.put(2, "two");

    Map.Entry<Integer, String> result =
        (Map.Entry<Integer, String>)
            shadowWrangler.intercept(
                "java/util/LinkedHashMap/eldest()Ljava/lang/Object;", map, null, getClass());

    Map.Entry<Integer, String> eldestMember = map.entrySet().iterator().next();
    assertThat(result).isEqualTo(eldestMember);
    assertThat(result.getKey()).isEqualTo(1);
    assertThat(result.getValue()).isEqualTo("one");
  }

  @Test
  public void intercept_elderOnLinkedHashMapHandler_shouldReturnNullForEmptyMap() throws Throwable {
    LinkedHashMap<Integer, String> map = new LinkedHashMap<>();

    Map.Entry<Integer, String> result =
        (Map.Entry<Integer, String>)
            shadowWrangler.intercept(
                "java/util/LinkedHashMap/eldest()Ljava/lang/Object;", map, null, getClass());

    assertThat(result).isNull();
  }

  @Test
  public void whenChildShadowHasNarrowerSdk_createShadowFor_shouldReturnSuperShadowSometimes()
      throws Exception {
    ShadowMap shadowMap =
        new ShadowMap.Builder()
            .addShadowClasses(ShadowDummyClass.class, ShadowChildOfDummyClass.class)
            .build();
    assertThat(
            new ShadowWrangler(shadowMap, sdk18, interceptors)
                .createShadowFor(new ChildOfDummyClass()))
        .isSameInstanceAs(ShadowWrangler.NO_SHADOW);
    assertThat(
            new ShadowWrangler(shadowMap, sdk19, interceptors)
                .createShadowFor(new ChildOfDummyClass()))
        .isInstanceOf(ShadowDummyClass.class);
    assertThat(
            new ShadowWrangler(shadowMap, sdk20, interceptors)
                .createShadowFor(new ChildOfDummyClass()))
        .isInstanceOf(ShadowChildOfDummyClass.class);
    assertThat(
            new ShadowWrangler(shadowMap, sdk21, interceptors)
                .createShadowFor(new ChildOfDummyClass()))
        .isInstanceOf(ShadowChildOfDummyClass.class);
    assertThat(
            new ShadowWrangler(shadowMap, sdk22, interceptors)
                .createShadowFor(new ChildOfDummyClass()))
        .isSameInstanceAs(ShadowWrangler.NO_SHADOW);
  }

  public static class DummyClass {}

  @Implements(value = DummyClass.class, minSdk = 19, maxSdk = 21)
  public static class ShadowDummyClass {
    @Implementation(minSdk = 20, maxSdk = 20)
    protected void __constructor__() {}

    @Implementation
    protected void methodWithoutRange() {}

    @Implementation(minSdk = 20, maxSdk = 20)
    protected void methodFor20() {}

    @Implementation(minSdk = 20)
    protected void methodMin20() {}

    @Implementation(maxSdk = 20)
    protected void methodMax20() {}
  }

  public static class ChildOfDummyClass extends DummyClass {}

  @Implements(value = ChildOfDummyClass.class, minSdk = 20, maxSdk = 21)
  public static class ShadowChildOfDummyClass {
    @Implementation
    protected void methodWithoutRange() {}
  }
}
