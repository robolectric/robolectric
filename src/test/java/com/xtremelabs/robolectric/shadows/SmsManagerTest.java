package com.xtremelabs.robolectric.shadows;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.telephony.SmsManager;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class SmsManagerTest {
	
	private SmsManager smsManager;
	private ShadowSmsManager shadowSmsManager;
	
	private String testDestinationAddress = "destinationAddress";
	private String testScAddress = "testServiceCenterAddress";
	private String testText = "testSmsBodyText";
	
	@Before
	public void setup() {
		Robolectric.bindShadowClass(ShadowSmsManager.class);
		smsManager = SmsManager.getDefault();
		shadowSmsManager = Robolectric.shadowOf(smsManager);
	}
	
	@After
	public void tearDown() {
		smsManager = null;
		shadowSmsManager = null;
	}
	
	@Test
	public void shouldHaveShadowSmsManager() {
		Assert.assertNotNull(shadowSmsManager);
	}
	
	@Test
	public void shouldStoreLastSentTextMessageParameters() {
		
		smsManager.sendTextMessage(testDestinationAddress, testScAddress, testText, null, null);
		
		ShadowSmsManager.TextSmsParams lastParams = shadowSmsManager.getLastSentTextMessageParams();
		
		Assert.assertEquals(testDestinationAddress, lastParams.getDestinationAddress());
		Assert.assertEquals(testScAddress, lastParams.getScAddress());
		Assert.assertEquals(testText, lastParams.getText());
	}

    @Test
    public void shouldClearLastSentTestMessageParameters() {
        smsManager.sendTextMessage(testDestinationAddress, testScAddress, testText, null, null);
        shadowSmsManager.clearLastSentTextMessageParams();
        Assert.assertNull(shadowSmsManager.getLastSentTextMessageParams());
    }
	
	@Test(expected=IllegalArgumentException.class)
	public void sendTextMessage_shouldThrowExceptionWithEmptyDestination() {
		smsManager.sendTextMessage("", testScAddress, testText, null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void sentTextMessage_shouldThrowExceptionWithEmptyText() {
		smsManager.sendTextMessage(testDestinationAddress, testScAddress, "", null, null);
	}
}
