package com.simple.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
	}
	
	// Menu stuff
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.phone_mac : 
			BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			MessageAlert("MAC address", btAdapter.getAddress());
			return true;
			
		case R.id.imei : 
			TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			MessageAlert("IMEI number", manager.getDeviceId());
			return true;
			
		case R.id.pc_mac :
			startActivityForResult(new Intent(this, MacDialogActivity.class), 0);
			return true;
			
		case R.id.controller :
			startActivityForResult(new Intent(this, BluetoothActivity.class), 0);
			return true;
			
		default:
			return false;
		}
	}
	
	public void MessageAlert(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title)
				.setMessage(message)
				.setPositiveButton("OK", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						// finish();
					}
				}).show();
	}

}
