package com.bluetooth.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluetoothDaemon {

	private static String lineRead = "";
	private static String lock = "gnome-screensaver-command -l";
	private static String[] env;

	// counters
	static int prevCount = 0, count = 0;

	// constants for checking if the screensaver is active
	private static final String COMMAND = "gnome-screensaver-command -q |  grep -q 'is active'";
	private static final String[] OPEN_SHELL = { "/bin/sh", "-c", COMMAND };
	private static final int EXPECTED_EXIT_CODE = 0;

	// start server
	private void startDaemon() throws IOException {

		// Create a UUID for SPP
		javax.bluetooth.UUID uuid = new javax.bluetooth.UUID("1101", true);
		// Create the service url
		String connectionString = "btspp://localhost:" + uuid
				+ ";name=Sample SPP Server";

		// get environment variables
		String line;
		int i = 0;
		BufferedReader br;

		br = new BufferedReader(
				new FileReader(new File("/tmp/environment.log")));
		while ((line = br.readLine()) != null)
			i++;
		env = new String[i];
		i = 0;
		br.close();
		br = new BufferedReader(
				new FileReader(new File("/tmp/environment.log")));
		while ((line = br.readLine()) != null) {
			env[i++] = line;
		}

		do {
			// open server url
			StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector
					.open(connectionString);

			// Wait for client connection
			System.out
					.println("\nServer Started. Waiting for clients to connect...");
			StreamConnection connection = streamConnNotifier.acceptAndOpen();

			// verify MAC address
			RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
			byte[] macFromFile = read("MAC");
			byte[] imeiFromFile = read("IMEI");
			byte[] actualMac = Encryptor.encrypt(dev.getBluetoothAddress());
			for (int counter = 0; counter < actualMac.length; counter++) {
				if (macFromFile[counter] != actualMac[counter]) {
					System.out.println("locking now..");
					Runtime.getRuntime().exec(lock, env);
					System.exit(0);
				}
			}

			InputStream inStream = connection.openInputStream();
			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					inStream));
			lineRead = bReader.readLine();
			count++;
			
			// execute commands
			doStuff(lineRead);
			
			// check IMEI
			byte[] actualImei = Encryptor.encrypt(lineRead);
			for (int counter = 0; counter < actualImei.length; counter++) {
				if (imeiFromFile[counter] != actualImei[counter]) {
					System.out.println("locking now..");
					Runtime.getRuntime().exec(lock, env);
					System.exit(0);
				}
			}
			

			System.out.println(lineRead);
			System.out.println("prevCount : " + prevCount);
			System.out.println("count : " + count);

			streamConnNotifier.close();
			if(count % 10 == 0) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.gc();						
					}
				}).start();
			}

		} while (true);
	}
	
	static void doStuff(String command) {
		try {
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			System.out.println(command);
		}
	}

	private static boolean isScreenSaverActive() {
		final Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(OPEN_SHELL, env);
			return process.waitFor() == EXPECTED_EXIT_CODE;
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static byte[] read(String type) {
		byte[] hash = null;
		try {
			FileInputStream fis = null;
			fis = new FileInputStream("/home/" + getUserName() + "/."
					+ getUserName() + ".dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			if (type.equals("MAC"))
				hash = (byte[]) ois.readObject();
			if (type.equals("IMEI")) {
				hash = (byte[]) ois.readObject();
				hash = (byte[]) ois.readObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	static String getUserName() {
		try {
			String line;
			Process p = Runtime.getRuntime().exec("whoami", env);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			if ((line = in.readLine()) != null) {
				return line.toString();
			}
			in.close();
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public static void main(String args[]) {

		// display local device address and name
		LocalDevice localDevice = null;
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e1) {
			e1.printStackTrace();
		}
		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());

		// looper for communication check
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(30000);
						if (count <= prevCount) {
							System.out.println("locking now..");
							Runtime.getRuntime().exec(lock, env);
							System.exit(0);
						} else {
							prevCount = count;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		// looper for checking if screen saver is active
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (isScreenSaverActive()) {
						System.out
								.println("screen saver activated. Daemon exiting..");
						System.exit(0);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();

		try {
			new BluetoothDaemon().startDaemon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}