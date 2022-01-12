package org.robolectric.junit.rules;

import static org.junit.Assert.assertThrows;

import dalvik.system.CloseGuard;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link CloseGuardRule}. */
@RunWith(RobolectricTestRunner.class)
public final class CloseGuardRuleTest {

  @Test
  public void noCloseGuards_doesNotFail() throws Throwable {
    new CloseGuardRule()
        .apply(
            new Statement() {
              @Override
              public void evaluate() {
                // No CloseGuards used
              }
            },
            Description.EMPTY)
        .evaluate();
  }

  @Test
  public void allCloseGuardsClosed_doesNotFail() throws Throwable {
    new CloseGuardRule()
        .apply(
            new Statement() {
              @Override
              public void evaluate() {
                CloseGuard closeGuard1 = CloseGuard.get();
                CloseGuard closeGuard2 = CloseGuard.get();
                closeGuard1.open("foo");
                closeGuard2.open("bar");

                closeGuard1.close();
                closeGuard2.close();
              }
            },
            Description.EMPTY)
        .evaluate();
  }

  @Test
  public void closeGuardsOpen_throwsException() {
    assertThrows(
        MultipleFailureException.class,
        () ->
            new CloseGuardRule()
                .apply(
                    new Statement() {
                      @Override
                      public void evaluate() {
                        CloseGuard closeGuard1 = CloseGuard.get();
                        CloseGuard closeGuard2 = CloseGuard.get();

                        closeGuard1.open("foo");
                        closeGuard2.open("bar");
                      }
                    },
                    Description.EMPTY)
                .evaluate());
  }
}
