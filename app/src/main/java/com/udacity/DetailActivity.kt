package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val fileName = intent.getStringExtra(FILE_NAME)
        val status = intent.getStringExtra(DOWNLOAD_STATUS)

        tv_file_name.text = fileName
        tv_status.apply {
            text = status
            setTextColor(
                when (text) {
                    DownloadStatus.SUCCESS.status -> ContextCompat.getColor(
                        this@DetailActivity,
                        R.color.colorPrimaryDark
                    )
                    else -> ContextCompat.getColor(this@DetailActivity, R.color.colorRed)
                }
            )
        }
        btn_ok.setOnClickListener { finish() }
    }
}
