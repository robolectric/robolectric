package org.robolectric.shadows;

import android.app.TaskInfo;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow for {@link TaskInfo} */
@Implements(value = TaskInfo.class)
public class ShadowTaskInfo {

  @RealObject private TaskInfo realTaskInfo;

  /**
   * For tests, sets the visibility of the real {@link TaskInfo}, which is returned by {@link
   * TaskInfo#isVisible()}.
   */
  public void setIsVisible(boolean isVisible) {
    realTaskInfo.isVisible = isVisible;
  }
}
