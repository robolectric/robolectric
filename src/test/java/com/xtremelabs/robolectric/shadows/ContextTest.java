package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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
    public void shouldWriteToCacheDir() throws Exception {
        File cacheTest = new File(context.getCacheDir(), "__test__");

        FileOutputStream fos = new FileOutputStream(cacheTest);
        fos.write("test".getBytes());
        fos.close();

        assertTrue(cacheTest.exists());
    }

    @Test
    public void getFilesDir_shouldCreateDirectory() throws Exception {
        assertTrue(context.getFilesDir().exists());
    }

    @Test
    public void openFileInput_shouldReturnAFileInputStream() throws Exception {
        String fileContents = "blah";

        File file = new File(context.getFilesDir(), "__test__");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(fileContents);
        fileWriter.close();

        FileInputStream fileInputStream = context.openFileInput("__test__");

        byte[] bytes = new byte[fileContents.length()];
        fileInputStream.read(bytes);
        assertThat(bytes, equalTo(fileContents.getBytes()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void openFileInput_shouldNotAcceptPathsWithSeparatorCharacters() throws Exception {
        context.openFileInput("data" + File.separator + "test");
    }

    @Test
    public void openFileOutput_shouldReturnAFileOutputStream() throws Exception {
        File file = new File("__test__");
        FileOutputStream fileOutputStream = context.openFileOutput("__test__", -1);
        String fileContents = "blah";
        fileOutputStream.write(fileContents.getBytes());

        FileInputStream fileInputStream = new FileInputStream(new File(context.getFilesDir(), file.getName()));
        byte[] readBuffer = new byte[fileContents.length()];
        fileInputStream.read(readBuffer);
        assertThat(new String(readBuffer), equalTo(fileContents));
    }

    @Test(expected = IllegalArgumentException.class)
    public void openFileOutput_shouldNotAcceptPathsWithSeparatorCharacters() throws Exception {
        context.openFileOutput(File.separator + "data" + File.separator + "test" + File.separator + "hi", 0);
    }
}
