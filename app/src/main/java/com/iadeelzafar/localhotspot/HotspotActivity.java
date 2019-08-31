package com.iadeelzafar.localhotspot;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.iadeelzafar.localhotspot.HotspotService.ACTION_LOCATION_ACCESS_GRANTED;
import static com.iadeelzafar.localhotspot.HotspotService.ACTION_TOGGLE_HOTSPOT;

public class HotspotActivity extends AppCompatActivity implements ZimHostCallbacks,LocationCallbacks {
  private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 102;
  private LocationServicesHelper locationServicesHelper;
  private HotspotService hotspotService;
  private ServiceConnection serviceConnection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_hotspot);
    Button button = (Button) findViewById(R.id.hotspotActionButton);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        toggleHotspot();
      }
    });
    locationServicesHelper = new LocationServicesHelper(this);

    serviceConnection = new ServiceConnection() {

      @Override
      public void onServiceConnected(ComponentName className, IBinder service) {
        hotspotService = ((HotspotService.HotspotBinder) service).getService();
        hotspotService.registerCallBack(HotspotActivity.this);
      }

      @Override
      public void onServiceDisconnected(ComponentName arg0) {
      }
    };
  }

  private void toggleHotspot() {
    //Check if location permissions are granted
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
      //Toggle hotspot if location permissions are granted
      startService(createHotspotIntent(
          ACTION_TOGGLE_HOTSPOT));
    } else {
      //Ask location permission if not granted
      ActivityCompat.requestPermissions(this,
          new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
          MY_PERMISSIONS_ACCESS_FINE_LOCATION);
    }
  }

  private Intent createHotspotIntent(String action) {
    return new Intent(this, HotspotService.class).setAction(action);
  }
  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode == MY_PERMISSIONS_ACCESS_FINE_LOCATION) {
      if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          toggleHotspot();
        }
      }
    }
  }
  @Override protected void onStart() {
    super.onStart();
    bindService();
  }

  @Override protected void onStop() {
    super.onStop();
    unbindService();
  }

  private void bindService() {
    bindService(new Intent(this, HotspotService.class), serviceConnection,
        Context.BIND_AUTO_CREATE);
  }

  private void unbindService() {
    if (hotspotService != null) {
      unbindService(serviceConnection);
    }
  }

  @Override public void onHotspotTurnedOn(@NonNull WifiConfiguration wifiConfiguration) {
        Toast.makeText(this,"Hotspot Turned on "+wifiConfiguration.SSID+ "\n" + wifiConfiguration.preSharedKey,Toast.LENGTH_LONG).show();
  }

  @Override public void onHotspotFailedToStart() {
    Toast.makeText(this,"Hotspot Turned off ",Toast.LENGTH_LONG).show();
  }

  @Override public void requestLocationAccess() {
    locationServicesHelper.setupLocationServices();
  }

  @Override public void onLocationSet() {
    startService(createHotspotIntent(ACTION_LOCATION_ACCESS_GRANTED));
  }
}
