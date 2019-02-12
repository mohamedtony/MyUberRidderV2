package tony.dev.mohamed.myuberridder.helper;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import tony.dev.mohamed.myuberridder.R;

/**
 * Helper class to manage notification channels, and create notifications.
 */
public class NotificationHelper extends ContextWrapper {
    public static final String MYUBERRIDDER_CHANNEL_ID = "tony.dev.mohamed.myuberridder";
    public static final String MYUBERRIDDER_NAME = "myUberRidder";
    private NotificationManager manager;

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context ctx, Uri sound) {
        super(ctx);

        NotificationChannel chan1 = new NotificationChannel(MYUBERRIDDER_CHANNEL_ID,
                MYUBERRIDDER_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        chan1.enableLights(true);
        chan1.enableVibration(true);
        chan1.setSound(sound, chan1.getAudioAttributes());
        chan1.setLightColor(Color.GREEN);
        chan1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(chan1);
/*
        NotificationChannel chan2 = new NotificationChannel(SECONDARY_CHANNEL,
                getString(R.string.noti_channel_second), NotificationManager.IMPORTANCE_HIGH);
        chan2.setLightColor(Color.BLUE);
        chan2.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(chan2);*/
    }

    /**
     * Get a notification of type 1
     * <p>
     * Provide the builder rather than the notification it's self as useful for making notification
     * changes.
     *
     * @param title the title of the notification
     * @param body  the body text for the notification
     * @return the builder as it keeps a reference to the notification (since API 24)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification1(String title, String body, PendingIntent pendingIntent) {
        return new Notification.Builder(getApplicationContext(), MYUBERRIDDER_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_noti_car)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
    }

    /**
     * Build notification for secondary channel.
     *
     * @param title Title for notification.
     * @param body Message for notification.
     * @return A Notification.Builder configured with the selected channel and details
     */
/*    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification2(String title, String body) {
        return new Notification.Builder(getApplicationContext(), SECONDARY_CHANNEL)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getSmallIcon())
                .setAutoCancel(true);
    }*/

    /**
     * Send a notification.
     *
     * @param id           The ID of the notification
     * @param notification The notification object
     */
    public void notify(int id, Notification.Builder notification) {
        getManager().notify(id, notification.build());
    }

    /**
     * Get the small icon for this app
     *
     * @return The small icon resource id
     */
    private int getSmallIcon() {
        return android.R.drawable.stat_notify_chat;
    }

    /**
     * Get the notification manager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
}