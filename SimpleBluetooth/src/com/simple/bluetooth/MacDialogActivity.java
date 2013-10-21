package com.simple.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MacDialogActivity extends Activity {

	EditText pcMac;
	Button ok, cancel;

	@SuppressLint("WorldWriteableFiles")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pc_mac_dialog);
		
		pcMac = (EditText) findViewById(R.id.pc_mac);
		ok = (Button) findViewById(R.id.btn_ok);
		cancel = (Button) findViewById(R.id.btn_cancel);
		
		SharedPreferences prefs = getSharedPreferences("PREFS",
				MODE_WORLD_WRITEABLE);
		if(prefs.contains("pcMacAddress"))
			pcMac.setText(prefs.getString("pcMacAddress", ""));

		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!validateMac(pcMac.getText().toString())) {
					Toast.makeText(getApplicationContext(), "Wrong MAC address",
							Toast.LENGTH_LONG).show();
					return;
				}
				SharedPreferences prefs = getSharedPreferences("PREFS",
						MODE_WORLD_WRITEABLE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("pcMacAddress", pcMac.getText().toString());
				editor.commit();
				finish();
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	boolean validateMac(String address) {
		return address.matches("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$");

	}

}
