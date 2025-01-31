

package com.owncloud.android

import android.app.Activity
import android.app.Application
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.content.pm.PackageInfoCompat
import com.owncloud.android.data.providers.implementation.OCSharedPreferencesProvider
import com.owncloud.android.datamodel.ThumbnailsCacheManager
import com.owncloud.android.db.PreferenceManager
import com.owncloud.android.dependecyinjection.commonModule
import com.owncloud.android.dependecyinjection.localDataSourceModule
import com.owncloud.android.dependecyinjection.remoteDataSourceModule
import com.owncloud.android.dependecyinjection.repositoryModule
import com.owncloud.android.dependecyinjection.useCaseModule
import com.owncloud.android.dependecyinjection.viewModelModule
import com.owncloud.android.domain.capabilities.usecases.GetStoredCapabilitiesUseCase
import com.owncloud.android.domain.spaces.model.OCSpace
import com.owncloud.android.domain.spaces.usecases.GetPersonalSpaceForAccountUseCase
import com.owncloud.android.extensions.createNotificationChannel
import com.owncloud.android.lib.common.SingleSessionManager
import com.owncloud.android.presentation.authentication.AccountUtils
import com.owncloud.android.presentation.migration.StorageMigrationActivity
import com.owncloud.android.presentation.releasenotes.ReleaseNotesActivity
import com.owncloud.android.presentation.security.biometric.BiometricActivity
import com.owncloud.android.presentation.security.biometric.BiometricManager
import com.owncloud.android.presentation.security.passcode.PassCodeActivity
import com.owncloud.android.presentation.security.passcode.PassCodeManager
import com.owncloud.android.presentation.security.pattern.PatternActivity
import com.owncloud.android.presentation.security.pattern.PatternManager
import com.owncloud.android.presentation.settings.logging.SettingsLogsFragment.Companion.PREFERENCE_ENABLE_LOGGING
import com.owncloud.android.providers.CoroutinesDispatcherProvider
import com.owncloud.android.providers.LogsProvider
import com.owncloud.android.providers.MdmProvider
import com.owncloud.android.ui.activity.FileDisplayActivity
import com.owncloud.android.ui.activity.FileDisplayActivity.Companion.PREFERENCE_CLEAR_DATA_ALREADY_TRIGGERED
import com.owncloud.android.ui.activity.WhatsNewActivity
import com.owncloud.android.utils.CONFIGURATION_ALLOW_SCREENSHOTS
import com.owncloud.android.utils.DOWNLOAD_NOTIFICATION_CHANNEL_ID
import com.owncloud.android.utils.DebugInjector
import com.owncloud.android.utils.FILE_SYNC_CONFLICT_NOTIFICATION_CHANNEL_ID
import com.owncloud.android.utils.FILE_SYNC_NOTIFICATION_CHANNEL_ID
import com.owncloud.android.utils.MEDIA_SERVICE_NOTIFICATION_CHANNEL_ID
import com.owncloud.android.utils.UPLOAD_NOTIFICATION_CHANNEL_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import timber.log.Timber


class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext

        startLogsIfEnabled() // 启动日志记录，如果已启用

        DebugInjector.injectDebugTools(appContext) // 注入调试工具

        createNotificationChannels() // 创建通知通道

        SingleSessionManager.setUserAgent(userAgent) // 设置用户代理

        ThumbnailsCacheManager.InitDiskCacheTask().execute()

        initDependencyInjection() // 初始化依赖注入

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Timber.d("${activity.javaClass.simpleName} onCreate(Bundle) starting")

                if (!areScreenshotsAllowed()) {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }

                if (activity !is PassCodeActivity &&
                    activity !is PatternActivity &&
                    activity !is BiometricActivity
                ) {
                    StorageMigrationActivity.runIfNeeded(activity)
                    if (isFirstRun()) {
                        WhatsNewActivity.runIfNeeded(activity)

                    } else {
                        ReleaseNotesActivity.runIfNeeded(activity)

                        val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
                        val clearDataAlreadyTriggered = pref.contains(PREFERENCE_CLEAR_DATA_ALREADY_TRIGGERED)
                        if (clearDataAlreadyTriggered || isNewVersionCode()) {
                            val dontShowAgainDialogPref = pref.getBoolean(PREFERENCE_KEY_DONT_SHOW_OCIS_ACCOUNT_WARNING_DIALOG, false)
                            if (!dontShowAgainDialogPref && shouldShowDialog(activity)) {
                                val checkboxDialog = activity.layoutInflater.inflate(R.layout.checkbox_dialog, null)
                                val checkbox = checkboxDialog.findViewById<CheckBox>(R.id.checkbox_dialog)
                                checkbox.setText(R.string.ocis_accounts_warning_checkbox_message)
                                val builder = AlertDialog.Builder(activity).apply {
                                    setView(checkboxDialog)
                                    setTitle(R.string.ocis_accounts_warning_title)
                                    setMessage(R.string.ocis_accounts_warning_message)
                                    setCancelable(false)
                                    setPositiveButton(R.string.ocis_accounts_warning_button) { _, _ ->
                                        if (checkbox.isChecked) {
                                            pref.edit().putBoolean(PREFERENCE_KEY_DONT_SHOW_OCIS_ACCOUNT_WARNING_DIALOG, true).apply()
                                        }
                                    }
                                }
                                val alertDialog = builder.create()
                                alertDialog.show()
                            }
                        } else { // “清除数据”按钮已从设备设置中的应用设置中按下。
                            AccountUtils.deleteAccounts(appContext)
                            WhatsNewActivity.runIfNeeded(activity)
                        }
                    }
                }

                PreferenceManager.migrateFingerprintToBiometricKey(applicationContext) // 将指纹迁移到生物识别键
                PreferenceManager.deleteOldSettingsPreferences(applicationContext) // 删除旧的设置首选项
            }

            private fun shouldShowDialog(activity: Activity) =
                runBlocking(CoroutinesDispatcherProvider().io) {
                    if (activity !is FileDisplayActivity) return@runBlocking false
                    val account = AccountUtils.getCurrentOwnCloudAccount(appContext) ?: return@runBlocking false

                    val getStoredCapabilitiesUseCase: GetStoredCapabilitiesUseCase by inject()
                    val capabilities = withContext(CoroutineScope(CoroutinesDispatcherProvider().io).coroutineContext) {
                        getStoredCapabilitiesUseCase(
                            GetStoredCapabilitiesUseCase.Params(
                                accountName = account.name
                            )
                        )
                    }
                    val spacesAllowed = capabilities != null && capabilities.isSpacesAllowed()

                    var personalSpace: OCSpace? = null
                    if (spacesAllowed) {
                        val getPersonalSpaceForAccountUseCase: GetPersonalSpaceForAccountUseCase by inject()
                        personalSpace = withContext(CoroutineScope(CoroutinesDispatcherProvider().io).coroutineContext) {
                            getPersonalSpaceForAccountUseCase(
                                GetPersonalSpaceForAccountUseCase.Params(
                                    accountName = account.name
                                )
                            )
                        }
                    }

                    spacesAllowed && personalSpace == null
                }

            override fun onActivityStarted(activity: Activity) {
                Timber.v("${activity.javaClass.simpleName} onStart() starting")
                PassCodeManager.onActivityStarted(activity)
                PatternManager.onActivityStarted(activity)
                BiometricManager.onActivityStarted(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                Timber.v("${activity.javaClass.simpleName} onResume() starting")
            }

            override fun onActivityPaused(activity: Activity) {
                Timber.v("${activity.javaClass.simpleName} onPause() ending")
            }

            override fun onActivityStopped(activity: Activity) {
                Timber.v("${activity.javaClass.simpleName} onStop() ending")
                PassCodeManager.onActivityStopped(activity)
                PatternManager.onActivityStopped(activity)
                BiometricManager.onActivityStopped(activity)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Timber.v("${activity.javaClass.simpleName} onSaveInstanceState(Bundle) starting")
            }

            override fun onActivityDestroyed(activity: Activity) {
                Timber.v("${activity.javaClass.simpleName} onDestroy() ending")
            }
        })

    }

    private fun startLogsIfEnabled() {
        val preferenceProvider = OCSharedPreferencesProvider(applicationContext)

        if (BuildConfig.DEBUG) {
            val alreadySet = preferenceProvider.containsPreference(PREFERENCE_ENABLE_LOGGING)
            if (!alreadySet) {
                preferenceProvider.putBoolean(PREFERENCE_ENABLE_LOGGING, true)
            }
        }

        enabledLogging = preferenceProvider.getBoolean(PREFERENCE_ENABLE_LOGGING, false)

        if (enabledLogging) {
            val mdmProvider = MdmProvider(applicationContext)
            LogsProvider(applicationContext, mdmProvider).startLogging()
        }
    }


    private fun areScreenshotsAllowed(): Boolean {
        if (BuildConfig.DEBUG) return true

        val mdmProvider = MdmProvider(applicationContext)
        return mdmProvider.getBrandingBoolean(CONFIGURATION_ALLOW_SCREENSHOTS, R.bool.allow_screenshots)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        createNotificationChannel(
            id = DOWNLOAD_NOTIFICATION_CHANNEL_ID,
            name = getString(R.string.download_notification_channel_name),
            description = getString(R.string.download_notification_channel_description),
            importance = IMPORTANCE_LOW
        )

        createNotificationChannel(
            id = UPLOAD_NOTIFICATION_CHANNEL_ID,
            name = getString(R.string.upload_notification_channel_name),
            description = getString(R.string.upload_notification_channel_description),
            importance = IMPORTANCE_LOW
        )

        createNotificationChannel(
            id = MEDIA_SERVICE_NOTIFICATION_CHANNEL_ID,
            name = getString(R.string.media_service_notification_channel_name),
            description = getString(R.string.media_service_notification_channel_description),
            importance = IMPORTANCE_LOW
        )

        createNotificationChannel(
            id = FILE_SYNC_CONFLICT_NOTIFICATION_CHANNEL_ID,
            name = getString(R.string.file_sync_conflict_notification_channel_name),
            description = getString(R.string.file_sync_conflict_notification_channel_description),
            importance = IMPORTANCE_LOW
        )

        createNotificationChannel(
            id = FILE_SYNC_NOTIFICATION_CHANNEL_ID,
            name = getString(R.string.file_sync_notification_channel_name),
            description = getString(R.string.file_sync_notification_channel_description),
            importance = IMPORTANCE_LOW
        )
    }

    private fun isFirstRun(): Boolean {
        if (getLastSeenVersionCode() != 0) {
            return false
        }
        return AccountUtils.getCurrentOwnCloudAccount(appContext) == null
    }

    companion object {
        const val MDM_FLAVOR = "mdm"

        lateinit var appContext: Context
            private set
        var enabledLogging: Boolean = false
            private set

        const val PREFERENCE_KEY_LAST_SEEN_VERSION_CODE = "lastSeenVersionCode"

        const val PREFERENCE_KEY_DONT_SHOW_OCIS_ACCOUNT_WARNING_DIALOG = "PREFERENCE_KEY_DONT_SHOW_OCIS_ACCOUNT_WARNING_DIALOG"



        val accountType: String
            get() = appContext.resources.getString(R.string.account_type)

        val versionCode: Int
            get() {
                return try {
                    val pInfo: PackageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
                    val longVersionCode: Long = PackageInfoCompat.getLongVersionCode(pInfo)
                    longVersionCode.toInt()
                } catch (e: PackageManager.NameNotFoundException) {
                    0
                }
            }

        val authority: String
            get() = appContext.resources.getString(R.string.authority)

        val authTokenType: String
            get() = appContext.resources.getString(R.string.authority)

        val dataFolder: String
            get() = appContext.resources.getString(R.string.data_folder)


        val userAgent: String
            get() {
                val appString = appContext.resources.getString(R.string.user_agent)
                val packageName = appContext.packageName
                var version = ""

                val pInfo: PackageInfo?
                try {
                    pInfo = appContext.packageManager.getPackageInfo(packageName, 0)
                    if (pInfo != null) {
                        version = pInfo.versionName
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    Timber.e(e, "Trying to get packageName")
                }

                return String.format(appString, version)
            }

        fun initDependencyInjection() {
            stopKoin()
            startKoin {
                androidContext(appContext)
                modules(
                    listOf(
                        commonModule,
                        viewModelModule,
                        useCaseModule,
                        repositoryModule,
                        localDataSourceModule,
                        remoteDataSourceModule
                    )
                )
            }
        }

        fun getLastSeenVersionCode(): Int {
            val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
            return pref.getInt(PREFERENCE_KEY_LAST_SEEN_VERSION_CODE, 0)
        }

        private fun isNewVersionCode(): Boolean {
            val lastSeenVersionCode = getLastSeenVersionCode()
            if (lastSeenVersionCode == 0) { // 偏好设置已被删除，因此我们可以删除账户并导航到登录
                return false
            }
            return lastSeenVersionCode != versionCode // 版本已更改，不必删除账户
        }
    }
}

