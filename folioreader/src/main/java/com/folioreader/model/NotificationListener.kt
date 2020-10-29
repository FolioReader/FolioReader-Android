package com.folioreader.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.webkit.WebView
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.folioreader.R

var webView: WebView? = null
var audioFocusRequest : AudioFocusRequest? = null
class NotificationListener : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (JScriptInterface.playing) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest =
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .build()
                getAudioManager(context)!!.requestAudioFocus(audioFocusRequest)
            } else {
                getAudioManager(context)!!.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }

            createNotificationChannel(context)
            val expandedView = RemoteViews(context.packageName, R.layout.notification_ui)

            expandedView.setImageViewResource(R.id.icon, R.drawable.ic_drawer)
            expandedView.setTextViewText(R.id.title, "Test")
            expandedView.setTextViewText(R.id.desc, "This is a description. - PAUSED")
            expandedView.setImageViewResource(R.id.pausePlay, android.R.drawable.ic_media_play)


            val pendingSwitchIntent = PendingIntent.getBroadcast(
                context, 0, Intent(
                    context,
                    NotificationListener::class.java
                ), 0
            )
            expandedView.setOnClickPendingIntent(R.id.pausePlay, pendingSwitchIntent)


            val builder = NotificationCompat.Builder(context, "0")
                .setSmallIcon(R.drawable.ic_drawer)
                .setContentTitle("Heading")
                .setContentText("Content")
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(null)



            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, builder.build())

            JScriptInterface.playing = false

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioFocusRequest != null) {
                    getAudioManager(context)?.abandonAudioFocusRequest(audioFocusRequest!!);
                    audioFocusRequest = null;
                }
            } else {
                getAudioManager(context)!!.abandonAudioFocus(null);
            }

            createNotificationChannel(context)
            val expandedView = RemoteViews(context.packageName, R.layout.notification_ui)

            expandedView.setImageViewResource(R.id.icon, R.drawable.ic_drawer)
            expandedView.setTextViewText(R.id.title, "Test")
            expandedView.setTextViewText(R.id.desc, "This is a description. - PLAYING")
            expandedView.setImageViewResource(R.id.pausePlay, android.R.drawable.ic_media_pause)
            val pendingSwitchIntent = PendingIntent.getBroadcast(
                context, 0, Intent(
                    context,
                    NotificationListener::class.java
                ), 0
            )
            expandedView.setOnClickPendingIntent(R.id.pausePlay, pendingSwitchIntent)
            val builder = NotificationCompat.Builder(context, "0")
                .setSmallIcon(R.drawable.ic_drawer)
                .setContentTitle("Heading")
                .setContentText("Content")
                .setCustomBigContentView(expandedView)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(null)



            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, builder.build())
            JScriptInterface.playing = true

        }

    }

    private fun createNotificationChannel(mcontext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "0",
                "Testco Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            serviceChannel.setSound(null, null)
            val manager: NotificationManager =
                mcontext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getAudioManager(context: Context): AudioManager? {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}