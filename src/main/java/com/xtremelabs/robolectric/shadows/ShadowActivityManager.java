package com.xtremelabs.robolectric.shadows;

import java.util.ArrayList;
import java.util.List;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import android.app.ActivityManager;
import android.content.ComponentName;

/**
 * Shadow for the Android {@code ActivityManager} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(ActivityManager.class)
public class ShadowActivityManager {
	
	private List<ActivityManager.RunningTaskInfo> tasks = 
		new ArrayList<ActivityManager.RunningTaskInfo>();
	
	private List<ActivityManager.RunningAppProcessInfo> processes = 
		new ArrayList<ActivityManager.RunningAppProcessInfo>();
	
	private String backgroundPackage;
	
	@Implementation
	public List<ActivityManager.RunningTaskInfo> getRunningTasks(int maxNum) {
		return tasks;
	}
	
	@Implementation
	public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
		return processes;
	}
	
	@Implementation
	public void killBackgroundProcesses(String packageName) {
		backgroundPackage = packageName;
	}
	
	/**
	 * Non-Android accessor to set the list of running tasks.
	 * @param tasks
	 */
	public void setTasks(List<ActivityManager.RunningTaskInfo> tasks) {
		this.tasks = tasks;
	}
	
	/**
	 * Non-Android accessor to set the list of running processes.
	 * @param processes
	 */
	public void setProcesses( List<ActivityManager.RunningAppProcessInfo> processes ) {
		this.processes = processes;
	}
	
	/**
	 * Non-Android accessor, for use in assertions.
	 */
	public String getBackgroundPackage() {
		return backgroundPackage;
	}
}
