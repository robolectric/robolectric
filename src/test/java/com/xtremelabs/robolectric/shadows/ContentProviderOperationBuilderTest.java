package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class ContentProviderOperationBuilderTest {
    private Builder builder;
    private ShadowContentProviderOperationBuilder shadowBuilder;
    
    @Before
    public void before() {
        builder = Robolectric.newInstanceOf(Builder.class);
        shadowBuilder = Robolectric.shadowOf(builder);
    }
    
    @Test
    public void withValue() {
        builder
            .withValue("stringTest", "bar")
            .withValue("intTest", 5)
            .withValue("longTest", 10L);
        
        Map<String, Object> values = shadowBuilder.getValues();
        assertThat(values.size(), is(3));
        assertThat(values.get("stringTest").toString(), equalTo("bar"));
        assertThat(Integer.parseInt(values.get("intTest").toString()), equalTo(5));
        assertThat(Long.parseLong(values.get("longTest").toString()), equalTo(10L));
    }
    
    @Test
    public void withSelection() {
        builder
            .withSelection("first", new String[] { "a", "b" })
            .withSelection("second", new String[] { "c", "d" });
        
        Map<String, String[]> selections = shadowBuilder.getSelections();
        assertThat(selections.size(), is(2));
        assertThat(selections.get("first"), equalTo(new String[] { "a", "b" }));
        assertThat(selections.get("second"), equalTo(new String[] { "c", "d" }));
    }
    
    @Test
    public void withValueBackReference() {
        builder.withValueBackReference("foo", 5);
        
        int backReference = shadowBuilder.getWithValueBackReference("foo");
        assertThat(backReference, is(5));
    }
    
    @Test
    public void build() {
        ContentProviderOperation operation = builder.build();
        assertThat(operation, notNullValue());
    }
}