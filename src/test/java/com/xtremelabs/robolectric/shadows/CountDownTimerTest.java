package com.xtremelabs.robolectric.shadows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.CountDownTimer;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;

import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class CountDownTimerTest {
	
	private ShadowCountDownTimer shadowCountDownTimer;
	private CountDownTimer countDownTimer;
	private long millisInFuture = 2000;
	private long countDownInterval = 1000;
	private String msg = null;
	
    @Before
    public void setUp() throws Exception {
    	
    	countDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {
    		
			@Override
			public void onFinish() {
				msg = "onFinish() is called";
			}

			@Override
			public void onTick(long millisUnitilFinished) {
				msg = "onTick() is called";
			}    		
    	};
    	shadowCountDownTimer = Robolectric.shadowOf(countDownTimer);
    }
	
	
	@Test
	public void testInvokeOnTick() {
		assertThat(msg, not(equalTo("onTick() is called")));
		shadowCountDownTimer.invokeTick(countDownInterval);
		assertThat(msg, equalTo("onTick() is called"));		
	}
	
	@Test
	public void testInvokeOnFinish() {
		assertThat(msg, not(equalTo("onFinish() is called")));
		shadowCountDownTimer.invokeFinish();
		assertThat(msg, equalTo("onFinish() is called"));
	}
	
	@Test
	public void testStart() {
		assertThat(shadowCountDownTimer.hasStarted(), equalTo(false));
		CountDownTimer timer = shadowCountDownTimer.start();
		assertThat(timer, notNullValue());
		assertThat(shadowCountDownTimer.hasStarted(), equalTo(true));
	}
	
	@Test
	public void testCancel() {
		CountDownTimer timer = shadowCountDownTimer.start();
		assertThat(timer, notNullValue());
		assertThat(shadowCountDownTimer.hasStarted(), equalTo(true));
		shadowCountDownTimer.cancel();
		assertThat(shadowCountDownTimer.hasStarted(), equalTo(false));			
	}
	
	@Test
	public void testAccessors() {
		assertThat(shadowCountDownTimer.getCountDownInterval(), equalTo(countDownInterval));
		assertThat(shadowCountDownTimer.getMillisInFuture(), equalTo(millisInFuture));
	}
}
