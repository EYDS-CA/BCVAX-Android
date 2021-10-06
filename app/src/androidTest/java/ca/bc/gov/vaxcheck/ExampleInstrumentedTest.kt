package ca.bc.gov.vaxcheck

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("ca.bc.gov.vaxcheck", appContext.packageName)
    }


    @Test
    fun testFlexibleUpdate_Completes() {

        val fakeAppUpdateManager =
            FakeAppUpdateManager(InstrumentationRegistry.getInstrumentation().targetContext)

        // Setup flexible update.
        fakeAppUpdateManager.setUpdateAvailable(2)

        ActivityScenario.launch(MainActivity::class.java)

        sleep(2000)

        // Validate that flexible update is prompted to the user.
        //assertTrue(fakeAppUpdateManager.isConfirmationDialogVisible)

        // Simulate user's and download behavior.
        fakeAppUpdateManager.userAcceptsUpdate()

        fakeAppUpdateManager.downloadStarts()

        fakeAppUpdateManager.downloadCompletes()

        fakeAppUpdateManager.installCompletes()
    }
}
