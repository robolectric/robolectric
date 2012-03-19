package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Environment;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(WithTestDefaultsRunner.class)
public class EnvironmentTest {

	@After
	public void tearDown() throws Exception {
		deleteDir(ShadowContext.EXTERNAL_CACHE_DIR);
		deleteDir(ShadowContext.EXTERNAL_FILES_DIR);
		ShadowEnvironment.setExternalStorageState("removed");
	}
	
	@Test
	public void testExternalStorageState() {
		assertThat( Environment.getExternalStorageState(), equalTo("removed") );
		ShadowEnvironment.setExternalStorageState("mounted");
		assertThat( Environment.getExternalStorageState(), equalTo("mounted") );
	}
	
	@Test
	public void testGetExternalStorageDirectory() {
		 assertTrue(Environment.getExternalStorageDirectory().exists());
	}
	
	@Test
	public void testGetExternalStoragePublicDirectory() {
		File extStoragePublic = Environment.getExternalStoragePublicDirectory("Movies"); 
		assertTrue(extStoragePublic.exists());
		assertThat(extStoragePublic, equalTo( new File(ShadowContext.EXTERNAL_FILES_DIR, "Movies" ) ) );
	}
	
    public void deleteDir(File path) {
		if (path.isDirectory()) {
			File[] files = path.listFiles();
			for (File f : files) {
				deleteDir(f);
			}
		}
		path.delete();
	}

}
