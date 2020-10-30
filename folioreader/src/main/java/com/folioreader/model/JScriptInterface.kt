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


class JScriptInterface(val mContext: Context, val title: String?) {

    @JavascriptInterface
    fun setIndexPlaying(index: Int) {
        currentIndexPlaying = index
    }

    @JavascriptInterface
    fun mediaAction(type: String?) {
        Log.e("Info", type!!)
        playing = Boolean.parseBoolean(type)
        if (playing) {
            createNotificationChannel()

            val expandedView = RemoteViews(mContext.packageName, R.layout.notification_ui)

            expandedView.setImageViewResource(R.id.icon, R.drawable.ic_sharp_contactless_24)
            expandedView.setTextViewText(R.id.title, "Testco")
            expandedView.setTextViewText(R.id.desc, "$title")
            expandedView.setImageViewResource(R.id.pausePlay,R.drawable.ic_baseline_pause_24)

            val pendingSwitchIntent = PendingIntent.getBroadcast(
                mContext, 0, Intent(
                    mContext,
                    NotificationListener::class.java
                ), 0
            )
            expandedView.setOnClickPendingIntent(R.id.pausePlay, pendingSwitchIntent)

            val builder = NotificationCompat.Builder(mContext, NotificationListener.channel_id)
                .setSmallIcon(R.drawable.ic_sharp_contactless_24)
                .setContentTitle("Heading")
                .setContentText("Content")
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(null)



            val notificationManager =
                mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(99, builder.build())


        } else {
            createNotificationChannel()

            val expandedView = RemoteViews(mContext.packageName, R.layout.notification_ui)

            expandedView.setImageViewResource(R.id.icon, R.drawable.ic_sharp_contactless_24)
            expandedView.setTextViewText(R.id.title, "Testco")
            expandedView.setTextViewText(R.id.desc, "$title")
            expandedView.setImageViewResource(R.id.pausePlay, R.drawable.ic_sharp_play_arrow_24)

            val pendingSwitchIntent = PendingIntent.getBroadcast(
                mContext, 0, Intent(
                    mContext,
                    NotificationListener::class.java
                ), 0
            )
            expandedView.setOnClickPendingIntent(R.id.pausePlay, pendingSwitchIntent)

            val builder = NotificationCompat.Builder(mContext, NotificationListener.channel_id)
                .setSmallIcon(R.drawable.ic_sharp_contactless_24)
                .setContentTitle("Heading")
                .setContentText("Content")
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(null)



            val notificationManager =
                mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(99, builder.build())

        }
    }

    companion object {
        var playing = false
        var currentIndexPlaying = -1;
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NotificationListener.channel_id,
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