package org.robolectric.shadows;

import org.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.provider.MediaStore.Images;
import static android.provider.MediaStore.Video;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class MediaStoreTest {
    @Test
    public void shouldInitializeFields() throws Exception {
        assertThat(Images.Media.EXTERNAL_CONTENT_URI.toString(), equalTo("content://media/external/images/media"));
        assertThat(Images.Media.INTERNAL_CONTENT_URI.toString(), equalTo("content://media/internal/images/media"));
        assertThat(Video.Media.EXTERNAL_CONTENT_URI.toString(), equalTo("content://media/external/video/media"));
        assertThat(Video.Media.INTERNAL_CONTENT_URI.toString(), equalTo("content://media/internal/video/media"));
    }
}
