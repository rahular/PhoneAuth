package com.simple.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class BluetoothSender extends Service {

	// Bluetooth related objects
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static String address;

	private final IBinder mBinder = new MyBinder();
	private Handler handler = new Handler();
	private boolean sendOrder;

	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferences prefs = getSharedPreferences("PREFS",
				MODE_WORLD_WRITEABLE);
		if (prefs.contains("pcMacAddress"))
			address = prefs.getString("pcMacAddress", "");
		sendOrder = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sendOrder = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				Log.d("BluetoothSender", "Sending command..");
				sendCommand();
				if (sendOrder)
					handler.postDelayed(this, 10000);
			}
		};
		handler.postDelayed(runnable, 1000);

		Log.d("BluetoothSender", "onStartCommand");
		Toast.makeText(getApplicationContext(), "Bluetooth Service Started",
				Toast.LENGTH_SHORT).show();
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		BluetoothSender getService() {
			return BluetoothSender.this;
		}
	}

	void sendCommand() {
		TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String strMsg = manager.getDeviceId() + "\n";
		byte[] msgBuffer = strMsg.getBytes();
		try {
			BluetoothDevice device = btAdapter.getRemoteDevice(address);
			try {
				btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				e.printStackTrace();
			}

			btAdapter.cancelDiscovery();
			try {
				btSocket.connect();
			} catch (IOException e) {
				try {
					btSocket.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			outStream.write(msgBuffer);

			Log.d("BluetoothSender", "Successfully sent..");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}