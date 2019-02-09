package org.robolectric.junit.rules;

import android.os.Looper;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowBaseLooper;
import org.robolectric.shadows.ShadowRealisticMessageQueue;

public class LooperStateDiagnosingRule implements TestRule {

  @Override
  public Statement apply(Statement base, Description description) {
    if (ShadowBaseLooper.useRealisticLooper()) {
      return new LooperDiagnosingStatement(base);
    } else {
      return base;
    }
  }

  private static class LooperDiagnosingStatement extends Statement {

    private final Statement baseStatement;

    LooperDiagnosingStatement(Statement base) {
      this.baseStatement = base;
    }

    @Override
    public void evaluate() throws Throwable {
      try {
        baseStatement.evaluate();
      }
      catch (Throwable t) {
        ShadowRealisticMessageQueue shadowRealisticMessageQueue = Shadow.extract(Looper.getMainLooper().getQueue());
        if (!shadowRealisticMessageQueue.isIdle()) {
          throw new Exception("Main thread has queued unexecuted runnables. " +
              "This might be the cause of the test failure. " +
              "You might need a ShadowBaseLooper#isIdle call.",
              t );
        }
        throw t;
      }
    }
  }
}
