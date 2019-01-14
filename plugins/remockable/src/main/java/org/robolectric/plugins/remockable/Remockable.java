package org.robolectric.plugins.remockable;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.mockito.internal.creation.DelegatingMethod;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.invocation.InterceptedInvocation;
import org.mockito.internal.progress.SequenceNumber;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.MockHandler;

public class Remockable {

  @SuppressWarnings("NewApi")
  public static <T> T mockOf(T object) {
    try {
      Class<?> clazz = object.getClass();
      Field mockField = clazz.getDeclaredField(RemockableDecorator.MOCK_FIELD_NAME);
      mockField.setAccessible(true);
      Object mockObj = mockField.get(object);
      if (mockObj == null) {
        mockObj = mock(clazz);
        mockField.set(object, mockObj);
      }
      return (T) mockObj;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  public static Object checkMock(boolean[] handled, Object mock, Object o,
      Class<?> clazz, String methodName, Class<?>[] argTypes, Object[] args) throws Throwable {
    MockHandler<Object> mockHandler = MockUtil.getMockHandler(mock);
    InvocationContainerImpl invocationContainer =
        (InvocationContainerImpl) mockHandler.getInvocationContainer();

    Method method = clazz.getMethod(methodName, argTypes);

    Invocation invocation = new InterceptedInvocation(() -> mock, new DelegatingMethod(method),
        args, null, new LocationImpl(), SequenceNumber.next());

    StubbedInvocationMatcher stubbedInvocation = invocationContainer.findAnswerFor(invocation);
    if (stubbedInvocation != null) {
      handled[0] = true;
      stubbedInvocation.captureArgumentsFrom(invocation);
      return stubbedInvocation.answer(invocation);
    }

    return null;
  }

}
