package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import com.xtremelabs.robolectric.util.H2Map;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@DatabaseConfig.UsingDatabaseMap(H2Map.class)
@RunWith(WithTestDefaultsRunner.class)
public class H2DatabaseTest extends DatabaseTestBase {
    @Test
    public void shouldUseH2DatabaseMap() throws Exception {
        assertThat(DatabaseConfig.getDatabaseMap().getClass().getName(), 
                equalTo(H2Map.class.getName()));
    }
}
