package com.iadeelzafar.localhotspot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.iadeelzafar.localhotspot.HotspotNotificationManager.HOTSPOT_NOTIFICATION_ID;

/**
 * HotspotService is used to add a foreground service for the wifi hotspot.
 * Created by Adeel Zafar on 07/01/2019.
 */

public class HotspotService extends Service implements HotspotStateListener {

  public static final String ACTION_TOGGLE_HOTSPOT = "toggle_hotspot";
  public static final String ACTION_LOCATION_ACCESS_GRANTED = "location_access_granted";


  public static final String ACTION_STOP = "hotspot_stop";
  private final IBinder serviceBinder = new HotspotBinder();
  private WifiHotspotManager hotspotManager;
  private WifiManager wifiManager;
  private HotspotNotificationManager hotspotNotificationManager;
  private ZimHostCallbacks zimHostCallbacks;

  @Override public void onCreate() {
    super.onCreate();
    hotspotNotificationManager = new HotspotNotificationManager(this);
    hotspotManager = new WifiHotspotManager(this);

  }

  @Override public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
    switch (intent.getAction()) {

      case ACTION_TOGGLE_HOTSPOT:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          if (hotspotManager.isHotspotStarted()) {
            stopHotspotAndDismissNotification();
          } else {
            zimHostCallbacks.requestLocationAccess();
          }
        }
        break;

      case ACTION_LOCATION_ACCESS_GRANTED:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          hotspotManager.turnOnHotspot();
        }
        break;


      case ACTION_STOP:
        stopHotspotAndDismissNotification();
        break;

      default:
        break;
    }
    return START_NOT_STICKY;
  }

  @Nullable @Override public IBinder onBind(@Nullable Intent intent) {
    return serviceBinder;
  }

  //Dismiss notification and turn off hotspot for devices>=O
  private void stopHotspotAndDismissNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      hotspotManager.turnOffHotspot();
    } else {
      stopForeground(true);
      stopSelf();
      hotspotNotificationManager.dismissNotification();
    }
  }

  private void startForegroundNotificationHelper() {
    startForeground(HOTSPOT_NOTIFICATION_ID,
        hotspotNotificationManager.buildForegroundNotification());
  }

  @Override public void onHotspotTurnedOn(@NonNull WifiConfiguration wifiConfiguration) {
    startForegroundNotificationHelper();
    zimHostCallbacks.onHotspotTurnedOn(wifiConfiguration);
  }

  @Override public void onHotspotFailedToStart() {
    zimHostCallbacks.onHotspotFailedToStart();
  }

  @Override public void onHotspotStopped() {
    stopForeground(true);
    stopSelf();
    hotspotNotificationManager.dismissNotification();
  }


  public class HotspotBinder extends Binder {

    @NonNull public HotspotService getService() {
      return HotspotService.this;
    }
  }

  public void registerCallBack(@Nullable ZimHostCallbacks myCallback) {
    zimHostCallbacks = myCallback;
  }
}
