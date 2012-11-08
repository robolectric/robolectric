package com.xtremelabs.robolectric.shadows;

import android.app.ActivityManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

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
    private ActivityManager.MemoryInfo memoryInfo;
    private int memoryClass;

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

    @Implementation
    public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) {
        if (memoryInfo != null) {
            outInfo.lowMemory = memoryInfo.lowMemory;
        }
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

    public void setMemoryInfo(ActivityManager.MemoryInfo memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    @Implementation
    public int getMemoryClass() {
        return memoryClass;
    }

    public void setMemoryClass(int memoryClass) {
        this.memoryClass = memoryClass;
    }

    @Implements(ActivityManager.MemoryInfo.class)
    public static class ShadowMemoryInfo {
        public boolean lowMemory;

        public void setLowMemory(boolean lowMemory) {
            this.lowMemory = lowMemory;
        }
    }
}
