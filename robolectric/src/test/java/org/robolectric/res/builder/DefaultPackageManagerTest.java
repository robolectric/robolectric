package org.robolectric.res.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DefaultPackageManagerTest {
  
  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldReturnServiceInfoIfExists() throws Exception {
    PackageManager packageManager = RuntimeEnvironment.getPackageManager();
    ServiceInfo serviceInfo = packageManager.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_SERVICES);
    assertEquals(serviceInfo.packageName, "org.robolectric");
    assertEquals(serviceInfo.name, "com.foo.Service");
    assertEquals(serviceInfo.permission, "com.foo.MY_PERMISSION");
    assertNotNull(serviceInfo.applicationInfo);  
  }
  
  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldReturnServiceInfoWithMetaDataWhenFlagsSet() throws Exception {
    PackageManager packageManager = RuntimeEnvironment.getPackageManager();
    ServiceInfo serviceInfo = packageManager.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_META_DATA);
    assertNotNull(serviceInfo.metaData);
  }
  
  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldReturnServiceInfoWithoutMetaDataWhenFlagsNotSet() throws Exception {
    PackageManager packageManager = RuntimeEnvironment.getPackageManager();
    ServiceInfo serviceInfo = packageManager.getServiceInfo(new ComponentName("org.robolectric", "com.foo.Service"), PackageManager.GET_SERVICES);
    assertNull(serviceInfo.metaData);
  }
  
  @Test
  @Config(manifest = "src/test/resources/TestPackageManagerGetServiceInfo.xml")
  public void getServiceInfo_shouldThrowNameNotFoundExceptionIfNotExist() throws Exception {
    PackageManager packageManager = RuntimeEnvironment.getPackageManager();
    ComponentName nonExistComponent = new ComponentName("org.robolectric", "com.foo.NonExistService");
    try {
      packageManager.getServiceInfo(nonExistComponent, PackageManager.GET_SERVICES);
    } catch (NameNotFoundException e) {
      return;
    }
    fail("NameNotFoundException is expected.");
  }
}