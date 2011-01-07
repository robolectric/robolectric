package com.xtremelabs.robolectric.shadows;

import android.net.Uri;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class UriTest {
    @Test
    public void shouldParseUris() throws Exception {
        Uri testUri = Uri.parse("http://someplace.com:8080/a/path?param=value&another_param=another_value#top");

        assertThat(testUri.getQuery(), equalTo("param=value&another_param=another_value"));
        assertThat(testUri.getPort(), equalTo(8080));
        assertThat(testUri.getAuthority(), equalTo("someplace.com:8080"));
        assertThat(testUri.getHost(), equalTo("someplace.com"));
        assertThat(testUri.getFragment(), equalTo("top"));
        assertThat(testUri.getPath(), equalTo("/a/path"));
        assertThat(testUri.getScheme(), equalTo("http"));
    }
}
