import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.robolectric.spek.RobolectricInstanceFactory
import org.robolectric.spek.useRobolectric
import org.spekframework.spek2.CreateWith
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

@CreateWith(RobolectricInstanceFactory::class)
object SpekIntegrationSpec : Spek({
    useRobolectric()

    describe("Specs using Spek") {
        it("starts up") {
            val application = ApplicationProvider.getApplicationContext<Application>()
            assertThat(application.packageName).isEqualTo("org.robolectric.testapp.test")
        }

        it("can run a second spec") {
            val application = ApplicationProvider.getApplicationContext<Application>()
            assertThat(application.packageName).isEqualTo("org.robolectric.testapp.test")
        }
    }
})
