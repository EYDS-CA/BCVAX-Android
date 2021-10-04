package ca.bc.gov.vaxcheck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installStatus
import dagger.hilt.android.AndroidEntryPoint


/**
 * [MainActivity]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.canonicalName

    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAppUpdate()
    }

    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all app entry points.
    override fun onResume() {
        super.onResume()

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popUpSnackBarForCompleteUpdate()
                } else if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            REQUEST_CODE_IMMEDIATE
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, e.message.toString())
                    }
                }
            }
    }

    private fun checkAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)

        // Before starting an update, register a listener for updates.
        appUpdateManager.registerListener(listener)

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.updatePriority() < 4 /* low/medium priority */
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this,
                        REQUEST_CODE_FLEXIBLE
                    )
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.updatePriority() >= 4 /* high priority */) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        REQUEST_CODE_IMMEDIATE
                    )
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FLEXIBLE) {
            when (resultCode) {
                RESULT_OK -> {
                    showInfoInSnackBar(getString(R.string.update_success))
                }
                RESULT_CANCELED -> {
                    showInfoInSnackBar(getString(R.string.update_cancelled))
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    showInfoInSnackBar(getString(R.string.update_failed))
                }
            }
        } else if (requestCode == REQUEST_CODE_IMMEDIATE) {
            when (resultCode) {
                RESULT_CANCELED -> {
                    showInfoInSnackBar(getString(R.string.update_cancelled))
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    showInfoInSnackBar(getString(R.string.update_failed))
                }
            }
        }
    }

    // Create a listener to track request state updates.
    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            // Show update progress bar. Not required for this app
        } else if (state.installStatus == InstallStatus.DOWNLOADED) {
            popUpSnackBarForCompleteUpdate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // When status updates are no longer needed, unregister the listener.
        appUpdateManager.unregisterListener(listener)
    }

    private fun showInfoInSnackBar(message: String) {
        Snackbar.make(
            findViewById(R.id.nav_host_fragment),
            message,
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(resources.getString(R.string.btn_ok)) { dismiss() }
            setActionTextColor(resources.getColor(R.color.yellow, null))
            show()
        }
    }

    private fun popUpSnackBarForCompleteUpdate() {
        Snackbar.make(
            findViewById(R.id.nav_host_fragment),
            getString(R.string.update_downloaded),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.restart)) { appUpdateManager.completeUpdate() }
            setActionTextColor(resources.getColor(R.color.yellow, null))
            show()
        }
    }

    companion object {
        private const val REQUEST_CODE_IMMEDIATE = 1
        private const val REQUEST_CODE_FLEXIBLE = 2
    }
}
