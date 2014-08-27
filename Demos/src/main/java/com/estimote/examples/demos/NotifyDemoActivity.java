package com.estimote.examples.demos;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.estimote.sdk.BeaconManager.MonitoringListener;

/**
 * Demo that shows how to use region monitoring. Two important steps are:
 * <ul>
 * <li>start monitoring region, in example in {@link #onResume()}</li>
 * <li>respond to monitoring changes by registering {@link MonitoringListener} in {@link BeaconManager}</li>
 * </ul>
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class NotifyDemoActivity extends Activity {

  private static final String TAG = NotifyDemoActivity.class.getSimpleName();
  private static final int NOTIFICATION_ID = 123;
  private static final int NOTIFICATION_ID2 = 124;
  private BeaconManager beaconManager;
  private NotificationManager notificationManager;
  private Region region;
  private Bitmap bmBigPicture;
  private TextView statusTextView;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.notify_demo);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    Beacon beacon = getIntent().getParcelableExtra(ListBeaconsActivity.EXTRAS_BEACON);
    region = new Region("regionId", beacon.getProximityUUID(), beacon.getMajor(), beacon.getMinor());
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    beaconManager = new BeaconManager(this);

    // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
    // In order for this demo to be more responsive and immediate we lower down those values.
    beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
    bmBigPicture = BitmapFactory.decodeResource(getResources(), R.drawable.tetris);
    statusTextView = (TextView) findViewById(R.id.status);
    beaconManager.setMonitoringListener(new MonitoringListener() {
      @Override
      public void onEnteredRegion(Region region, List<Beacon> beacons) {
        postNotification("Do you want to play tetris?");
        statusTextView.setText("entered region");
      }

      @Override
      public void onExitedRegion(Region region) {
    	  postNotification2("Exited region");
    	  statusTextView.setText("Exited region");
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();

    notificationManager.cancel(NOTIFICATION_ID);
    notificationManager.cancel(NOTIFICATION_ID2);
    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
        try {
          beaconManager.startMonitoring(region);
        } catch (RemoteException e) {
          Log.d(TAG, "Error while starting monitoring");
        }
      }
    });
  }

  @Override
  protected void onDestroy() {
    notificationManager.cancel(NOTIFICATION_ID);
    notificationManager.cancel(NOTIFICATION_ID2);
    beaconManager.disconnect();
    super.onDestroy();
  }

  private void postNotification(String msg) {
    Intent notifyIntent = new Intent(NotifyDemoActivity.this, NotifyDemoActivity.class);
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, 
			new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://long567.dothome.co.kr/tetris/tetris.html")), 0);
  /*  PendingIntent pendingIntent = PendingIntent.getActivities(
        NotifyDemoActivity.this,
        0,
        new Intent[]{notifyIntent},
        PendingIntent.FLAG_UPDATE_CURRENT);*/
    Notification notification = new Notification.BigPictureStyle(
    		new Notification.Builder(NotifyDemoActivity.this)
        .setSmallIcon(R.drawable.beacon_gray)
        .setContentTitle("Tetris!")
        .setContentText(msg)
        .setAutoCancel(true)
        .setLargeIcon(bmBigPicture)
        .setTicker("Entered region")
      //  .setContentIntent(intent)
        
        .addAction(R.drawable.ic_launcher, "Connect", intent))
        .bigPicture(bmBigPicture)
        .setBigContentTitle("Tetris!")
		.setSummaryText("Do you want to play tetris?")
        .build();
    
    notification.defaults |= Notification.DEFAULT_SOUND;
    notification.defaults |= Notification.DEFAULT_LIGHTS;
    notificationManager.notify(NOTIFICATION_ID, notification);
    notificationManager.cancel(NOTIFICATION_ID2);
 
    
  }
  private void postNotification2(String msg) {
	    Intent notifyIntent = new Intent(NotifyDemoActivity.this, NotifyDemoActivity.class);
	    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	 
	    
	    Notification notification = new Notification.BigTextStyle(
				new Notification.Builder(NotifyDemoActivity.this)
					.setSmallIcon(R.drawable.beacon_gray)
					.setContentTitle("Exited region")
					.setContentText("Exited region")	
					.setLargeIcon(bmBigPicture)
					.setTicker("Exited region"))
		.setSummaryText("Exited region")
		.bigText("If you want to play tetris, go closer to the beacon").build();

	    notification.defaults |= Notification.DEFAULT_SOUND;
	    notification.defaults |= Notification.DEFAULT_LIGHTS;
	    notificationManager.notify(NOTIFICATION_ID2, notification);
	    notificationManager.cancel(NOTIFICATION_ID);
	 
	    
	  }
  
}
