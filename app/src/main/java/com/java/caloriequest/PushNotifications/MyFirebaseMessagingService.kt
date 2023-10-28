package com.java.caloriequest.PushNotifications



import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.java.caloriequest.R
import android.app.PendingIntent
import android.content.Intent
import com.java.caloriequest.Activities.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the incoming FCM message and create a PendingIntent to open MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        // Create a PendingIntent to open MainActivity
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        // Create and display the notification
        val notification = NotificationCompat.Builder(this, "your_notification_channel_id")
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle("Water Intake Reminder!!")
            .setContentText("Stay Hydrated, It's Time to Sip and Refresh!")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
    }
}

