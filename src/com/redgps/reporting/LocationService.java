package com.redgps.reporting;

import java.sql.Date;
import java.text.SimpleDateFormat;

import org.json.JSONObject;
import org.json.JSONException;
import android.annotation.SuppressLint;
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
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String now = df.format(new Date(System.currentTimeMillis()));

			String msg = "Hello " + this.mHelloTo + " - its currently " + now;
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
