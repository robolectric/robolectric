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

    @Test
    public void testObject_isNull_shouldReturnTrueForNullValues() throws Exception {
        assertTrue(new JSONObject("{'name':null}").isNull("name")); // passes
    }

    @Test
    public void testObject_getString_shouldReturnNullForNullValues() throws Exception {
        assertEquals(null, new JSONObject("{'name':null}").getString("name")); // fails
    }

    @Test
    public void testObject_getJSONObject_shouldReturnNullForNullValues() throws Exception {
        assertEquals(null, new JSONObject("{'name':null}").getJSONObject("name")); // fails
    }

    @Test
    public void testObject_getInt_shouldReturnNullForNullValues() throws Exception {
        assertEquals(null, new JSONObject("{'name':null}").getInt("name")); // fails
    }

    @Test
    public void testObject_get_shouldReturnNullForNullValues() throws Exception {
        assertEquals(null, new JSONObject("{'name':null}").get("name")); // fails
    }

    @Test
    public void testObject_optString_shouldReturnNullForNullValues() throws Exception {
        assertEquals(null, new JSONObject("{'name':null}").optString("name")); // fails
    }

    @Test
    public void testObject_optInt_shouldReturnNullForNullValues() throws Exception {
        assertEquals(null, new JSONObject("{'name':null}").optInt("name")); // fails
    }

    @Test
    public void testArray_isNull_shouldReturnTrueForNullValues() throws Exception {
        assertTrue(new JSONArray("[null]").isNull(0)); // passes
    }

    @Test
    public void testArray_getString_shouldReturnNullForNullValues() throws Exception {
        assertEquals(null, new JSONArray("[null]").getString(0)); // fails
    }
}
