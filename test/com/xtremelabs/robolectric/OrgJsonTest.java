package com.xtremelabs.robolectric;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class OrgJsonTest {
    @Test
    public void testHasImplementation() throws Exception {
        assertEquals("value", new JSONObject("{'name':'value'}").getString("name"));
    }
}
