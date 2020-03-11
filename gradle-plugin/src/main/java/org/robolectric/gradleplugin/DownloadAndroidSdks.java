package org.robolectric.gradleplugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class DownloadAndroidSdks extends DefaultTask {

  @TaskAction
  public void run() {
    System.out.println("DownloadAndroidSdks! " + getProject());
  }
}
