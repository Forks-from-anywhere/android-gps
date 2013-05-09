package com.redgps.reporting;

import java.sql.Date;
import java.text.SimpleDateFormat;

import org.json.JSONObject;
import org.json.JSONException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.red_folder.phonegap.plugin.backgroundservice.BackgroundService;

public class LocationService extends BackgroundService {

	private final static String TAG = LocationService.class.getSimpleName();
	private String mHelloTo = "World";
	
	@SuppressLint("SimpleDateFormat")
	@Override
	protected JSONObject doWork() {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		try {
			/*LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			MyLocationListener listener = new MyLocationListener();
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);*/
			LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
			Location location = null;
			boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			boolean isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if(isNetworkEnabled) {
				location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			} else if(isGpsEnabled) {
				location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			String msg = "";
			if (location != null) {
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				msg = "My location - longitude: " + Double.toString(longitude) +
						" latitude: " + Double.toString(latitude);
			} else {
				msg = "No location found";
			}
			result.put("Message", msg);
			Log.d(TAG, msg);
		} catch (JSONException e) {}
		return result;
	}

	@Override
	protected JSONObject getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected JSONObject initialiseLatestResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setConfig(JSONObject arg0) {
		// TODO Auto-generated method stub

	}
}
