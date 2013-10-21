package com.simple.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothStalker extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Intent startServiceIntent = new Intent(context, BluetoothSender.class);
		
		if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
					BluetoothAdapter.ERROR);
			switch (state) {
			case BluetoothAdapter.STATE_OFF:
				context.stopService(startServiceIntent);
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				break;
			case BluetoothAdapter.STATE_ON:
				context.startService(startServiceIntent);
				break;
			case BluetoothAdapter.STATE_TURNING_ON:
				break;
			}
		}
	}

}
