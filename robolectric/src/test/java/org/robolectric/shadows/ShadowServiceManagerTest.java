package org.robolectric.shadows;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.view.IWindowManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowServiceManagerTest {
  @Test
  public void forWindowService_getService_returnsIWindowManager() throws Exception {
//    (IWindowManager) ServiceManager.getService(Context.WINDOW_SERVICE)
    IBinder service = ServiceManager.getService(Context.WINDOW_SERVICE);
    assertThat(service).isInstanceOf(IWindowManager.class);
    IWindowManager iWindowManager = IWindowManager.Stub.asInterface(service);
    assertThat(iWindowManager).isNotNull();
    iWindowManager.openSession(null, null, null);
  }
}