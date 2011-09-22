package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.pm.ResolveInfo;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class ResolveInfoTest {

    private ResolveInfo mResolveInfo;
    private ShadowResolveInfo mShadowInfo;

    @Before
    public void setup() {
        mResolveInfo = new ResolveInfo();
        mShadowInfo = Robolectric.shadowOf(mResolveInfo);
    }

    @Test
    public void testLoadLabel() {
        mShadowInfo.setLabel("test");
        assertThat("test", equalTo(mResolveInfo.loadLabel(null)));
    }

    @Test
    public void testNewResolveInfo() {
        ResolveInfo info = ShadowResolveInfo.newResolveInfo("name", "package");
        assertThat(info.loadLabel(null).toString(), equalTo("name"));
        assertThat(info.activityInfo.packageName, equalTo("package"));
    }
}
