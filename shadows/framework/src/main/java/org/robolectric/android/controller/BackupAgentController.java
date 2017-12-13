package org.robolectric.android.controller;

import android.app.backup.BackupAgent;
import android.content.Context;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

public class BackupAgentController<T extends BackupAgent> extends
    ComponentController<BackupAgentController<T>, T> {
  private BackupAgentController(T backupAgent) {
    super(backupAgent);
  }

  public static <T extends BackupAgent> BackupAgentController<T> of(T backupAgent) {
    return new BackupAgentController<>(backupAgent).attach();
  }

  private BackupAgentController<T> attach() {
    if (attached) {
      return this;
    }
    Context baseContext = RuntimeEnvironment.application.getBaseContext();
    ReflectionHelpers.callInstanceMethod(BackupAgent.class, component, "attach",
        ReflectionHelpers.ClassParameter.from(Context.class, baseContext));
    return this;
  }

  @Override
  public BackupAgentController<T> create() {
    invokeWhilePaused("onCreate");
    return this;
  }

  @Override
  public BackupAgentController<T> destroy() {
    invokeWhilePaused("onDestroy");
    return this;
  }
}
