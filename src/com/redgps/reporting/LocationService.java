package com.redgps.reporting;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.red_folder.phonegap.plugin.backgroundservice.BackgroundService;

public class LocationService extends BackgroundService {

	private final static String TAG = LocationService.class.getSimpleName();
	private String serverIp = "0.0.0.0";
	private int port = 0;
	
	@Override
	protected JSONObject doWork() {
		String identifier = null;
		TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
		identifier = mngr.getDeviceId();
		JSONObject result = new JSONObject();
		try {
			LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
			Location location = null;
			boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			boolean isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if(isGpsEnabled) {
				location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			if(isNetworkEnabled && location == null) {
				location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			} 
			String msg = "";
			if (location != null) {
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				double bearing = location.getBearing();
				double speed = location.getSpeed();
				msg = "My location - longitude: " + Double.toString(longitude) +
						" latitude: " + Double.toString(latitude) + 
						" bearing: " + bearing +
						" IMEI: " + identifier +
						" Server: " + this.serverIp +
						" Port: " + this.port;
				String info = identifier + "|" + 
						Double.toString(longitude) + "|" +
						Double.toString(latitude) + "|" +
						bearing + "|" +
						Double.toString(speed);
			    int server_port = this.port;
			    if (this.serverIp != "0.0.0.0") {
				    InetAddress serverAddr = InetAddress.getByName(this.serverIp);  
				    byte[] message = info.getBytes();
				    DatagramPacket p = new DatagramPacket(message, message.length, serverAddr, server_port);
				    DatagramSocket socket = new DatagramSocket();
				    socket.send(p);
				    socket.close();
			    }
			} else {
				msg = "No location found";
			}
			result.put("Message", msg);
			Log.d(TAG, msg);
		} catch (JSONException e) {} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	protected void setConfig(JSONObject config) {
		// TODO Auto-generated method stub
		try {
			if (config.has("server_ip")) {
				this.serverIp = config.getString("server_ip");
			}
			if (config.has("port")) {
				this.port = Integer.parseInt(config.getString("port"));
			}
		} catch (JSONException e) {}
	}
}
