package com.xtremelabs.robolectric.util;

import android.app.Application;
import com.xtremelabs.robolectric.TestApplication;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ClassNameResolverTest {

    @Test
    public void shouldResolveClassesBySimpleName() throws Exception {
        assertEquals(TestApplication.class, new ClassNameResolver<Application>("com.xtremelabs.robolectric", "TestApplication").resolve());
    }

    @Test
    public void shouldResolveClassesByDottedSimpleName() throws Exception {
        assertEquals(TestApplication.class, new ClassNameResolver<Application>("com.xtremelabs.robolectric", ".TestApplication").resolve());
    }

    @Test
    public void shouldResolveClassesByFullyQualifiedName() throws Exception {
        assertEquals(TestApplication.class, new ClassNameResolver<Application>("com.xtremelabs.robolectric", "com.xtremelabs.robolectric.TestApplication").resolve());
    }

    @Test
    public void shouldResolveClassesByPartiallyQualifiedName() throws Exception {
        assertEquals(TestApplication.class, new ClassNameResolver<Application>("com.xtremelabs", ".robolectric.TestApplication").resolve());
    }

    @Test
    public void shouldNotResolveClassesByUndottedPartiallyQualifiedNameBecauseAndroidDoesnt() throws Exception {
        assertEquals(null, new ClassNameResolver<Application>("com.xtremelabs", "robolectric.TestApplication").resolve());
    }
}
