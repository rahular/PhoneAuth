package com.simple.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class BluetoothActivity extends Activity implements
		android.view.View.OnClickListener, OnSeekBarChangeListener {

	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;

	// UI buttons
	Button lock, unlock, reboot, shutDown, sendCmd;
	EditText cmdLine;
	SeekBar volume, brightness;
	CheckBox runInTerm;

	// Well known SPP UUID
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static String address;

	// EXPERIMENTAL
	private int brightnessProgress, volumeProgress;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_activity);
		
		SharedPreferences prefs = getSharedPreferences("PREFS",
				MODE_WORLD_WRITEABLE);
		if(prefs.contains("pcMacAddress"))
			address = prefs.getString("pcMacAddress", "");

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		CheckBTState();

		lock = (Button) findViewById(R.id.btn_lock);
		unlock = (Button) findViewById(R.id.btn_unlock);
		reboot = (Button) findViewById(R.id.btn_reboot);
		shutDown = (Button) findViewById(R.id.btn_shutdown);
		sendCmd = (Button) findViewById(R.id.btn_sendcmd);

		volume = (SeekBar) findViewById(R.id.sb_volume);
		brightness = (SeekBar) findViewById(R.id.sb_brightness);

		cmdLine = (EditText) findViewById(R.id.et_cmd);

		runInTerm = (CheckBox) findViewById(R.id.cb_run);

		lock.setOnClickListener(this);
		unlock.setOnClickListener(this);
		reboot.setOnClickListener(this);
		shutDown.setOnClickListener(this);
		sendCmd.setOnClickListener(this);

		brightness.setOnSeekBarChangeListener(this);
		volume.setOnSeekBarChangeListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	void sendCommand(String strMsg) {
		strMsg += "\n";
		byte[] msgBuffer = strMsg.getBytes();
		try {
			BluetoothDevice device = btAdapter.getRemoteDevice(address);
			try {
				btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				AlertBox(
						"Fatal Error",
						"In onResume() and socket create failed: "
								+ e.getMessage() + ".");
			}

			btAdapter.cancelDiscovery();
			try {
				btSocket.connect();
			} catch (IOException e) {
				try {
					btSocket.close();
				} catch (IOException e2) {
					AlertBox("Fatal Error",
							"In onResume() and unable to close socket during connection failure"
									+ e2.getMessage() + ".");
				}
			}
			try {
				outStream = btSocket.getOutputStream();
			} catch (IOException e) {
				AlertBox(
						"Fatal Error",
						"In onResume() and output stream creation failed:"
								+ e.getMessage() + ".");
			}
			outStream.write(msgBuffer);
		} catch (IOException e) {
			String msg = "An exception occurred during write: "
					+ e.getMessage();
			if (address.equals("00:00:00:00:00:00"))
				msg = msg
						+ ".\n\nUpdate your server address from 00:00:00:00:00:00"
						+ "to the correct address on line 37 in the java code";
			msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString()
					+ " exists on server.\n\n";

			AlertBox("Fatal Error", msg);
		}
	}

	private void CheckBTState() {
		if (btAdapter == null) {
			AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
		} else {
			if (btAdapter.isEnabled()) {
				// do something
			} else {
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	public void AlertBox(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title)
				.setMessage(message + " Press OK to exit.")
				.setPositiveButton("OK", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				}).show();
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

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_lock) {
			sendCommand("gnome-screensaver-command -l");
		} else if (id == R.id.btn_unlock) {
			sendCommand("gnome-screensaver-command -d");
		} else if (id == R.id.btn_reboot) {
			// TODO : add reboot command
		} else if (id == R.id.btn_shutdown) {
			// TODO : add shutdown command
		} else if (id == R.id.btn_sendcmd) {
			if (cmdLine.getText().toString().isEmpty()) {
				Toast.makeText(getApplicationContext(), "Nothing to execute",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (runInTerm.isChecked()) {
				sendCommand("gnome-terminal -x "
						+ cmdLine.getText().toString());
			}
			else
				sendCommand(cmdLine.getText().toString());
			cmdLine.setText("");
		}

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar.getId() == R.id.sb_brightness)
			brightnessProgress = progress;
		else if (seekBar.getId() == R.id.sb_volume)
			volumeProgress = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// do something useful
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar.getId() == R.id.sb_brightness) {
			sendCommand("xbacklight -set " + brightnessProgress);
		} else if (seekBar.getId() == R.id.sb_volume) {
			sendCommand("pactl set-sink-volume 0 -- " + volumeProgress + "%");
		}
	}
}