package com.redgps.reporting;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
//import android.app.Activity;
import android.view.Menu;
import org.apache.cordova.DroidGap;
import android.widget.Toast;

public class MainActivity extends DroidGap {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		super.loadUrl("file:///android_asset/www/index.html");
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		MyLocationListener listener = new MyLocationListener();
		boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		boolean isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(isNetworkEnabled) {
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
		} else if(isGpsEnabled) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public class MyLocationListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location arg0) {
			// TODO Auto-generated method stub
			String msg = "Hello  - longitude is: " + 
					Double.toString(arg0.getLongitude()) + " and latitude is: " +
					Double.toString(arg0.getLatitude());
			//Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			Log.d(LocationService.class.getSimpleName(), msg);
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			Toast.makeText( getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
