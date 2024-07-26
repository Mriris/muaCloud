

package com.owncloud.android.utils

import android.accounts.Account
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.owncloud.android.R
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.presentation.authentication.ACTION_UPDATE_EXPIRED_TOKEN
import com.owncloud.android.presentation.authentication.EXTRA_ACCOUNT
import com.owncloud.android.presentation.authentication.EXTRA_ACTION
import com.owncloud.android.presentation.authentication.LoginActivity
import com.owncloud.android.presentation.conflicts.ConflictsResolveActivity
import com.owncloud.android.presentation.settings.SettingsActivity
import com.owncloud.android.presentation.settings.SettingsActivity.Companion.KEY_NOTIFICATION_INTENT
import com.owncloud.android.ui.activity.UploadListActivity
import java.util.Random

object NotificationUtils {

    const val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    @JvmStatic
    fun newNotificationBuilder(context: Context, channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId).apply {
            color = ContextCompat.getColor(context, R.color.primary)
            setSmallIcon(R.drawable.notification_icon)
        }
    }

    fun createBasicNotification(
        context: Context,
        contentTitle: String,
        contentText: String,
        notificationChannelId: String,
        notificationId: Int,
        intent: PendingIntent?,
        onGoing: Boolean = false,
        timeOut: Long?,
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = newNotificationBuilder(context, notificationChannelId).apply {
            setContentTitle(contentTitle)
            color = ContextCompat.getColor(context, R.color.primary)
            setSmallIcon(R.drawable.notification_icon)
            setWhen(System.currentTimeMillis())
            setContentText(contentText)
            setOngoing(onGoing)
            setAutoCancel(true)
        }

        intent?.let {
            notificationBuilder.setContentIntent(it)
        }

        timeOut?.let {


            notificationBuilder.setTimeoutAfter(it)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())

        if (SDK_INT < Build.VERSION_CODES.O && timeOut != null) {
            cancelWithDelay(
                notificationManager = notificationManager,
                notificationId = notificationId,
                delayInMillis = timeOut
            )
        }
    }

    fun composePendingIntentToRefreshCredentials(context: Context, account: Account): PendingIntent {
        val updateCredentialsIntent =
            Intent(context, LoginActivity::class.java).apply {
                putExtra(EXTRA_ACCOUNT, account)
                putExtra(EXTRA_ACTION, ACTION_UPDATE_EXPIRED_TOKEN)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_FROM_BACKGROUND)
            }

        val pendingIntent = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT

        return PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), updateCredentialsIntent, pendingIntent)
    }

    fun composePendingIntentToUploadList(context: Context): PendingIntent {
        val showUploadListIntent = Intent(context, UploadListActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        return PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), showUploadListIntent, pendingIntentFlags)
    }

    fun composePendingIntentToCameraUploads(context: Context, notificationKey: String): PendingIntent {
        val openSettingsActivity = Intent(context, SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(KEY_NOTIFICATION_INTENT, notificationKey)
        }

        return PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), openSettingsActivity, pendingIntentFlags)
    }

    @JvmStatic
    fun cancelWithDelay(
        notificationManager: NotificationManager,
        notificationId: Int,
        delayInMillis: Long
    ) {
        val thread = HandlerThread(
            "NotificationDelayerThread_" + Random(System.currentTimeMillis()).nextInt(),
            Process.THREAD_PRIORITY_BACKGROUND
        )
        thread.start()
        val handler = Handler(thread.looper)
        handler.postDelayed({
            notificationManager.cancel(notificationId)
            (Thread.currentThread() as HandlerThread).looper.quit()
        }, delayInMillis)
    }


    @JvmStatic
    fun notifyConflict(fileInConflict: OCFile, account: Account?, context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = newNotificationBuilder(context, FILE_SYNC_CONFLICT_NOTIFICATION_CHANNEL_ID)
        notificationBuilder
            .setTicker(context.getString(R.string.conflict_title))
            .setContentTitle(context.getString(R.string.conflict_title))
            .setContentText(
                String.format(
                    context.getString(R.string.conflict_description),
                    fileInConflict.remotePath
                )
            )
            .setAutoCancel(true)
        val showConflictActivityIntent = Intent(context, ConflictsResolveActivity::class.java)
        showConflictActivityIntent.flags = showConflictActivityIntent.flags or Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_FROM_BACKGROUND
        showConflictActivityIntent.putExtra(ConflictsResolveActivity.EXTRA_FILE, fileInConflict)
        notificationBuilder.setContentIntent(
            PendingIntent.getActivity(
                context, System.currentTimeMillis().toInt(),
                showConflictActivityIntent, pendingIntentFlags
            )
        )
        var notificationId = 0

        if (fileInConflict.id!!.toInt() >= Int.MIN_VALUE && fileInConflict.id!!.toInt() <= Int.MAX_VALUE) {
            notificationId = fileInConflict.id!!.toInt()
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
