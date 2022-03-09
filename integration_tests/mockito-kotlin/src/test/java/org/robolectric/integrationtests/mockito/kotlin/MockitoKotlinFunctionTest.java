package org.robolectric.integrationtests.mockito.kotlin;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

import kotlin.Function;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

/** Tests for mocking Kotlin classes with Mockito (in Kotlin code). */
@RunWith(RobolectricTestRunner.class)
public class MockitoKotlinFunctionTest {
  @Test
  public void testFunctionMock() {
    Function function = Mockito.mock(Function.class);
    assertThat(function).isNotNull();
  }

  @Test
  public void testFunction0Mock() {
    Function0 function = Mockito.mock(Function0.class);
    doReturn(null).when(function).invoke();
    Object retVal = function.invoke();
    assertThat(retVal).isNull();
  }

  @Test
  public void testFunction1Mock() {
    Function1 function = Mockito.mock(Function1.class);
    doReturn(null).when(function).invoke(any());
    Object retVal = function.invoke(null);
    assertThat(retVal).isNull();
  }
}
