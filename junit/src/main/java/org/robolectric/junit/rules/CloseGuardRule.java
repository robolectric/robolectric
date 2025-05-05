package org.robolectric.junit.rules;

import org.junit.rules.Verifier;
import org.junit.runners.model.MultipleFailureException;
import org.robolectric.shadows.ShadowCloseGuard;

/** Rule for failing tests that leave any CloseGuards open. */
public final class CloseGuardRule extends Verifier {

  @Override
  public void verify() throws Throwable {
    MultipleFailureException.assertEmpty(ShadowCloseGuard.getErrors());
  }
}
