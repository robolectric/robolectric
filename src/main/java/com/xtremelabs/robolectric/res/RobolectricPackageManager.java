package com.xtremelabs.robolectric.res;

import java.util.*;
import java.util.Map.Entry;

import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.tester.android.content.pm.StubPackageManager;

public class RobolectricPackageManager extends StubPackageManager {

    private Map<String, PackageInfo> packageList;
    private Map<Intent, List<ResolveInfo>> resolveList = new HashMap<Intent, List<ResolveInfo>>();
    private Map<ComponentName, ComponentState> componentList = new HashMap<ComponentName,ComponentState>();
    private Map<ComponentName, Drawable> drawableList = new HashMap<ComponentName, Drawable>();
    private Map<String, Boolean> systemFeatureList = new HashMap<String, Boolean>();
    private Map<IntentFilter, ComponentName > preferredActivities = new HashMap<IntentFilter, ComponentName>();
    
    private ContextWrapper contextWrapper;
    private RobolectricConfig config;
    private ApplicationInfo applicationInfo;

    public RobolectricPackageManager(ContextWrapper contextWrapper, RobolectricConfig config) {
        this.contextWrapper = contextWrapper;
        this.config = config;
        initializePackageInfo();
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        if (packageList.containsKey(packageName)) {
        	return packageList.get(packageName);
        }

        throw new NameNotFoundException();
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {

        if (config.getPackageName().equals(packageName)) {
            if (applicationInfo == null) {
                applicationInfo = new ApplicationInfo();
                applicationInfo.flags = config.getApplicationFlags();
                applicationInfo.targetSdkVersion = config.getSdkVersion();
                applicationInfo.packageName = config.getPackageName();
                applicationInfo.processName = config.getProcessName();
                applicationInfo.name = config.getApplicationName();
            }
            return applicationInfo;
        }

        PackageInfo info;
        if ((info = packageList.get(packageName)) != null) {
        	return info.applicationInfo;
        }

        throw new NameNotFoundException();
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        return new ArrayList<PackageInfo>(packageList.values());
    }

    @Override
    public List<ResolveInfo> queryIntentActivities( Intent intent, int flags ) {
    	List<ResolveInfo> result = resolveList.get( intent );
    	return (result == null) ? new ArrayList<ResolveInfo>() : result;
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
    	List<ResolveInfo> candidates = queryIntentActivities(intent, flags);
    	return candidates.isEmpty() ? null : candidates.get(0);
    }

    @Override
    public ResolveInfo resolveService(Intent intent, int flags) {
        return resolveActivity(intent, flags);
    }

    public void addResolveInfoForIntent( Intent intent, List<ResolveInfo> info ) {
        resolveList.put(intent, info);
    }

    public void addResolveInfoForIntent(Intent intent, ResolveInfo info) {
        List<ResolveInfo> l = resolveList.get(intent);
        if (l == null) {
            l = new ArrayList<ResolveInfo>();
            resolveList.put(intent, l);
        }
        l.add(info);
    }

    @Override
    public Drawable getActivityIcon(Intent intent) {
    	return drawableList.get(intent.getComponent());
    }

    @Override
    public Drawable getActivityIcon(ComponentName componentName) {
    	return drawableList.get(componentName);
    }

    public void addActivityIcon( ComponentName component, Drawable d ) {
    	drawableList.put( component, d);
    }

    public void addActivityIcon( Intent intent, Drawable d ) {
    	drawableList.put( intent.getComponent(), d);
    }

	@Override
	public Intent getLaunchIntentForPackage(String packageName) {
		Intent i = new Intent();
		i.setComponent( new ComponentName(packageName, "") );
		return i;
	}

	@Override
	public CharSequence getApplicationLabel(ApplicationInfo info) {
		return info.name;
	}

	@Override
	public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
		componentList.put(componentName, new ComponentState(newState, flags));
	}
	
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    	preferredActivities.put(filter, activity);
    }
    
    @Override
    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
    	if( outFilters == null ){ return 0; }
    	
    	Set< IntentFilter> filters = preferredActivities.keySet();
    	for( IntentFilter filter: outFilters ){
    		step:
			for ( IntentFilter testFilter : filters ) {
				ComponentName name = preferredActivities.get( testFilter );
				// filter out based on the given packageName;
				if( packageName != null && !name.getPackageName().equals( packageName ) ){
					continue step;
				}
				
				// Check actions
				Iterator< String > iterator = filter.actionsIterator();
				while ( iterator.hasNext() ) {
					if ( !testFilter.matchAction( iterator.next() ) ) {
						continue step;
					}
				}

				iterator = filter.categoriesIterator();
				while ( iterator.hasNext() ) {
					if ( !filter.hasCategory( iterator.next() ) ) {
						continue step;
					}
				}
				
				if( outActivities == null ){
					outActivities = new ArrayList<ComponentName>();
				}
				
				outActivities.add( name );
			}
    	}
    	
    	return 0;
    }

	/**
	 * Non-Android accessor.  Use to make assertions on values passed to
	 * setComponentEnabledSetting.
	 *
	 * @param componentName
	 * @return
	 */
	public RobolectricPackageManager.ComponentState getComponentState(ComponentName componentName) {
		return componentList.get(componentName);
	}

    /**
     * Non-Android accessor.  Used to add a package to the list of those
     * already 'installed' on system.
     *
     * @param packageInfo
     */
    public void addPackage( PackageInfo packageInfo ) {
    	 packageList.put(packageInfo.packageName, packageInfo);
    }

    public void addPackage( String packageName ) {
    	PackageInfo info = new PackageInfo();
    	info.packageName = packageName;
    	addPackage( info );
    }

    @Override
    public boolean hasSystemFeature(String name) {
        return systemFeatureList.containsKey(name) ? systemFeatureList.get(name) : false;
    }

    /**
     * Non-Android accessor.  Used to declare a system feature is
     * or is not supported.
     *
     * @param name
     * @param supported
     */
    public void setSystemFeature(String name, boolean supported) {
    	systemFeatureList.put(name, supported);
    }

    private void initializePackageInfo() {
    	if (packageList != null) { return; }

        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = contextWrapper.getPackageName();
        packageInfo.versionName = "1.0";

        packageList = new HashMap<String, PackageInfo>();
        addPackage( packageInfo );
    }

    public class ComponentState {
    	public int newState;
    	public int flags;

		public ComponentState(int newState, int flags) {
			this.newState = newState;
			this.flags = flags;
		}
    }
}
