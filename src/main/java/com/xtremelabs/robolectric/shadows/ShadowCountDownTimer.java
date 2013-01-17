package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.os.CountDownTimer;

@Implements(CountDownTimer.class)
public class ShadowCountDownTimer {

    private boolean started;
    private long countDownInterval;
    private long millisInFuture;
    
    private static ShadowCountDownTimer last;

    @RealObject CountDownTimer countDownTimer;

    public void __constructor__(long millisInFuture, long countDownInterval) {
        this.countDownInterval = countDownInterval;
        this.millisInFuture = millisInFuture;
        this.started = false;
        last = this;
    }
    
    public static ShadowCountDownTimer getLast(){
    	return last;
    }

    @Implementation
    public final synchronized CountDownTimer start() {
        started = true;
        return countDownTimer;
    }


    @Implementation
    public final void cancel() {
        started = false;
    }


    /**
     * ******************************************************
     * Non-implementation methods for firing abstract methods
     * *******************************************************
     */
    public void invokeTick(long millisUntilFinished) {
        countDownTimer.onTick(millisUntilFinished);
    }

    public void invokeFinish() {
        countDownTimer.onFinish();
    }

    public boolean hasStarted() {
        return started;
    }
    
    public long getCountDownInterval() {
    	return countDownInterval;
    }
    
    public long getMillisInFuture() {
    	return millisInFuture;
    }
}
