package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import sun.misc.IOUtils;
import android.app.Activity;
import android.app.Application;
import android.content.res.AssetManager;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class AssetManagerTest {
	Application app;
	AssetManager assetManager;
	String sp = System.getProperty("file.separator");

	@Before
	public void setUp() throws Exception {
		Robolectric.application = new Application();
		app = Robolectric.application;
		assetManager = app.getAssets();
	}

	@Test
	public void assertGetAssetsNotNull() {
		AssetManager manager;

		manager = new Activity().getAssets();
		assertNotNull(manager);

		manager = app.getAssets();
		assertNotNull(manager);

		manager = app.getResources().getAssets();
		assertNotNull(manager);
	}

	@Test
	public void assetsPathListing() throws IOException {
		List<String> files;
		String testPath;

		testPath = "";
		files = Arrays.asList(assetManager.list(testPath));
		assertTrue(files.contains("docs"));
		assertTrue(files.contains("assetsHome.txt"));

		testPath = "docs";
		files = Arrays.asList(assetManager.list(testPath));
		assertTrue(files.contains("extra"));

		testPath = "docs" + sp + "extra";
		files = Arrays.asList(assetManager.list(testPath));
		assertTrue(files.contains("testing"));

		testPath = "docs" + sp + "extra" + sp + "testing";
		files = Arrays.asList(assetManager.list(testPath));
		assertTrue(files.contains("hello.txt"));
	}

	@Test
	public void assetsInputStreams() throws IOException {
		String testPath;
		String fileContents;
		InputStream iS;

		testPath = "assetsHome.txt";
		iS = assetManager.open(testPath);
		fileContents = new String(IOUtils.readFully(iS, -1, true));
		assertEquals("assetsHome!", fileContents);

		testPath = "docs" + sp + "extra" + sp + "testing" + sp + "hello.txt";
		iS = assetManager.open(testPath);
		fileContents = new String(IOUtils.readFully(iS, -1, true));
		assertEquals("hello!", fileContents);
	}

}
