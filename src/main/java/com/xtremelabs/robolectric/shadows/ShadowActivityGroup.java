package com.xtremelabs.robolectric.shadows;



import java.util.HashMap;

import android.R;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(ActivityGroup.class)
public class ShadowActivityGroup extends ShadowActivity {
	@RealObject protected ActivityGroup realActivityGroup;
	
//    @Implementation
//    public void onContentChanged() {
//      
//    }
	
    //protected LocalActivityManager mLocalActivityManager;
    
//    public void __constructor__() {
//    }
//    
//    public void __constructor__(boolean singleActivityMode) {
//      //  mLocalActivityManager = new LocalActivityManager(this, singleActivityMode);
//    }

    
//    protected void onCreate(Bundle savedInstanceState) {
//        //  super.onCreate(savedInstanceState);
////        Bundle states = savedInstanceState != null
////                ? (Bundle) savedInstanceState.getBundle(STATES_KEY) : null;
////        mLocalActivityManager.dispatchCreate(states);
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        mLocalActivityManager.dispatchResume();
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Bundle state = mLocalActivityManager.saveInstanceState();
//        if (state != null) {
//            outState.putBundle(STATES_KEY, state);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mLocalActivityManager.dispatchPause(isFinishing());
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mLocalActivityManager.dispatchStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mLocalActivityManager.dispatchDestroy(isFinishing());
//    }

    /**
     * Returns a HashMap mapping from child activity ids to the return values
     * from calls to their onRetainNonConfigurationInstance methods.
     *
     * {@hide}
     */
    
//    public HashMap<String,Object> onRetainNonConfigurationChildInstances() {
//    	 realActivityGroup.getLocalActivityManager().dispatchRetainNonConfigurationInstance();
//       // return mLocalActivityManager.dispatchRetainNonConfigurationInstance();
//    }

//    public Activity getCurrentActivity() {
//    	 realActivityGroup.getLocalActivityManager().getCurrentActivity();
//    }
//
//    public final LocalActivityManager getLocalActivityManager() {
//       realActivityGroup.getLocalActivityManager();
//    }
//
//    @Override
//    void dispatchActivityResult(String who, int requestCode, int resultCode,
//            Intent data) {
//        if (who != null) {
//            Activity act = mLocalActivityManager.getActivity(who);
//            /*
//            if (Config.LOGV) Log.v(
//                TAG, "Dispatching result: who=" + who + ", reqCode=" + requestCode
//                + ", resCode=" + resultCode + ", data=" + data
//                + ", rec=" + rec);
//            */
//            if (act != null) {
//                act.onActivityResult(requestCode, resultCode, data);
//                return;
//            }
//        }
//        super.dispatchActivityResult(who, requestCode, resultCode, data);
//    }

	
}
