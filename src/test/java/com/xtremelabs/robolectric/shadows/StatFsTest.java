package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.StatFs;

import java.io.File;

@RunWith(WithTestDefaultsRunner.class)
public class StatFsTest {
    @Test
    public void shouldRegisterStats() throws Exception {
        ShadowStatFs.registerStats("/tmp", 100, 20, 10);
        StatFs statsFs = new StatFs("/tmp");

        assertThat(statsFs.getBlockCount(), equalTo(100));
        assertThat(statsFs.getFreeBlocks(), equalTo(20));
        assertThat(statsFs.getAvailableBlocks(), equalTo(10));
        assertThat(statsFs.getBlockSize(), equalTo(ShadowStatFs.BLOCK_SIZE));
    }

    @Test
    public void shouldRegisterStatsWithFile() throws Exception {
        File path = new File("/tmp");
        ShadowStatFs.registerStats(path, 100, 20, 10);
        StatFs statsFs = new StatFs(path.getAbsolutePath());

        assertThat(statsFs.getBlockCount(), equalTo(100));
        assertThat(statsFs.getFreeBlocks(), equalTo(20));
        assertThat(statsFs.getAvailableBlocks(), equalTo(10));
        assertThat(statsFs.getBlockSize(), equalTo(ShadowStatFs.BLOCK_SIZE));
    }

    @Test
    public void shouldResetStateBetweenTests() throws Exception {
        StatFs statsFs = new StatFs("/tmp");
        assertThat(statsFs.getBlockCount(), equalTo(0));
        assertThat(statsFs.getFreeBlocks(), equalTo(0));
        assertThat(statsFs.getAvailableBlocks(), equalTo(0));
        assertThat(statsFs.getBlockSize(), equalTo(ShadowStatFs.BLOCK_SIZE));
    }

    @Test
    public void shouldRestat() throws Exception {
        ShadowStatFs.registerStats("/tmp", 100, 20, 10);
        StatFs statsFs = new StatFs("/tmp");

        assertThat(statsFs.getBlockCount(), equalTo(100));
        assertThat(statsFs.getFreeBlocks(), equalTo(20));
        assertThat(statsFs.getAvailableBlocks(), equalTo(10));

        ShadowStatFs.registerStats("/tmp", 3, 2, 1);

        statsFs.restat("/tmp");
        assertThat(statsFs.getBlockCount(), equalTo(3));
        assertThat(statsFs.getFreeBlocks(), equalTo(2));
        assertThat(statsFs.getAvailableBlocks(), equalTo(1));
    }
}
