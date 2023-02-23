package com.udacity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

private const val NOTIFICATION_ID = 0
const val FILE_NAME = "FILE_NAME"
const val DOWNLOAD_STATUS = "DOWNLOAD_STATUS"
private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

fun NotificationManager.sendNotification(
    fileName: String,
    downloadStatus: DownloadStatus,
    notificationTitle: String,
    notificationDescription: String,
    applicationContext: Context
) {
    val contentIntent = Intent(applicationContext, DetailActivity::class.java)
    contentIntent.apply {
        putExtra(FILE_NAME, fileName)
        putExtra(DOWNLOAD_STATUS, downloadStatus.status)
    }

    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        FLAGS
    )

    val builder = getNotificationBuilder(
        applicationContext = applicationContext,
        contentPendingIntent = contentPendingIntent,
        messageTitle = notificationTitle,
        messageBody = notificationDescription
    )
    notify(NOTIFICATION_ID, builder.build())
}

private fun getNotificationBuilder(
    applicationContext: Context,
    contentPendingIntent: PendingIntent?,
    messageTitle: String,
    messageBody: String
): NotificationCompat.Builder {
    return NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.download_notification_channel_id)
    )
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(messageTitle)
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .addAction(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_button),
            contentPendingIntent
        )
}


fun NotificationManager.createNotificationChannel(context: Context){
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val notificationChannel = android.app.NotificationChannel(
            context.getString(R.string.download_notification_channel_id),
            context.getString(R.string.download_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = android.graphics.Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description =
            context.getString(R.string.download_notification_channel_description)
        this.createNotificationChannel(notificationChannel)
    }
}
