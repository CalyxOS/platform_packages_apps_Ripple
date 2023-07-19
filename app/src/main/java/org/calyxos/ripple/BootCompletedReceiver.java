package org.calyxos.ripple;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

import info.guardianproject.panic.PanicTrigger;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String panicChannelID = "panic";
    private static final int oneShotMigrationNotificationID = 100;
    private static final String oneShotMigrationNotification = "oneShotMigrationNotification";
    private static final String panicPreferences = "panicPreferences";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != null && Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            // Check and post migration notification if required
            handleOneShotNotification(context);
        }
    }

    private void handleOneShotNotification(Context ctx) {
        SharedPreferences sharedPreferences = ctx
                .getSharedPreferences(panicPreferences, Context.MODE_PRIVATE);

        boolean shouldShowNotification =
                !sharedPreferences.getBoolean(oneShotMigrationNotification, false)
                        && PanicTrigger.getEnabledResponders(ctx).contains("org.fdroid.fdroid")
                        && PanicTrigger.getAllResponders(ctx).contains("org.calyxos.panic");

        if (shouldShowNotification) {
            NotificationManager notificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(ctx, notificationManager);
            }
            notificationManager.notify(oneShotMigrationNotificationID, getNotification(ctx));
        }

        sharedPreferences.edit().putBoolean(oneShotMigrationNotification, true).apply();
    }

    private Notification getNotification(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                "org.calyxos.ripple",
                "org.calyxos.ripple.SettingsActivityLink"
        ));

        PendingIntent contentIntent = PendingIntent
                .getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, panicChannelID)
                .setSmallIcon(R.drawable.ic_ripple_trigger)
                .setContentTitle(context.getString(R.string.discover_panic_title))
                .setContentText(context.getString(R.string.discover_panic_desc))
                .setContentIntent(contentIntent)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM)
                .setAutoCancel(true)
                .build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(Context ctx, NotificationManager notificationManager) {
        NotificationChannel notificationChannel = new NotificationChannel(
                panicChannelID,
                ctx.getString(R.string.panic_channel_title),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationChannel.setDescription(ctx.getString(R.string.panic_channel_desc));
        notificationManager.createNotificationChannel(notificationChannel);
    }
}