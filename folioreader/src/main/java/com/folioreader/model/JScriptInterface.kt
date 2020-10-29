package com.folioreader.model

import android.app.NotificationChannel
import com.folioreader.R

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import java.lang.Boolean


class JScriptInterface(val mContext: Context) {
    @JavascriptInterface
    fun mediaAction(type: String?) {
        Log.e("Info", type!!)
        playing = Boolean.parseBoolean(type)
        if (playing) {
            createNotificationChannel()

            val expandedView = RemoteViews(mContext.packageName, R.layout.notification_ui)

            expandedView.setImageViewResource(R.id.icon, R.drawable.ic_drawer)
            expandedView.setTextViewText(R.id.title, "Test")
            expandedView.setTextViewText(R.id.desc, "TEST - PLAYING123")
            expandedView.setImageViewResource(R.id.pausePlay,android.R.drawable.ic_media_pause)

            val pendingSwitchIntent = PendingIntent.getBroadcast(
                mContext, 0, Intent(
                    mContext,
                    NotificationListener::class.java
                ), 0
            )
            expandedView.setOnClickPendingIntent(R.id.pausePlay, pendingSwitchIntent)

            val builder = NotificationCompat.Builder(mContext, "0")
                .setSmallIcon(R.drawable.ic_drawer)
                .setContentTitle("Heading")
                .setContentText("Content")
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(null)



            val notificationManager =
                mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, builder.build())


        } else {
            createNotificationChannel()

            val expandedView = RemoteViews(mContext.packageName, R.layout.notification_ui)

            expandedView.setImageViewResource(R.id.icon, R.drawable.ic_drawer)
            expandedView.setTextViewText(R.id.title, "Test")
            expandedView.setTextViewText(R.id.desc, "TEST - PAUSED123")
            expandedView.setImageViewResource(R.id.pausePlay, android.R.drawable.ic_media_play)

            val pendingSwitchIntent = PendingIntent.getBroadcast(
                mContext, 0, Intent(
                    mContext,
                    NotificationListener::class.java
                ), 0
            )
            expandedView.setOnClickPendingIntent(R.id.pausePlay, pendingSwitchIntent)

            val builder = NotificationCompat.Builder(mContext, "0")
                .setSmallIcon(R.drawable.ic_drawer)
                .setContentTitle("Heading")
                .setContentText("Content")
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(null)



            val notificationManager =
                mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, builder.build())

        }
    }

    companion object {
        var playing = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "0",
                "Testco Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            serviceChannel.setSound(null,null)
            val manager: NotificationManager =
                mContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getAudioManager(context: Context): AudioManager? {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}