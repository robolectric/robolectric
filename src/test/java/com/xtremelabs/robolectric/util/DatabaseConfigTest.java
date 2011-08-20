package com.xtremelabs.robolectric.util;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.DatabaseConfig.DatabaseMap;
import com.xtremelabs.robolectric.util.DatabaseConfig.NullDatabaseMapException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.ResultSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class DatabaseConfigTest {

    @Before
    public void setup() {
        DatabaseConfig.setDatabaseMap(new H2Map());
    }

    @Test
    public void testSettingDatabaseMapLoadsCorrectly() throws Exception {
        assertThat(DatabaseConfig.getDatabaseMap().getClass().getName(), equalTo(H2Map.class.getName()));
        assertThat(DatabaseConfig.isMapLoaded(), equalTo(false));
        DatabaseConfig.getMemoryConnection(); //load map
        assertThat(DatabaseConfig.isMapLoaded(), equalTo(true));
        assertThat(DatabaseConfig.getResultSetType(), equalTo(ResultSet.TYPE_SCROLL_INSENSITIVE));

        H2Map_TypeForwardOnly ForwardOnlyMap = new H2Map_TypeForwardOnly();
        DatabaseConfig.setDatabaseMap(ForwardOnlyMap);
        assertThat(DatabaseConfig.isMapLoaded(), equalTo(false));
        assertThat(DatabaseConfig.getDatabaseMap(), equalTo((DatabaseMap) ForwardOnlyMap));
        DatabaseConfig.getMemoryConnection(); //load map
        assertThat(DatabaseConfig.isMapLoaded(), equalTo(true));
        assertThat(DatabaseConfig.getDatabaseMap().getClass().getName(), equalTo(H2Map_TypeForwardOnly.class.getName()));
        assertThat(DatabaseConfig.getResultSetType(), equalTo(ResultSet.TYPE_FORWARD_ONLY));
    }

    @Test
    public void testMapIsNotNull() throws Exception {
        assertThat(DatabaseConfig.isMapNull(), equalTo(false));
    }

    private void setDatabaseMapNull() {
        DatabaseConfig.setDatabaseMap(null);
        assertThat(DatabaseConfig.isMapNull(), equalTo(true));
        assertThat(DatabaseConfig.isMapLoaded(), equalTo(false));
    }

    @Test(expected = NullDatabaseMapException.class)
    public void testLoadingNullMapThrowsException() throws Exception {
        setDatabaseMapNull();
        DatabaseConfig.getMemoryConnection(); //attempt to load driver for map and throw exception
    }

    @Test(expected = NullDatabaseMapException.class)
    public void testGetScrubSQLThrowsExceptionWithNullMap() throws Exception {
        setDatabaseMapNull();
        DatabaseConfig.getScrubSQL("");
    }

    @Test(expected = NullDatabaseMapException.class)
    public void testGetSelectLastInsertIdentityThrowsExceptionWithNullMap() throws Exception {
        setDatabaseMapNull();
        DatabaseConfig.getSelectLastInsertIdentity();
    }

    @Test(expected = NullDatabaseMapException.class)
    public void testGetResultSetTypeThrowsExceptionWithNullMap() throws Exception {
        setDatabaseMapNull();
        DatabaseConfig.getResultSetType();
    }

    @Test
    public void testGetScrubSQL() throws Exception {
        assertThat(DatabaseConfig.getScrubSQL("autoincrement"), equalTo(DatabaseConfig.getDatabaseMap().getScrubSQL("autoincrement")));
    }

    @Test
    public void testGetSelectLastInsertIdentity() throws Exception {
        assertThat(DatabaseConfig.getSelectLastInsertIdentity(), equalTo(DatabaseConfig.getDatabaseMap().getSelectLastInsertIdentity()));
    }

    @Test
    public void testGetResultSetType() throws Exception {
        assertThat(DatabaseConfig.getResultSetType(), equalTo(DatabaseConfig.getDatabaseMap().getResultSetType()));
    }
}
