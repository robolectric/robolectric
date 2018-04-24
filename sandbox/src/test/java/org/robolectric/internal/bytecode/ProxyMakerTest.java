package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ProxyMakerTest {
  private static final ProxyMaker.MethodMapper IDENTITY_NAME = new ProxyMaker.MethodMapper() {
    @Override public String getName(String className, String methodName) {
      return methodName;
    }
  };

  @Test
  public void proxyCall() {
    ProxyMaker maker = new ProxyMaker(IDENTITY_NAME);

    Thing mock = mock(Thing.class);
    Thing proxy = maker.createProxyFactory(Thing.class).createProxy(Thing.class, mock);
    assertThat(proxy.getClass()).isNotSameAs(Thing.class);

    proxy.returnNothing();
    verify(mock).returnNothing();

    when(mock.returnInt()).thenReturn(42);
    assertThat(proxy.returnInt()).isEqualTo(42);
    verify(mock).returnInt();

    proxy.argument("hello");
    verify(mock).argument("hello");
  }

  @Test
  public void cachesProxyClass() {
    ProxyMaker maker = new ProxyMaker(IDENTITY_NAME);
    Thing thing1 = mock(Thing.class);
    Thing thing2 = mock(Thing.class);

    Thing proxy1 = maker.createProxy(Thing.class, thing1);
    Thing proxy2 = maker.createProxy(Thing.class, thing2);

    assertThat(proxy1.getClass()).isSameAs(proxy2.getClass());
  }

  public static class Thing {
    public Thing() {
      throw new UnsupportedOperationException();
    }

    public void returnNothing() {
      throw new UnsupportedOperationException();
    }

    public int returnInt() {
      throw new UnsupportedOperationException();
    }

    public void argument(String arg) {
      throw new UnsupportedOperationException();
    }
  }
}
