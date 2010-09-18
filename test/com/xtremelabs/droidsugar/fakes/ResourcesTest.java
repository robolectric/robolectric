package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.content.res.Resources;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.R;
import com.xtremelabs.droidsugar.util.ResourceLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ResourcesTest {
    @Test(expected = Resources.NotFoundException.class)
    public void getStringArray_shouldThrowExceptionIfNotFound() throws Exception {
        DroidSugarAndroidTestRunner.addGenericProxies();
        FakeContextWrapper.resourceLoader = new ResourceLoader(R.class, new File("test/res"));

        new Activity().getResources().getStringArray(-1);
    }
}
