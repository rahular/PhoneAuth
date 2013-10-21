package com.bluetooth.daemon;

import com.mac.register.PhoneData;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluetoothLogin {

	static String lineRead = "";
	static boolean exit = false;

	// start server
	private void startListener() throws IOException {
		ArrayList<PhoneData> data = read();

		while (!exit) {
			exit = true;

			// Create a UUID for SPP
			javax.bluetooth.UUID uuid = new javax.bluetooth.UUID("1101", true);
			// Create the service url
			String connectionString = "btspp://localhost:" + uuid
					+ ";name=Sample SPP Server";

			// open server url
			StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector
					.open(connectionString);

			// Wait for client connection
			System.out
					.println("\nServer Started. Waiting for clients to connect...");
			StreamConnection connection = streamConnNotifier.acceptAndOpen();

			// verify MAC address
			int i = 0;
			RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
			byte[] actualMac = Encryptor.encrypt(dev.getBluetoothAddress());
			while (i < data.size()) {
				exit = true;
				byte[] tempMac = data.get(i).getMac();
				for (int counter = 0; counter < actualMac.length; counter++) {
					if (tempMac[counter] != actualMac[counter]) {
						// System.out.println("MAC address did not match");
						exit = false;
						break;
					}
				}
				if (exit)
					break;
				i++;
			}

			// read string from spp client
			InputStream inStream = connection.openInputStream();
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					inStream));
			lineRead = bReader.readLine();
			System.out.println(lineRead);

			if (exit) {
				byte[] imeiFromFile = data.get(i).getImei();
				byte[] actualImei = Encryptor.encrypt(lineRead);
				for (int counter = 0; counter < actualImei.length; counter++) {
					// System.out.println(imeiFromFile[counter] + "," +
					// actualImei[counter]);
					if (imeiFromFile[counter] != actualImei[counter]) {
						// System.out.println("IMEI numbers did not match");
						exit = false;
						break;
					}
				}
			}

			// close connection
			connection.close();
			streamConnNotifier.close();

			if (exit)
				System.out.println("exiting from BluetoothLogin");
		}
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<PhoneData> read() {
		ArrayList<PhoneData> data = null;
		try {
			FileInputStream fis = null;
			fis = new FileInputStream("/usr/local/bin/.AuthData.dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			data = (ArrayList<PhoneData>) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	public static void main(String[] args) {

		// display local device address and name
		LocalDevice localDevice = null;
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			e.printStackTrace();
		}
		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());

		try {
			new BluetoothLogin().startListener();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}