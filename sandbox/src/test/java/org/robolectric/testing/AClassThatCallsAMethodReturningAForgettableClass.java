package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassThatCallsAMethodReturningAForgettableClass {
  public void callSomeMethod() {
    @SuppressWarnings("unused")
    AClassToForget forgettableClass = getAForgettableClass();
  }

  public AClassToForget getAForgettableClass() {
    throw new RuntimeException("should never be called!");
  }
}
