package org.robolectric.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.robolectric.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestRunners.WithDefaults.class)
public class H2Map_TypeForwardOnlyTest {

	H2Map_TypeForwardOnly map;
	@Before
    public void setUp() throws Exception {
      map = new H2Map_TypeForwardOnly();  
    }

   
    
    @Test
    public void testGetH2ResultSetIs_TYPE_FORWARD_ONLY() throws SQLException {
        assertThat(map.getResultSetType()).isEqualTo(ResultSet.TYPE_FORWARD_ONLY);
    }
}
