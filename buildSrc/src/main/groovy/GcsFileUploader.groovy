import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GcsFileUploader extends DefaultTask {
    String prefix
    File dir
    String bucket
    String key
    String secret

    @TaskAction
    public void writeProperties() throws Exception {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        def bucket = storage.create(BucketInfo.of("robolectric-test-results"))
        dir.eachFileRecurse { f ->
            println "Upload to ${prefix}/${f.path}"
            bucket.create("${prefix}/${f.path}", f.newInputStream(), "text/html")
        }
    }
}