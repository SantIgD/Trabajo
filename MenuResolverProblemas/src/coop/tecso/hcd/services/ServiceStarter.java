package coop.tecso.hcd.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class ServiceStarter {

    public static void startService(Context context, Intent serviceIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    public static void startForeground(Service service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Context context = service.getApplicationContext();
            Notification notification = createForegroundNotification(context);
            service.startForeground(1, notification);
        }
    }

    private static Notification createForegroundNotification(Context context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return new Notification();
        }

        String NOTIFICATION_CHANNEL_ID = "com.fantommers.HCDigital";
        String channelName = "HC Background Service";

        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setPriority(NotificationManager.IMPORTANCE_MIN);
        notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        return notificationBuilder.build();
    }
}
