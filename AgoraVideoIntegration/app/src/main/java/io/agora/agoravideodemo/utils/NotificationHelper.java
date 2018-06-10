package io.agora.agoravideodemo.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import io.agora.agoravideodemo.R;
import io.agora.agoravideodemo.RtcService;
import io.agora.agoravideodemo.ui.MainActivity;
import io.agora.agoravideodemo.ui.VideoChatViewActivity;


/**
 * Helper class to manage notification channels, and create notifications.
 */
public class NotificationHelper extends ContextWrapper {
    private NotificationManager manager;
    public static final String CALL_STATUS_CHANNEL = "CALL_STATUS_CHANNEL";
    public static final String SIGNALING_SERVICE_CHANNEL = "CALL_STATUS_CHANNEL";

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param ctx The application context
     */
    public NotificationHelper(Context ctx) {
        super(ctx);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                getManager().getNotificationChannel(CALL_STATUS_CHANNEL) == null) {
            NotificationChannel chan1 = new NotificationChannel(CALL_STATUS_CHANNEL,
                    getString(R.string.noti_channel_call), NotificationManager.IMPORTANCE_LOW);
            chan1.setLightColor(Color.GREEN);
            chan1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager().createNotificationChannel(chan1);
        }
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
    public NotificationCompat.Builder getNotification1(String title, String body) {

        Intent endCallIntent = new Intent(this, RtcService.class);
        endCallIntent.setAction(RtcService.ACTION_END_CALL);
        PendingIntent endCallPendingIntent =
                PendingIntent.getService(this, 0, endCallIntent, 0);


        Intent resultIntent = new Intent(this, VideoChatViewActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);


        return new NotificationCompat.Builder(getApplicationContext(), CALL_STATUS_CHANNEL)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getSmallIcon())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setOngoing(true)
                .setContentIntent(resultPendingIntent)
                .addAction(0, "End Call", endCallPendingIntent)
                .setAutoCancel(true);
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
    public NotificationCompat.Builder getSignalingNotification(String title, String body) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        return new NotificationCompat.Builder(getApplicationContext(), SIGNALING_SERVICE_CHANNEL)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(getSmallIcon())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setOngoing(true)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
    }

    /**
     * Send a notification.
     *
     * @param id           The ID of the notification
     * @param notification The notification object
     */
    public void notify(int id, Notification notification) {
        getManager().notify(id, notification);
    }

    /**
     * Get the small icon for this app
     *
     * @return The small icon resource id
     */
    private int getSmallIcon() {
        return R.drawable.ic_notification;
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