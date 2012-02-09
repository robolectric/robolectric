package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ContextTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
        assertThat(context.getFilesDir().listFiles().length, is(0));
        assertThat(context.getCacheDir().listFiles().length, is(0));
    }
    
    @After
    public void after() {
        File[] files = context.getFilesDir().listFiles();
        for (File file : files)
            file.delete();
        files = context.getCacheDir().listFiles();
        for (File file : files)
            file.delete();
    }

    @Test
    public void shouldStubThemeStuff() throws Exception {
        assertThat(context.obtainStyledAttributes(null), not(nullValue()));
        assertThat(context.obtainStyledAttributes(0, null), not(nullValue()));
        assertThat(context.obtainStyledAttributes(null, null), not(nullValue()));
        assertThat(context.obtainStyledAttributes(null, null, 0, 0), not(nullValue()));
    }

    @Test
    public void getCacheDir_shouldCreateDirectory() throws Exception {
        assertTrue(context.getCacheDir().exists());
    }

    @Test
    public void getExternalCacheDir_shouldCreateDirectory() throws Exception {
        assertTrue(context.getExternalCacheDir().exists());
    }

    @Test
    public void shouldWriteToCacheDir() throws Exception {
        assertNotNull(context.getCacheDir());
        File cacheTest = new File(context.getCacheDir(), "__test__");

        assertThat(cacheTest,
                        equalTo(new File(new File(System.getProperty("java.io.tmpdir"), "android-cache"), "__test__")));

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cacheTest);
            fos.write("test".getBytes());
        } finally {
            if (fos != null)
                fos.close();
        }
        assertTrue(cacheTest.exists());
    }

    @Test
    public void shouldWriteToExternalCacheDir() throws Exception {
        assertNotNull(context.getExternalCacheDir());
        File cacheTest = new File(context.getExternalCacheDir(), "__test__");

        assertThat(cacheTest,
                equalTo(new File(new File(System.getProperty("java.io.tmpdir"), "android-external-cache"), "__test__")));
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cacheTest);
            fos.write("test".getBytes());
        } finally {
            if (fos != null)
                fos.close();
        }

        assertTrue(cacheTest.exists());
    }

    @Test
    public void getFilesDir_shouldCreateDirectory() throws Exception {
        assertTrue(context.getFilesDir().exists());
    }
    
    @Test
    public void getExternalFilesDir_shouldCreateDirectory() throws Exception {
        assertTrue(context.getExternalFilesDir(null).exists());
    }

    @Test
    public void openFileInput_shouldReturnAFileInputStream() throws Exception {
        String fileContents = "blah";

        File file = new File(context.getFilesDir(), "__test__");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(fileContents);
        fileWriter.close();

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = context.openFileInput("__test__");
    
            byte[] bytes = new byte[fileContents.length()];
            fileInputStream.read(bytes);
            assertThat(bytes, equalTo(fileContents.getBytes()));
        } finally {
            if (fileInputStream != null)
                fileInputStream.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void openFileInput_shouldNotAcceptPathsWithSeparatorCharacters() throws Exception {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = context.openFileInput("data" + File.separator + "test");
        } finally {
            if (fileInputStream != null)
                fileInputStream.close();
        }
    }

    @Test
    public void openFileOutput_shouldReturnAFileOutputStream() throws Exception {
        File file = new File("__test__");
        String fileContents = "blah";
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput("__test__", -1);
            fileOutputStream.write(fileContents.getBytes());
        } finally {
            if (fileOutputStream != null)
                fileOutputStream.close();
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(context.getFilesDir(), file.getName()));
            byte[] readBuffer = new byte[fileContents.length()];
            fileInputStream.read(readBuffer);
            assertThat(new String(readBuffer), equalTo(fileContents));
        } finally {
            if (fileInputStream != null)
                fileInputStream.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void openFileOutput_shouldNotAcceptPathsWithSeparatorCharacters() throws Exception {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(File.separator + "data" + File.separator + "test" + File.separator + "hi", 0);
        } finally {
            if (fos != null)
                fos.close();
        }
    }
    
    @Test
    public void deleteFile_shouldReturnTrue() throws IOException {
        File filesDir = context.getFilesDir();
        File file = new File(filesDir, "test.txt");
        boolean successfully = file.createNewFile();
        assertThat(successfully, is(true));
        successfully = context.deleteFile(file.getName());
        assertThat(successfully, is(true));
    }
    
    @Test
    public void deleteFile_shouldReturnFalse() throws IOException {
        File filesDir = context.getFilesDir();
        File file = new File(filesDir, "test.txt");
        boolean successfully = context.deleteFile(file.getName());
        assertThat(successfully, is(false));
    }
}
