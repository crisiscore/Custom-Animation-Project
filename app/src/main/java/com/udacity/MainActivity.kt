package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var downloadUrl: DownloadUrl? = null
    private var downloadStatus: DownloadStatus = DownloadStatus.UNAVAILABLE

    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initializeNotificationManager()
        registerBroadcastReceiver()

        custom_button.setOnClickListener {
            if (downloadUrl != null) {
                custom_button.setButtonState(ButtonState.Loading).apply {
                    download()
                }
            } else {
                showToastMessage(getString(R.string.select_download_file_message))
            }
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            downloadUrl = when (checkedId) {
                R.id.rbGlide -> DownloadUrl.GLIDE
                R.id.rbLoadApp -> DownloadUrl.LOAD_APP
                R.id.rbRetrofit -> DownloadUrl.RETROFIT
                else -> null
            }
        }
    }

    private fun initializeNotificationManager() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(context = this)
    }

    private fun registerBroadcastReceiver() {
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun showToastMessage(toastMessage: String, context: Context = this) {
        Toast.makeText(
            context,
            toastMessage,
            Toast.LENGTH_SHORT
        ).show()
    }


    private val fileName: String
        get() = when (downloadUrl) {
            DownloadUrl.GLIDE -> getString(R.string.glide_label)
            DownloadUrl.LOAD_APP -> getString(R.string.load_app_label)
            DownloadUrl.RETROFIT -> getString(R.string.retrofit_label)
            else -> getString(R.string.app_name)
        }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val extraDownloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (extraDownloadId == downloadID) {
                val downloadManager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(extraDownloadId))

                if (cursor.moveToFirst()) {
                    downloadStatus =
                        when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                DownloadStatus.SUCCESS
                            }
                            DownloadManager.STATUS_FAILED -> {
                                DownloadStatus.FAILURE
                            }
                            else -> {
                                DownloadStatus.UNAVAILABLE
                            }
                        }
                }

                notificationManager.sendNotification(
                    fileName = fileName,
                    downloadStatus = downloadStatus,
                    notificationTitle = getString(R.string.notification_title),
                    notificationDescription = getString(R.string.notification_description),
                    applicationContext = applicationContext
                )
            }
        }
    }

    private fun download() {
        downloadUrl?.let {
            val request =
                DownloadManager.Request(Uri.parse(it.url))
                    .setTitle(getString(R.string.notification_title))
                    .setDescription(getString(R.string.app_name))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID = downloadManager.enqueue(request)
        }
    }

}
