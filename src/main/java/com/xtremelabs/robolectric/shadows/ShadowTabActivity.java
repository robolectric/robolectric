package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.R;
import android.app.TabActivity;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;

@Implements(TabActivity.class)
public class ShadowTabActivity extends ShadowActivityGroup {
	@RealObject private TabActivity realTabActivity;
	    private TabHost mTabHost;
	    private String mDefaultTab = null;
	    private int mDefaultTabIndex = -1;    
	    	    
	    /**
	     * Sets the default tab that is the first tab highlighted.
	     * 
	     * @param tag the name of the default tab
	     */
	    @Implementation
	    public void setDefaultTab(String tag) {
	        mDefaultTab = tag;
	        mDefaultTabIndex = -1;
	    }

	    /**
	     * Sets the default tab that is the first tab highlighted.
	     * 
	     * @param index the index of the default tab
	     */
	    @Implementation
	    public void setDefaultTab(int index) {
	        mDefaultTab = null;
	        mDefaultTabIndex = index;
	    }

//	    @Override
//	    protected void onRestoreInstanceState(Bundle state) {
//	        super.onRestoreInstanceState(state);
//	        ensureTabHost();
//	        String cur = state.getString("currentTab");
//	        if (cur != null) {
//	            mTabHost.setCurrentTabByTag(cur);
//	        }
//	        if (mTabHost.getCurrentTab() < 0) {
//	            if (mDefaultTab != null) {
//	                mTabHost.setCurrentTabByTag(mDefaultTab);
//	            } else if (mDefaultTabIndex >= 0) {
//	                mTabHost.setCurrentTab(mDefaultTabIndex);
//	            }
//	        }
//	    }

//	    @Override
//	    protected void onPostCreate(Bundle icicle) {        
//	        super.onPostCreate(icicle);
//
//	        ensureTabHost();
//
//	        if (mTabHost.getCurrentTab() == -1) {
//	            mTabHost.setCurrentTab(0);
//	        }
//	    }
//
//	    @Override
//	    protected void onSaveInstanceState(Bundle outState) {
//	        super.onSaveInstanceState(outState);
//	        String currentTabTag = mTabHost.getCurrentTabTag();
//	        if (currentTabTag != null) {
//	            outState.putString("currentTab", currentTabTag);
//	        }
//	    }
//
//	    /**
//	     * Updates the screen state (current list and other views) when the
//	     * content changes.
//	     * 
//	     *@see Activity#onContentChanged()
//	     */
	  //  @Override
	    @Implementation
	    public void onContentChanged() {
	     //   super.onContentChanged();
	        mTabHost = (TabHost) findViewById(R.id.tabhost);

	        if (mTabHost == null) {
	            throw new RuntimeException(
	                    "Your content must have a TabHost whose id attribute is " +
	                    "'android.R.id.tabhost'");
	        }
//	        mTabHost.setup(getLocalActivityManager());
	    }

//	    private void ensureTabHost() {
//	        if (mTabHost == null) {
//	            this.setContentView(R.layout.tab_content);
//	        }
//	    }
//
//	    @Override
//	    protected void
//	    onChildTitleChanged(Activity childActivity, CharSequence title) {
//	        // Dorky implementation until we can have multiple activities running.
//	        if (getLocalActivityManager().getCurrentActivity() == childActivity) {
//	            View tabView = mTabHost.getCurrentTabView();
//	            if (tabView != null && tabView instanceof TextView) {
//	                ((TextView) tabView).setText(title);
//	            }
//	        }
//	    }
	    

	    /**
	     * Returns the {@link TabHost} the activity is using to host its tabs.
	     *
	     * @return the {@link TabHost} the activity is using to host its tabs.
	     */
	    @Implementation
	    public TabHost getTabHost() {
	       // ensureTabHost();
	    	if (this.mTabHost==null)mTabHost = new TabHost(this.realTabActivity);
	    	return mTabHost;
	    }

	    /**
	     * Returns the {@link TabWidget} the activity is using to draw the actual tabs.
	     *
	     * @return the {@link TabWidget} the activity is using to draw the actual tabs.
	     */
	    @Implementation
	    public TabWidget getTabWidget() {
	        return mTabHost.getTabWidget();
	    }
	
}
