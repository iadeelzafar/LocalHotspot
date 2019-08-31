package com.iadeelzafar.localhotspot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import static com.iadeelzafar.localhotspot.HotspotService.ACTION_STOP;

public class HotspotNotificationManager {

  public static final int HOTSPOT_NOTIFICATION_ID = 666;
  private Context context;
  private NotificationManager notificationManager;
  public static final String HOTSPOT_SERVICE_CHANNEL_ID = "DANG_CH";

  public HotspotNotificationManager(Context context) {
    this.context = context;
    notificationManager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
  }

  private void hotspotNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel hotspotServiceChannel = new NotificationChannel(
          HOTSPOT_SERVICE_CHANNEL_ID,
          "DANG NAME",
          NotificationManager.IMPORTANCE_DEFAULT);
      hotspotServiceChannel.setDescription("DES");
      hotspotServiceChannel.setSound(null, null);
      notificationManager.createNotificationChannel(hotspotServiceChannel);
    }
  }

  @NonNull public Notification buildForegroundNotification() {
    Intent targetIntent = new Intent(context, HotspotActivity.class);
    targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent contentIntent =
        PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    hotspotNotificationChannel();

    Intent stopIntent = new Intent(context, HotspotService.class).setAction(ACTION_STOP);
    PendingIntent stopHotspot =
        PendingIntent.getService(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    return new NotificationCompat.Builder(context)
        .setContentTitle("Title")
        .setContentText("Hotspot running")
        .setContentIntent(contentIntent)
        .setWhen(System.currentTimeMillis())
        .addAction(1,
            "STOP",
            stopHotspot)
        .setChannelId(HOTSPOT_SERVICE_CHANNEL_ID)
        .build();
  }

  public void dismissNotification() {
    notificationManager.cancel(HOTSPOT_NOTIFICATION_ID);
  }
}
