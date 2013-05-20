package com.redgps.reporting;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.Timestamp;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import java.text.SimpleDateFormat;

import com.red_folder.phonegap.plugin.backgroundservice.BackgroundService;

public class LocationService extends BackgroundService {

	private final static String TAG = LocationService.class.getSimpleName();
	private String serverIp = "0.0.0.0";
	private int port = 0;
	
	@SuppressLint("SimpleDateFormat")
	@Override
	protected JSONObject doWork() {
		String identifier = null;
		TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
		identifier = mngr.getDeviceId();
		JSONObject result = new JSONObject();
		boolean isUsingGps = true;
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
				isUsingGps = false;
			} 
			String msg = "";
			if (location != null) {
				GpsStatus status = lm.getGpsStatus(null);
				Iterator<GpsSatellite> stats = status.getSatellites().iterator();
				int satelliteCount = 0;
				while(stats.hasNext()) {
					satelliteCount++;
					stats.next();
				}
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				double bearing = location.getBearing();
				double speed = location.getSpeed();
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a");
				String fecha = sdf.format(date);
				TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
				String number = tm.getLine1Number();
				msg =  "<b>Ultimo reporte: <b>" +  fecha +
						"<br /><b>Latitud: </b>" + Double.toString(latitude) +
						"<br /><b>Longitud: </b>" + Double.toString(longitude) + 
						"<br /><b>Satelites:</b>" + satelliteCount +
						"<br /><b>IMEI: </b>" + identifier;
				String info = ">ANDROID|" + (int)(System.currentTimeMillis() / 1000L) + "|" + 
						identifier + "|" + 
						Double.toString(longitude) + "|" +
						Double.toString(latitude) + "|" +
						(number != null ? number : "") + "|" +
						bearing + "|" +
						Double.toString(speed) + "|" +  
						satelliteCount + "|" + 
						(isUsingGps ? "false" : "true");
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
			Log.d(LocationService.TAG, msg);
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
