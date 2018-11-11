package com.rebataur.forexapp.firebasemessaging;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rebataur.forexapp.MainActivity;
import com.rebataur.forexapp.R;
import com.rebataur.forexapp.Splashscreen;
import com.rebataur.forexapp.utils.AjaxCall;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    public static final String CHANNEL_1_ID = "rupeeRemit";

    public static NotificationManagerCompat notificationManager;

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        AjaxCall.storeFCMToken(token, new AjaxCall.APICallback() {
            @Override
            public void apiSuccess(JSONObject jsonObject) {
                Log.e("NewToken Sent","YES");
            }

            @Override
            public void apiFailure(Exception e) {
                Log.e("NewToken Sent","NO");

            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "FROM:" + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Mesage body:" + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle());
        }
    }

    private void sendNotification(String body, String title) {

        SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        int notificationNumber = prefs.getInt("notificationNumber", 0);

        PendingIntent pendingIntent;

        Intent intent = new Intent(this, Splashscreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        pendingIntent = PendingIntent.getActivity(this, 0/*Request code*/, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(notificationNumber, notification);
        SharedPreferences.Editor editor = prefs.edit();
        notificationNumber++;
        editor.putInt("notificationNumber", notificationNumber);
        editor.apply();
    }
}
