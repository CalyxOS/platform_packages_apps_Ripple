package org.calyxos.ripple;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

import info.guardianproject.panic.PanicTrigger;

public class BootCompletedReceiver extends BroadcastReceiver {

    public static final String EXTRA_FROM_NOTIFICATION = "fromNotification";
    public static final String ONE_SHOT_MIGRATION_NOTIFICATION_KEY = "oneShotMigrationNotification";
    public static final String PANIC_SHARED_PREFS = "panicPreferences";

    private static final String panicChannelID = "panic";
    private static final String updateInfoChannelID = "updateInfo";
    private static final int oneShotMigrationNotificationID = 100;
    private static final int oneShotUpdateInfoNotificationID = 101;

    private static final String UPDATE_INFO_URL = "https://calyxos.org/pixel4-faceunlock";

    // Mirrored from PanicKit
    private static final String ENABLED_SHARED_PREFS =
            "info.guardianproject.panic.PanicTrigger.ENABLED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != null && Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            // Check and post migration notification if required
            handleOneShotNotification(context);
        }
    }

    private void handleOneShotNotification(Context ctx) {
        SharedPreferences panicPreferences = ctx
                .getSharedPreferences(PANIC_SHARED_PREFS, Context.MODE_PRIVATE);

        // Get shared preferences created by PanicKit for enabled apps
        SharedPreferences enabledPreferences = ctx
                .getSharedPreferences(ENABLED_SHARED_PREFS, Context.MODE_PRIVATE);

        boolean shouldShowNotification =
                !panicPreferences.getBoolean(ONE_SHOT_MIGRATION_NOTIFICATION_KEY, false)
                        && enabledPreferences.getBoolean("org.fdroid.fdroid", false)
                        && PanicTrigger.getAllResponders(ctx).contains("org.calyxos.panic");

        if (shouldShowNotification) {
            NotificationManager notificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(ctx, notificationManager);
            }
            notificationManager.notify(oneShotMigrationNotificationID, getNotification(ctx));
        } else {
            panicPreferences.edit().putBoolean(ONE_SHOT_MIGRATION_NOTIFICATION_KEY, true).apply();
        }

        boolean shouldShowUpdateInfoNotification =
                Build.PRODUCT.equals("flame") || Build.PRODUCT.equals("coral");
        if (shouldShowUpdateInfoNotification) {
            NotificationManager notificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createUpdateInfoNotificationChannel(ctx, notificationManager);
            }
            notificationManager.notify(oneShotUpdateInfoNotificationID,
                    getUpdateInfoNotification(ctx));
        }
    }

    private Notification getNotification(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(EXTRA_FROM_NOTIFICATION, true);

        PendingIntent contentIntent = PendingIntent
                .getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, panicChannelID)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setContentTitle(context.getString(R.string.discover_panic_title))
                .setContentText(context.getString(R.string.discover_panic_desc))
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM)
                .setAutoCancel(true)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    private Notification getUpdateInfoNotification(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_INFO_URL));

        PendingIntent contentIntent = PendingIntent
                .getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String longDescription = context.getString(R.string.update_info_desc_long);

        Notification notification = new NotificationCompat.Builder(context, updateInfoChannelID)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setContentTitle(context.getString(R.string.update_info_title))
                .setContentText(context.getString(R.string.update_info_desc))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(longDescription))
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM)
                .setAutoCancel(false)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
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

    @RequiresApi(Build.VERSION_CODES.O)
    private void createUpdateInfoNotificationChannel(Context ctx,
            NotificationManager notificationManager) {
        NotificationChannel notificationChannel = new NotificationChannel(
                updateInfoChannelID,
                ctx.getString(R.string.update_info_channel_title),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationChannel.setDescription(ctx.getString(R.string.update_info_channel_desc));
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
