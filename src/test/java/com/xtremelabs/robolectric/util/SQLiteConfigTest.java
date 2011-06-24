package com.xtremelabs.robolectric.util;

import static com.xtremelabs.robolectric.util.SQLite.buildInsertString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.sql.Connection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.sqlite.SQLiteJDBCLoader;

import android.content.ContentValues;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.SQLite.SQLStringAndBindings;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteConfigTest {

	Connection connection;
	@Before
    public void setUp() throws Exception {
		connection = DBConfig.OpenMemoryConnection();
    }
	
	//TODO: instead test that the mode matches which test runner is in use
	@Test
    public void testDBRunningJavaMode() {
		Assert.assertTrue(!SQLiteJDBCLoader.isNativeMode());
    }
	
}
