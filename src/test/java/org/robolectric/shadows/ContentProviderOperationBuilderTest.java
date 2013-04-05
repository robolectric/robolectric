package org.robolectric.shadows;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ContentProviderOperationBuilderTest {
    private Builder builder;
    private ShadowContentProviderOperation.ShadowBuilder shadowBuilder;
    
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
        assertThat(values.size()).isEqualTo(3);
        assertThat(values.get("stringTest").toString()).isEqualTo("bar");
        assertThat(Integer.parseInt(values.get("intTest").toString())).isEqualTo(5);
        assertThat(Long.parseLong(values.get("longTest").toString())).isEqualTo(10L);
    }
    
    @Test
    public void withSelection() {
        builder
            .withSelection("first", new String[] { "a", "b" })
            .withSelection("second", new String[] { "c", "d" });
        
        Map<String, String[]> selections = shadowBuilder.getSelections();
        assertThat(selections.size()).isEqualTo(2);
        assertThat(selections.get("first")).isEqualTo(new String[]{"a", "b"});
        assertThat(selections.get("second")).isEqualTo(new String[]{"c", "d"});
    }
    
    @Test
    public void withValueBackReference() {
        builder.withValueBackReference("foo", 5);
        
        int backReference = shadowBuilder.getWithValueBackReference("foo");
        assertThat(backReference).isEqualTo(5);
    }
    
    @Test
    public void build() {
        ContentProviderOperation operation = builder.build();
        assertThat(operation).isNotNull();
    }
}