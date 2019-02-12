package tony.dev.mohamed.myuberridder.FirebaseService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import tony.dev.mohamed.myuberridder.R;
import tony.dev.mohamed.myuberridder.RatingActivity;
import tony.dev.mohamed.myuberridder.helper.Common;
import tony.dev.mohamed.myuberridder.helper.NotificationHelper;
import tony.dev.mohamed.myuberridder.models.Token;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateTkenToService(s);
    }

    private void updateTkenToService(String tokenStr) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Token token = new Token(tokenStr);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            reference.child(Common.tokens_tb).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
        }
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        Toast.makeText(this, "canclled", Toast.LENGTH_SHORT).show();
        if(remoteMessage.getData()!=null) {
            Map<String, String> data = remoteMessage.getData();
            final String title = data.get("title");
            final String message = data.get("message");
            if (title != null) {
                if (title.equals("Cancel")) {
                    android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                    //Handler handler1=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                        }
                    });

                    LocalBroadcastManager.getInstance(MyFirebaseMessagingService.this)
                            .sendBroadcast(new Intent(Common.CANCEL_BROADCAST_STRING));
                } else if (title.equals("Arrived")) {
                    android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                    //Handler handler1=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                        }
                    });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showArriveNotificationAPI26(message);
                    } else {
                        showArriveNotification(message);
                    }

                } else if (title.equals("DropOff")) {
                    android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                    //Handler handler1=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                        }
                    });
                    opentRateActivity(remoteMessage.getNotification());

                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArriveNotificationAPI26(String notification) {
        // Creating a pending intent and wrapping our intent
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext(), defaultSoundUri);
        Notification.Builder builder = notificationHelper.getNotification1("Arrived", notification, pendingIntent);
        notificationHelper.notify(1, builder);
    }

    private void opentRateActivity(RemoteMessage.Notification notification) {
        LocalBroadcastManager.getInstance(MyFirebaseMessagingService.this)
                .sendBroadcast(new Intent(Common.BROADCAST_DROPOFF));
        Intent intent = new Intent(getBaseContext(), RatingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showArriveNotification(String body) {
        // Creating a pending intent and wrapping our intent
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Arrived")
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());


    }
}


