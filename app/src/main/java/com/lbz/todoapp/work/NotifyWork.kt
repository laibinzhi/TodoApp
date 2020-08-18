package com.lbz.todoapp.work

import android.app.Notification.DEFAULT_ALL
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color.RED
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lbz.todoapp.R
import com.lbz.todoapp.newtodo.AddTaskActivity
import java.util.*

/**
 * @author: laibinzhi
 * @date: 2020-08-17 16:07
 * @github: https://github.com/laibinzhi
 * @blog: https://www.laibinzhi.top/
 */
class NotifyWork(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    override fun doWork(): Result {
        val id = inputData.getInt(NOTIFICATION_ID, 0)
        val title = inputData.getString(TASK_TITLE) ?: "Title"
        sendNotification(id, title)
        return Result.success()
    }

    private fun sendNotification(id: Int, title: String) {
        val intent = Intent(applicationContext, AddTaskActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)

        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val subtitleNotification = "点击可进入Task详情$id"
        val pendingIntent = getActivity(applicationContext, 0, intent, 0)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title).setContentText(subtitleNotification)
            .setDefaults(DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true)

        notification.priority = PRIORITY_MAX

        if (SDK_INT >= O) {
            notification.setChannelId(NOTIFICATION_CHANNEL)

            val ringtoneManager = getDefaultUri(TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_RINGTONE)
                .setContentType(CONTENT_TYPE_SONIFICATION).build()

            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, IMPORTANCE_HIGH)

            channel.enableLights(true)
            channel.lightColor = RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.setSound(ringtoneManager, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id, notification.build())
    }


    companion object {
        const val NOTIFICATION_ID = "appName_notification_id"
        const val TASK_TITLE ="task_title"
        const val NOTIFICATION_NAME = "appName"
        const val NOTIFICATION_CHANNEL = "appName_channel_01"
    }
}