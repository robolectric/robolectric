package org.robolectric.android.controller;

import android.app.backup.BackupAgent;
import android.content.Context;

import org.robolectric.RuntimeEnvironment;

public class BackupAgentController<T extends BackupAgent> {
  private T backupAgent;

  private BackupAgentController(T backupAgent) {
    this.backupAgent = backupAgent;
  }

  public static <T extends BackupAgent> BackupAgentController<T> of(T backupAgent) {
    return new BackupAgentController<>(backupAgent);
  }

  /**
   * Sets up the {@link BackupAgent} with the Runtime.environment attached as a base context.
   */
  public BackupAgentController<T> setUp() {
    Context baseContext = RuntimeEnvironment.application.getBaseContext();
    backupAgent.attach(baseContext);
    return this;
  }

  public T get() {
    return backupAgent;
  }
}
